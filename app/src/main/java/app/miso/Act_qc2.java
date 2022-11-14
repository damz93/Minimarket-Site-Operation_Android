package app.miso;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;
import app.miso.bantuan.JSONP2;
import app.miso.bantuan.JSONParser;
import app.miso.bantuan.RequestHandler;

public class Act_qc2 extends Activity implements View.OnClickListener {
    Button btn_back,btn_save;
    ConnectionDetector cd;
    Boolean isInternetPresent = false;
    JSONP2 jParser = new JSONP2();
    Bitmap rotateBMP,bitmapz2;
    String selectedImagePath;
    String mCurrentPhotoPath;
    public static final String UPLOAD_KEY = "image";
    JSONArray contacts = null;
    LinearLayout ln_detail2;
    ProgressDialog dd_dialog;
    Integer jumlah_data=0;
    String s_user,kode_qc,cek,TAG;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mCaptureBtn;
    ImageView img_view_btn, img_view;
    boolean check = true;
    private static final String TAG_SUCCESS = "success";
    int success;
    private static final String TAG_MESSAGE = "message";
    TextView t_user2,tx_gudang,tx_witel;
    String str_gudang_tampil,str_witel_tampil;
    private static String url_simpan_qc =  "http://minisiteoperation.com/json/save-qc.php";
    private static String url_refresh_qc = "http://minisiteoperation.com/json/all-qc-temp.php";
    private static String url_cek_ware = "http://minisiteoperation.com/json/cek-warehouse.php";

    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_qc2);

        lv = (ListView) findViewById(R.id.list_qc2);
        btn_back = findViewById(R.id.btn_back2);
        btn_save = findViewById(R.id.btn_save2);

        btn_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);

        t_user2 = findViewById(R.id.tx_user3);
        tx_gudang = findViewById(R.id.tx_gudangqc);
        tx_witel = findViewById(R.id.tx_witelqc);
        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama+"("+level+")";
        t_user2.setText(semua);
        s_user = stg.getusnme();

        ln_detail2 = findViewById(R.id.ln_detailqc2);
        ln_detail2.setVisibility(View.INVISIBLE);

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            new cek_warehouse().execute();
        }
        else{
            inet_mati();
        }

    }


    @Override
    public void onClick(View v) {
        if(v == btn_back){
            Intent peminjaman = new Intent(Act_qc2.this, Act_qc.class);
            finish();
            startActivity(peminjaman);

        }
        else if(v == btn_save){
            try {
                new simpan_2().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class refresh_tabel2 extends AsyncTask< String, String, String > {
        ArrayList<HashMap< String, String >> contactList = new ArrayList < HashMap < String, String >> ();@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc2.this);
            dd_dialog.setMessage("Loading Data ...");
            dd_dialog.setIndeterminate(false);
            dd_dialog.setCancelable(false);
            dd_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {

            JSONParser jParser = new JSONParser();
            int i;
            Act_set_get a = new Act_set_get();
            String aa = a.getusnme();
            JSONObject json = jParser.ambilURL(url_refresh_qc + "?username="+s_user);
            try {
                contacts = json.getJSONArray("ALL_QC");
                for (i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    HashMap< String, String > map = new HashMap < String, String > ();
                    String serial_number = c.getString("serial_number").trim();
                    String fisik1 = c.getString("fisik1").trim();
                    String adaptor = c.getString("adaptor").trim();
                    String remote = c.getString("remote").trim();
                    String patchcore = c.getString("patchcore").trim();
                    String rj45 = c.getString("rj45").trim();

                    map.put("serial_numberx", serial_number);
                    map.put("fisik1x", fisik1);
                    map.put("adaptorx", adaptor);
                    map.put("remotex", remote);
                    map.put("patchcorex", patchcore);
                    map.put("rj45x", rj45);

                    contactList.add(map);
                }
                jumlah_data=i;
            } catch (JSONException e) {

            }

            return null;
        }@
                Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dd_dialog.dismiss();
            ListAdapter adapter2 = new SimpleAdapter(getApplicationContext(),contactList,
                    R.layout.lay_qc_list2, new String[] {"serial_numberx", "fisik1x", "adaptorx",  "remotex", "patchcorex", "rj45x"},
                    new int[] {R.id.tx_snumber2, R.id.tx_fisik12, R.id.tx_fisik22,R.id.tx_remote2, R.id.tx_fisik32,R.id.tx_fisik42});
            lv.setAdapter(adapter2);
            lv.setVisibility(View.VISIBLE);
            ln_detail2.setVisibility(View.VISIBLE);
            //   jumlah_data = i;
            //   Toast.makeText(Act_peminjaman.this,"Jumlah Data: "+jumlah_data.toString(),Toast.LENGTH_LONG).show();
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class cek_warehouse extends AsyncTask< String, String, String > {
        protected void onPreExecute() {
            super.onPreExecute();
       /*     dd_dialog = new ProgressDialog(Act_qc2.this);
            dd_dialog.setMessage("Loading Data ...");
            dd_dialog.setIndeterminate(false);
            dd_dialog.setCancelable(false);
            dd_dialog.show();*/
        }

        @
                Override
        protected String doInBackground(String...arg0) {

            JSONParser jParser = new JSONParser();
            int i;
            JSONObject json = jParser.ambilURL(url_cek_ware + "?username="+s_user);
            try {
                contacts = json.getJSONArray("CEK_WAREHOUSE");
                for (i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    HashMap< String, String > map = new HashMap < String, String > ();
                    str_gudang_tampil = c.getString("nama").trim();
                    str_witel_tampil = c.getString("regional").trim();
                }
            } catch (JSONException e) {

            }

            return null;
        }@
                Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
      //      dd_dialog.dismiss();
            tx_gudang.setText(str_gudang_tampil);
            tx_witel.setText(str_witel_tampil);

            new refresh_tabel2().execute();

        }
    }
    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_qc2.this).create();
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

    @SuppressLint("StaticFieldLeak")
    public class simpan_2 extends AsyncTask < String, String, String > {

        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc2.this);
            dd_dialog.setMessage("Saving Process.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", s_user));
                url_simpan_qc = url_simpan_qc+ "?usname=" + s_user;
                JSONObject json = jParser.makeHttpRequest(url_simpan_qc, "POST", params);
                success = json.getString("success");
                kode_qc = json.getString("get_kode_qc");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            dd_dialog.dismiss();
            if (success.equals("1")) {
                Toast.makeText(Act_qc2.this, "Saved data", Toast.LENGTH_LONG).show();
                Act_set_get ab = new Act_set_get();
                ab.setkode_log(kode_qc);
                finish();
                Intent a = new Intent(Act_qc2.this,Act_sign_qc.class);
                startActivity(a);
            }
        }

    }
    public void onBackPressed() {
        dd_kembali();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_qc2.this,Act_qc.class);
        finish();
        startActivity(i);
    }

}