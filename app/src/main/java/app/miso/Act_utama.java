package app.miso;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;

public class Act_utama extends Activity implements View.OnClickListener {
    TextView t_user;
    String s_user;
    Button b_logout,b_check,b_bor,b_set,b_pdf,b_qc;
    Intent buka;
    ConnectionDetector cd;
    ImageButton img1;
    Boolean isInternetPresent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_utama);
        //img1.setImageResource(R.drawable.m1);

        t_user = findViewById(R.id.tx_user);

        b_logout = findViewById(R.id.btn_logout);
        b_bor = findViewById(R.id.btn_in_pick);
        //b_bor.setBackgroundResource(R.drawable.m_1);
        b_set = findViewById(R.id.btn_pengaturan);
        //b_set.setBackgroundResource(R.drawable.m_3);
        b_check = findViewById(R.id.btn_stok_op);
        b_qc = findViewById(R.id.btn_qc);
        //b_check.setBackgroundResource(R.drawable.m_2);

        b_logout.setOnClickListener(this);
        b_bor.setOnClickListener(this);
        b_set.setOnClickListener(this);
        b_check.setOnClickListener(this);
        b_qc.setOnClickListener(this);

        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama+"("+level+")";
        t_user.setText(semua);
    }
    @Override
    public void onClick(View v) {
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if(v == b_set){
            Intent n = new Intent(Act_utama.this, Act_pengaturan.class);
            finish();
            startActivity(n);
        }
        else if(v == b_bor){
            Act_set_get a = new Act_set_get();
            String level2 = a.getLevel();
            if (level2 == "Teknisi") {
                Intent n = new Intent(Act_utama.this, Act_peminjaman.class);
                finish();
                startActivity(n);
            }
            else{
                pesanUnkn();
            }
        }
         else if(v == b_logout){
            onBackPressed();
        }
         else if(v == b_check){
             Act_set_get a = new Act_set_get();
             String level = a.getLevel();
             if (level == "Petugas") {
                 Intent n = new Intent(Act_utama.this, Act_stok_opn.class);
                 finish();
                 startActivity(n);
             }
             else{
                 pesanUnkn();
             }
        }
         else if(v == b_qc){
             Act_set_get a = new Act_set_get();
             String level = a.getLevel();
             if (level == "Petugas") {
                 Intent n = new Intent(Act_utama.this, Act_qc.class);
                 finish();
                 startActivity(n);
             }
             else{
                 pesanUnkn();
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
                        dd_kembali();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to logout??").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
    }

    public void dd_kembali(){
        Intent n = new Intent(Act_utama.this, Act_login.class);
        finish();
        startActivity(n);
    }
    public void pesanUnkn() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_utama.this).create();
        dd_dialog.setTitle("Warning");
        dd_dialog.setIcon(R.drawable.warning);
        dd_dialog.setMessage("You don't have access.....");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
}