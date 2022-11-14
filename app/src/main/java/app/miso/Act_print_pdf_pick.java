package app.miso;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import app.miso.bantuan.Act_set_get;

public class Act_print_pdf_pick extends Activity implements View.OnClickListener {
    String kode_stok;
    Button btn_main,btn_ref,btn_home;
    TextView tx_user;
    WebView myWebView;
    WebView printWeb;
    String user;
    String url_print = "https://minisiteoperation.com/print-pdf-inv-picking2.php?kode_log=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_print_pdf_pick);

        Act_set_get at = new Act_set_get();
        tx_user = findViewById(R.id.tx_user900);
        btn_main = findViewById(R.id.btn_back90);
        btn_ref = findViewById(R.id.btn_refres90);
        btn_home = findViewById(R.id.btn_home);

        btn_main.setOnClickListener(this);
        btn_ref.setOnClickListener(this);
        btn_home.setOnClickListener(this);


        tx_user.setText(at.getusnme());
        kode_stok = at.getKode_log();
        url_print = url_print+kode_stok;
        myWebView = (WebView) findViewById(R.id.web_view);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // initializing the printWeb Object
                printWeb = myWebView;
            }
        });
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.loadUrl(url_print);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_main){
            onBackPressed();
        }
        else if (v == btn_home){
            finish();
            Intent abc = new Intent(Act_print_pdf_pick.this, Act_utama.class);
            startActivity(abc);
        }
        else if(v == btn_ref){
//            myWebView.loadUrl(url_print);

            if (myWebView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Calling createWebPrintJob()
                    //    PrintTheWebPage(myWebView);
                    PrintTheWebPage(printWeb);
                } else {
                    // Showing Toast message to user
                    Toast.makeText(Act_print_pdf_pick.this, "Not available for device below Android LOLLIPOP", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Showing Toast message to user
                Toast.makeText(Act_print_pdf_pick.this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onBackPressed(){
        DialogInterface.OnClickListener dd_dialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        //dd_kembali();
                        finish();
                        Intent abc = new Intent(Act_print_pdf_pick.this, Act_sign_kepala.class);
                        startActivity(abc);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to back Sign Petugas Gudang??").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_print_pdf_pick.this,Act_utama.class);
        finish();
        startActivity(i);
    }

    PrintJob printJob;

    // a boolean to check the status of printing
    boolean printBtnPressed = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void PrintTheWebPage(WebView webView) {

        // set printBtnPressed true
        printBtnPressed = true;

        // Creating  PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // setting the name of job
        String jobName = getString(R.string.app_name) + " webpage" + webView.getUrl();

        // Creating  PrintDocumentAdapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        assert printManager != null;
        printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        if (printJob != null && printBtnPressed) {
            if (printJob.isCompleted()) {
                // Showing Toast Message
                Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
            } else if (printJob.isStarted()) {
                // Showing Toast Message
                Toast.makeText(this, "isStarted", Toast.LENGTH_SHORT).show();

            } else if (printJob.isBlocked()) {
                // Showing Toast Message
                Toast.makeText(this, "isBlocked", Toast.LENGTH_SHORT).show();

            } else if (printJob.isCancelled()) {
                // Showing Toast Message
                Toast.makeText(this, "isCancelled", Toast.LENGTH_SHORT).show();

            } else if (printJob.isFailed()) {
                // Showing Toast Message
                Toast.makeText(this, "isFailed", Toast.LENGTH_SHORT).show();

            } else if (printJob.isQueued()) {
                // Showing Toast Message
                Toast.makeText(this, "isQueued", Toast.LENGTH_SHORT).show();

            }
            // set printBtnPressed false
            printBtnPressed = false;
        }
    }

}