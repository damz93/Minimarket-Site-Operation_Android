package app.miso;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import app.miso.bantuan.Act_set_get;

public class Act_sign_teknisi extends Activity implements View.OnClickListener {
    String kode_stok;
    Button btn_back,btn_simpan;
    TextView tx_user;
    WebView myWebView;
    WebView printWeb;
    String user;
    //String url_print = "https://minisiteoperation.com/print-pdf-stok-opm.php?kode_stok=";
    String url_print = "https://minisiteoperation.com/sign/index2.php?kode_log=";
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_sign_teknisi);
        Act_set_get at = new Act_set_get();
        tx_user = findViewById(R.id.tx_user900);
        btn_back = findViewById(R.id.btn_back_sign);
        btn_simpan = findViewById(R.id.btn_next_to_kepala);
        btn_back.setOnClickListener(this);
        btn_simpan.setOnClickListener(this);


        tx_user.setText(at.getusnme()+" - " +at.getKode_log());
        kode_stok = at.getKode_log();
        url_print = url_print+kode_stok;
        myWebView = findViewById(R.id.web_view_sign_teknisi);

        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
//dari sini
        myWebView.getSettings().setLoadsImagesAutomatically(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);

        // Tiga baris di bawah ini agar laman yang dimuat dapat
        // melakukan zoom.
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);
        // Baris di bawah untuk menambahkan scrollbar di dalam WebView-nya
        myWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        myWebView.setWebViewClient(new WebViewClient());
//
        myWebView.loadUrl(url_print);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_back){
            onBackPressed();
        }
        else if(v == btn_simpan){
            Intent abc = new Intent(Act_sign_teknisi.this,Act_sign_kepala.class);
            startActivity(abc);
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
                        dd_kembali();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to back main menu??").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_sign_teknisi.this,Act_utama.class);
        finish();
        startActivity(i);
    }


}