package app.miso;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;
import app.miso.bantuan.JSONP2;

public class Act_pengaturan extends Activity implements View.OnClickListener{
    EditText edt_pskg, edt_psbaru,edt_psbaru2;
    TextView t_user;
    Button btn_perbarui,btn_kembali;
    String str_pskg, str_psbaru, str_psbaru2, str_username;
    AlertDialog dyam_dialog;
    private TextView txtStatus;
    Boolean isInternetPresent = false;
    JSONP2 jParser = new JSONP2();
    ProgressDialog damz_dialog;
    ConnectionDetector cd;

    private String url_sel_user = "http://minisiteoperation.com/json/all-user.php";
    private String url_upd_user = "http://minisiteoperation.com/json/update-user.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_pengaturan);
        edt_pskg = findViewById(R.id.edit_pssekarang);
        edt_psbaru = findViewById(R.id.edit_passw_baru);
        edt_psbaru2 = findViewById(R.id.edit_conf_pasw);
        btn_perbarui = findViewById(R.id.btn_perbarui);
        btn_kembali = findViewById(R.id.btn_kembali);
        btn_perbarui.setOnClickListener(this);
        btn_kembali.setOnClickListener(this);
        txtStatus =  findViewById(R.id.txt_alert2);
        t_user = findViewById(R.id.tx_user2);

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
        str_pskg = edt_pskg.getText().toString();
        str_psbaru = edt_psbaru.getText().toString();
        str_psbaru2 = edt_psbaru2.getText().toString();
        Act_set_get a = new Act_set_get();
        str_username = a.getusnme();

        try {
            if (v == btn_perbarui) {
                if ((str_pskg.length() > 0) && (str_psbaru.length() > 0)&& (str_psbaru2.length() > 0)) {
                    if (!(str_psbaru.equals(str_psbaru2))) {
                        tidak_sama();
                    }
                    else{
                        if (isInternetPresent) {
                            readWebpage(v);
                        }
                        else{
                            inet_mati();
                        }
                    }
                } else {
                    try {
                        field_kosong();
                    } catch (Exception e) {
                        Toast.makeText(Act_pengaturan.this, "Ini errornya: " + e, Toast.LENGTH_LONG).show();
                    }
                }
            } else if (v == btn_kembali) {
                onBackPressed();
            }
        }
        catch(Exception e){
            Toast.makeText(Act_pengaturan.this, "Errornya: " + e, Toast.LENGTH_LONG).show();
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
                        kosong();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to back main menu??").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_pengaturan.this,Act_utama.class);
        finish();
        startActivity(i);
    }
    private void kosong() {
        edt_pskg.setText("");
        edt_psbaru.setText("");
        edt_psbaru2.setText("");
        edt_pskg.requestFocus();
    }
    private void tidak_sama(){
        dyam_dialog = new AlertDialog.Builder(Act_pengaturan.this).create();
        dyam_dialog.setTitle("Warning");
        dyam_dialog.setIcon(R.drawable.warning);
        dyam_dialog.setMessage("Field password baru dan field Ulangi password harus sesuai");
        dyam_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dyam_dialog.show();
    }
    private void field_kosong(){
        AlertDialog dyam_dialog = new AlertDialog.Builder(Act_pengaturan.this).create();
        dyam_dialog.setTitle("Warning");
        dyam_dialog.setIcon(R.drawable.warning);
        dyam_dialog.setMessage("Please Complete the field");
        dyam_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dyam_dialog.show();
    }
    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_pengaturan.this).create();
        dd_dialog.setTitle("Warning");
        dd_dialog.setIcon(R.drawable.warning);
        dd_dialog.setMessage("Please Activate Your Connection Internet...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }

    private class CallWebPageTask extends AsyncTask< String, Void, String > {
        private ProgressDialog dialog;
        protected Context applicationContext;@
                Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog.show(applicationContext, "Updating", "Please Wait...", true);
        }

        @
                Override
        protected String doInBackground(String...urls) {
            String response = "";
            response = getRequest(urls[0]);
            return response;

        }

        @
                Override
        protected void onPostExecute(String result) {
            this.dialog.cancel();
            txtStatus.setText(result);
            String u = txtStatus.getText().toString();
            //Toast.makeText(Act_pengaturan.this,txtStatus.getText().toString(), Toast.LENGTH_LONG).show();
            if (u.substring(27, 29).trim().equals("ok")) {
                new input().execute();
            } else {
                AlertDialog dyam_dialog = new AlertDialog.Builder(Act_pengaturan.this).create();
                dyam_dialog.setTitle("Warning");
                dyam_dialog.setIcon(R.drawable.warning);
                dyam_dialog.setMessage("Password is Wrong...");
                dyam_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dyam_dialog.show();
                edt_pskg.requestFocus();
            }
        }
    }
    public void readWebpage(View view) {
        CallWebPageTask task = new CallWebPageTask();
        task.applicationContext = Act_pengaturan.this;
        String url = url_sel_user + "?username=" + str_username + "&password=" + str_pskg;
   //     Toast.makeText(Act_pengaturan.this,url, Toast.LENGTH_LONG).show();
        task.execute(new String[] {
                url
        });
    }
    public String getRequest(String Url) {
        String sret;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(Url);
        try {
            HttpResponse response = client.execute(request);
            sret = request(response);
        } catch (Exception ex) {
            sret = "Failed Connect to server!";
        }
        return sret;
    }

    public static String request(HttpResponse response) {
        String result = "";
        try {
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader( in ));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            } in .close();
            result = str.toString();
        } catch (Exception ex) {
            result = "Error";
        }
        return result;
    }
    public class input extends AsyncTask < String, String, String > {
        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            //damz_dialog = new ProgressDialog(Act_pengaturan.this);
       //     damz_dialog.setMessage("Updating");
            //damz_dialog.setIndeterminate(false);
            //damz_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", str_username));
                params.add((NameValuePair) new BasicNameValuePair("psword", str_psbaru));
                url_upd_user = url_upd_user+ "?usname=" + str_username+ "&psword=" + str_psbaru;
                JSONObject json = jParser.makeHttpRequest(url_upd_user, "POST", params);
                success = json.getString("success");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
           // damz_dialog.dismiss();
            if (success.equals("1")) {
                Toast.makeText(getApplicationContext(), "Successfully updated password.....", Toast.LENGTH_LONG).show();
                kosong();
                dd_kembali();
            } else {
                //   Toast.makeText(getApplicationContext(), "Password Gagal diupdate", Toast.LENGTH_LONG).show();

            }
        }
    }
}