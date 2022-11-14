package app.miso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;

import static android.view.PixelCopy.request;

public class Act_login extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    String level[] = {
            "Admin",
            "Petugas",
           // "Super Admin",
            "Teknisi"
    };
    Spinner sp_level;
    String pilih_level;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    Integer     success;
    TextView tx_daftar;
    Intent op_form;
    EditText ed_username, ed_pass;
    Button bt_login,bt_cancel;
    JSONArray str_login = null;
    private TextView txtStatus;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    ProgressDialog damz_log;
    String d_usr;
    JSONArray contacts = null;
    WebView wbview_dd;
    String iddz;
    String var_usr, var_pass,var_level;
    private static final String TAG = Act_login.class.getSimpleName();
    String tag_json_obj = "json_obj_req";
    private String surl = "http://minisiteoperation.com/json/login.php";
    private GoogleApiClient client2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_login);


        sp_level = (Spinner) findViewById(R.id.spn_level);
        //ArrayAdapter < String > adapter = new ArrayAdapter < String > (this, android.R.layout.simple_spinner_dropdown_item, level);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.template_spinner,level);
        sp_level.setAdapter(adapter);
        sp_level.setOnItemSelectedListener(this);


        bt_login = (Button) findViewById(R.id.btn_login);
        bt_cancel = (Button) findViewById(R.id.btn_cancel_);

        ed_username = (EditText) findViewById(R.id.edt_username);
        ed_pass = (EditText) findViewById(R.id.edt_password);
        txtStatus = (TextView) findViewById(R.id.txt_alert);
        wbview_dd = (WebView) findViewById(R.id.wbv_dd);
        bt_login.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        //bt_batal.setOnClickListener(this);
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }
    public void onClick(View v) {
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        //wbview_dd.getSettings().setJavaScriptEnabled(true);
        //wbview_dd.setWebViewClient(new MyBrowser());
        //pg_dd();
        var_usr = ed_username.getText().toString();
        var_pass = ed_pass.getText().toString();
        var_level = sp_level.getSelectedItem().toString();
        try {
            if (isInternetPresent) {
                if (v == bt_login) {
                    if ((var_usr.length() > 0) && (var_pass.length() > 0)) {
                        readWebpage(v);

                    } else {
                        //	Toast.makeText(Act_login.this, "Lengkapi field Username maupun Password yang disediakan", Toast.LENGTH_LONG).show();
                        try {
                            AlertDialog dyam_dialog = new AlertDialog.Builder(Act_login.this).create();
                            dyam_dialog.setTitle("Warning");
                            dyam_dialog.setIcon(R.drawable.warning);
                            dyam_dialog.setMessage("Username and Password must be filled....");
                            dyam_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dyam_dialog.show();
                        } catch (Exception e) {
                            Toast.makeText(Act_login.this, "Ini errornya: " + e, Toast.LENGTH_LONG).show();
                        }
                    }
                } else if(v== bt_cancel){
                    kosong();
                }
            } else {
                pesanUnkn();
            }
        } catch (Exception e) {
            Toast.makeText(Act_login.this, "Errornya: " + e, Toast.LENGTH_LONG).show();
        }

        }



    public void onItemSelected(AdapterView< ? > parent, View arg1, int arg2, long arg3) {

        pilih_level = sp_level.getSelectedItem().toString();
        parent.getItemAtPosition(arg2);
        switch (parent.getId()) {
            case R.id.spn_level:
                if (pilih_level.equals("Admin")) {
                   // Toast.makeText(getApplicationContext(), "Pilih Admin...",Toast.LENGTH_LONG).show();
                } else if (pilih_level.equals("Pengawas")) {
                    //Toast.makeText(getApplicationContext(), "Pilih Pengawas...",Toast.LENGTH_LONG).show();
                } else if (pilih_level.equals("Super Admin")) {
                    //Toast.makeText(getApplicationContext(), "Pilih Super Admin...",Toast.LENGTH_LONG).show();
                }
                else{
//                    Toast.makeText(getApplicationContext(), "Pilih Teknisi...",Toast.LENGTH_LONG).show();
                }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public String getRequest (String Url){
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
    public static String request (HttpResponse response){

        String result = "";
        try {
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        } catch (Exception ex) {
            result = "Error";
        }
        return result;
    }
    @
            Override
    public void onStart () {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Act_login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ap.miso/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @
            Override
    public void onStop () {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Act_login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ap.miso/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }

    private class CallWebPageTask extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;
        protected Context applicationContext;

        @
                Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog.show(applicationContext, "Login process", "Please Wait...", true);
        }

        @
                Override
        protected String doInBackground(String... urls) {
            try {
                String response = "";
                response = getRequest(urls[0]);
                return response;
            } catch (Exception e) {
                String a = e.toString();
                Toast.makeText(Act_login.this, "Ini errornya: " + a, Toast.LENGTH_LONG).show();

            }
            return null;
        }

        @
                Override
        protected void onPostExecute(String result) {
            this.dialog.cancel();
            txtStatus.setText(result);
            String u = txtStatus.getText().toString();
            Integer aa = u.length();
            try {
                if (u.substring(27, 29).trim().equals("ok")) {
                    Intent aaa = new Intent(Act_login.this, Act_utama.class);
                    Act_set_get stg = new Act_set_get();
                    String idd = u.substring(68, aa).trim();
                    iddz = idd;
                    stg.setnama(idd);
                    stg.setusnme(var_usr);
                    stg.setlevel(var_level);
                    finish();
                    startActivity(aaa);
                    Toast.makeText(Act_login.this, "Welcome, " + iddz, Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog damz_dialog = new AlertDialog.Builder(Act_login.this).create();
                    damz_dialog.setTitle("Warning");
                    damz_dialog.setIcon(R.drawable.warning);
                    damz_dialog.setMessage("Login Failed, wrong Username and Password... ");
                    damz_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    damz_dialog.show();
                    ed_username.requestFocus();
                }
            } catch (Exception e) {
                //Toast.makeText(Act_login.this,"Error karena: "+e.toString().trim(),Toast.LENGTH_LONG).show();
                AlertDialog damz_dialog = new AlertDialog.Builder(Act_login.this).create();
                damz_dialog.setTitle("Warning");
                damz_dialog.setIcon(R.drawable.warning);
                String a = e.toString();
                damz_dialog.setMessage("Server Connection is problem. Please try again " + u);
                damz_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                damz_dialog.show();
                ed_username.requestFocus();
                //  Toast.makeText(Act_login.this,u, Toast.LENGTH_LONG).show();
            }

        }
    }
    private void kosong() {
        ed_username.setText("");
        ed_pass.setText("");
        ed_username.requestFocus();
    }

    public void readWebpage(View view) {
        CallWebPageTask task = new CallWebPageTask();
        task.applicationContext = Act_login.this;
        String url = surl + "?usname=" + ed_username.getText().toString() + "&psword=" + ed_pass.getText().toString()+ "&level=" + var_level;
        task.execute(new String[] {
                url
        });;
    }



    private class MyBrowser extends WebViewClient {@
            Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
    }
    public void onBackPressed() {

    }

    public void pesanUnkn() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_login.this).create();
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


}