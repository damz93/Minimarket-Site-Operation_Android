package app.miso;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.http.Request;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Response;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;
import app.miso.bantuan.GPSTracker;
import app.miso.bantuan.JSONP2;
import app.miso.bantuan.JSONParser;
import app.miso.bantuan.RequestHandler;

public class Act_stok_opn extends Activity implements View.OnClickListener{
    ListView lv;
    Integer juml_qty = 0;
    String str1,str_username;
    final List< String > list = new ArrayList < String > ();
    ProgressDialog damz_log;
    public static String url_cari_barang = "http://minisiteoperation.com/json/cari-barang3.php";
    private static String url_lihat_nama_barang  = "http://minisiteoperation.com/json/nama-barang3.php";
    public static String url_simpan_sementara = "http://minisiteoperation.com/json/save-stok-temp.php";
    private static String url_refresh_stok_opm = "http://minisiteoperation.com/json/all-stok-opname.php";
    private static String url_delete_satu_data =  "http://minisiteoperation.com/json/delete-stok-op-temp-detail.php";
    private static String url_delete_semua_data = "http://minisiteoperation.com/json/delete-stok-op-temp.php";
    String s_serial_numb,s_kode_barang, s_nama_barang, s_qty, s_user,s_qty_system, s_qty_fisik;
    LinearLayout ln_detail;
    TextView tx_nama_barang;
    AutoCompleteTextView edt_kode_barang;
    EditText edt_serial_numb,edt_qt_syst, edt_qt_fis;
    Button btn_brcode,btn_add_to,btn_kembali,btn_qrcode,btn_cari_kodb,btn_refresh,btn_clearall,btn_next,btn_clearkode,btn_clear_sn;
    String str_kode_barang,st_cek_br;
    String str_kode_barang_del, str_username_del;
    Integer jumlah_data=0;
    String qt_syst, qt_fis;
    TextView t_user;
    ConnectionDetector cd;
    Boolean isInternetPresent = false;
    JSONP2 jParser = new JSONP2();
    JSONArray contacts = null;
    ProgressDialog dd_dialog;
    Act_set_get abcc = new Act_set_get();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_stok_opm);
        lv = (ListView) findViewById(R.id.list_inventory_log10);
        t_user = findViewById(R.id.tx_user10);
        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama+"("+level+")";
        t_user.setText(semua);
        s_user =nama;
        str_username_del = stg.getusnme();
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            new refresh_tabel().execute();
        }
        else{
            inet_mati();
        }

        try{
            new AmbilNamaBarang10().execute();
        }
        catch (Exception e){
            Toast.makeText(Act_stok_opn.this,"Error karena, "+e,Toast.LENGTH_LONG).show();
        }
        tx_nama_barang = findViewById(R.id.tx_nambar10);
        edt_kode_barang = findViewById(R.id.edt_kode_barang10);
        edt_qt_syst = findViewById(R.id.edt_qty_system10);
        edt_qt_syst.setEnabled(false);
        edt_qt_fis = findViewById(R.id.edt_qty_fisik10);
        edt_serial_numb = findViewById(R.id.edt_noseri10);

        ln_detail = findViewById(R.id.ln_detail10);
        ln_detail.setVisibility(View.INVISIBLE);

        btn_add_to = findViewById(R.id.btn_add_to_list10);
        btn_kembali = findViewById(R.id.btn_kembali10);
        btn_cari_kodb = findViewById(R.id.btn_cari_kode_barang10);
        btn_next = findViewById(R.id.btn_next10);
        btn_brcode = findViewById(R.id.btn_brcode2);
        btn_clearkode = findViewById(R.id.btn_clear_kode10);
        btn_clear_sn = findViewById(R.id.btn_clear_seria1l0);
        btn_brcode.setOnClickListener(this);
        btn_add_to.setOnClickListener(this);
        btn_cari_kodb.setOnClickListener(this);
        btn_kembali.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_clearkode.setOnClickListener(this);
        btn_clear_sn.setOnClickListener(this);

        btn_qrcode = (Button) findViewById(R.id.btn_qr_code10);
        btn_qrcode.setOnClickListener(this);
        btn_brcode.setEnabled(false);
        btn_refresh = (Button) findViewById(R.id.btn_refrs10);
        btn_refresh.setOnClickListener(this);
        btn_clearall = (Button) findViewById(R.id.btn_clear10);
        btn_clearall.setOnClickListener(this);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String kode_barang = ((TextView) view.findViewById(R.id.tx_code10)).getText().toString();

                //Toast.makeText(Act_peminjaman.this, "Kode Barang: "+kode_barang,Toast.LENGTH_LONG).show();
                DialogInterface.OnClickListener dd_dialog = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                str_kode_barang_del = kode_barang;
                                //Toast.makeText(Act_peminjaman.this, str_kode_barang_del+s_user,Toast.LENGTH_LONG).show();
                                new hapus_satu_item().execute();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Act_stok_opn.this);
                builder.setMessage("are you sure you want to delete the item, with the item code *"+kode_barang+"*???").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
            }
        });



    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);


        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (st_cek_br=="brcode") {
            if (scanningResult != null) {
                juml_qty = juml_qty + 1;
                String scanContent = scanningResult.getContents();
                String s = edt_serial_numb.getText().toString();
                //String scanFormat = scanningResult.getFormatName();
                //ed_no_seri.setText("FORMAT: " + scanFormat);
                //ed_no_seri.setText("CONTENT: " + scanContent);
                if (s.equals("")){
                    edt_serial_numb.setText(scanContent);
                    edt_qt_fis.setText(juml_qty.toString());
                }
                else {
                    edt_serial_numb.setText(s+" | "+scanContent+"");
                    edt_qt_fis.setText(juml_qty.toString());
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else{
            if (result != null) {
                if (result.getContents() == null) {
                    androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(Act_stok_opn.this).create();
                    alertDialog.setTitle("Scan Results");
                    alertDialog.setMessage("You cancel scanning");
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "YA",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                } else {
    /*                androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(Act_peminjaman.this).create();
                    alertDialog.setTitle("Hasil Scan");
                    alertDialog.setMessage(result.toString());
                    alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "YA",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();*/
/*
                    edt_kode_barang.setText(result.getContents().toString());
                    str_kode_barang = edt_kode_barang.getText().toString();
                    str_kode_barang = str_kode_barang.replaceAll("\\s+", "");
                    str_kode_barang = str_kode_barang.replace(" ", "");
*/

                    edt_kode_barang.setText(result.getContents().toString());
                    //str_kode_barang = edt_kode_barang.getText().toString();
                    str_kode_barang = edt_kode_barang.getText().toString();
                    str_kode_barang = str_kode_barang.replace(' ','+');

                    if (isInternetPresent) {
                        new cari_kode_barang().execute();
                    }
                    else{
                        inet_mati();
                    }

                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
    @Override
    public void onClick(View v) {
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        //str1 = edt_psbaru.getText().toString();
        Act_set_get a = new Act_set_get();
        str_username = a.getusnme();

        s_serial_numb = edt_serial_numb.getText().toString();
        s_kode_barang = edt_kode_barang.getText().toString();
        s_nama_barang = tx_nama_barang.getText().toString();
        s_qty_system = edt_qt_syst.getText().toString();
        s_qty_system =edt_qt_fis.getText().toString();
        s_user = str_username;


        try {
            if (v== btn_next){
                if (jumlah_data>0) {
                    new refresh_tabel().execute();
                    finish();
                    Intent stok_opm2 = new Intent(Act_stok_opn.this, Act_stok_opn2.class);
                    startActivity(stok_opm2);
                }
                else{
                    AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
                    dd_dialog.setTitle("Warning");
                    dd_dialog.setIcon(R.drawable.warning);
                    dd_dialog.setMessage("No items yet...");
                    dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dd_dialog.show();
                }
            }
            else if(v == btn_clearkode){
                edt_kode_barang.setText("");
                edt_kode_barang.requestFocus();
            }
            else if(v == btn_clear_sn){
                edt_serial_numb.setText("");
                edt_qt_fis.setText("0");
                juml_qty = 0;
            }
            else if (v == btn_clearall){
                DialogInterface.OnClickListener dd_dialog = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                new hapus_semua_item().execute();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Act_stok_opn.this);
                builder.setMessage("are you sure you want to delete all item???").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
            }
            else if (v == btn_add_to) {
                if (((edt_serial_numb.length() > 0) && (edt_kode_barang.length() > 0) && (edt_qt_fis.length()>0)&& (edt_qt_syst.length()>0)&&(tx_nama_barang.getText().toString()!="....."))) {
                    if (isInternetPresent) {
                        s_qty_fisik = edt_qt_fis.getText().toString();
                        s_qty_system = edt_qt_syst.getText().toString();
                        simpan1(s_serial_numb,s_kode_barang, s_nama_barang, s_qty_system, s_qty_fisik, s_user);
                    }
                    else{
                        inet_mati();
                    }

                } else {
                    try {
                        field_kosong();
                    } catch (Exception e) {
                        Toast.makeText(Act_stok_opn.this, "Ini errornya: " + e, Toast.LENGTH_LONG).show();
                    }
                }
            } else if (v == btn_kembali) {
                onBackPressed();
            }
            else if (v == btn_brcode) {
                st_cek_br = "brcode";
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
            }
            else if(v == btn_qrcode){
                st_cek_br = "qrcode";
                final Activity activity = this;
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("SCAN");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
            else if(v == btn_cari_kodb){
                if (isInternetPresent) {
                    str_kode_barang = edt_kode_barang.getText().toString();
//                    str_kode_barang = str_kode_barang.replaceAll("\\s+", "");
  //                  str_kode_barang = str_kode_barang.replace(" ", "");
                    str_kode_barang = str_kode_barang.replace(' ','+');
                    new cari_kode_barang().execute();
                }
                else{
                    inet_mati();
                }

            }
            else if(v == btn_refresh){
                if (isInternetPresent) {
                    //str_kode_barang = edt_kode_barang.getText().toString();
                    kosong();
                    new refresh_tabel().execute();
                }
                else{
                    inet_mati();
                }

            }

        }
        catch(Exception e){
            Toast.makeText(Act_stok_opn.this, "Errornya: " + e, Toast.LENGTH_LONG).show();
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
                        new hapus_semua_item().execute();
                        dd_kembali();
                        kosong();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("are you sure you want to back main menu??").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
    }

    private void field_kosong(){
        AlertDialog dyam_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
        dyam_dialog.setTitle("Warning");
        dyam_dialog.setIcon(R.drawable.warning);
        dyam_dialog.setMessage("Please Complete the field.....");
        dyam_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dyam_dialog.show();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_stok_opn.this,Act_utama.class);
        finish();
        startActivity(i);
    }
    private void kosong() {
        edt_kode_barang.setText("");
        edt_qt_fis.setText("");
        edt_qt_syst.setText("");
        btn_brcode.setEnabled(false);
        juml_qty=0;
        edt_serial_numb.setText("");
        tx_nama_barang.setText(".....");
        edt_kode_barang.requestFocus();
    }
    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
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
    private void item_not_found() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
        dd_dialog.setTitle("Warning");
        dd_dialog.setIcon(R.drawable.warning);
        dd_dialog.setMessage("Item name not found...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
    private void item_found() {
        Toast.makeText(Act_stok_opn.this,"Item name found......",Toast.LENGTH_LONG).show();
        /*AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
        dd_dialog.setTitle("Info");
        dd_dialog.setIcon(R.drawable.info);
        dd_dialog.setMessage("Item name found...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();

         */
    }

    public class cari_kode_barang extends AsyncTask < String, String, String > {

        String aaz,nam_bar,qty,keter;
        int countz = 0;
        String[] str1;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn.this);
            dd_dialog.setMessage("Looking for item code.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.setCancelable(false);
            dd_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            //aaz = str_kode_barang;
            JSONParser jParser = new JSONParser();
            //JSONObject json = jParser.ambilURL(url_cari_barang + "?kode_barang=" + str_kode_barang);
            JSONObject json = jParser.ambilURL(url_cari_barang + "?kode_barang=" + str_kode_barang+"&username="+s_user);
            JSONObject json1 = null;
            try {
                contacts = json.getJSONArray("NAMA");
                str1 = new String[contacts.length()];
                int aai = 0;
                //for (int aai = 0; aai < contacts.length(); aai++) {
                JSONObject c = contacts.getJSONObject(aai);

                json1 = contacts.getJSONObject(aai);
                nam_bar = c.getString("nama_barang");
                keter = c.getString("keter");
                qty = c.getString("qty");
                //}
            } catch (JSONException e) {}
            return null;
        }
        @
                Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dd_dialog.dismiss();
            try{
                tx_nama_barang.setText(nam_bar.toString());
                edt_qt_syst.setText(qty.toString());
                //item_found();
                //Toast.makeText(Act_stok_opn.this,keter,Toast.LENGTH_LONG).show();
                if (keter.equals("NO SERI")){
                    //edt_qt_fis.setText(edt_qt_syst.getText());
                    edt_qt_fis.setText("1");
                    edt_serial_numb.setText("");
                    edt_qt_fis.setEnabled(false);
                    edt_serial_numb.setEnabled(true);
                    btn_brcode.setEnabled(true);
                    edt_serial_numb.requestFocus();
                    //Toast.makeText(Act_stok_opn.this,"Input Serial Number...",Toast.LENGTH_LONG).show();
                    scan_serial_number();
                }
                else{
                    edt_serial_numb.setText("-");
                    edt_qt_fis.setText("");
                    edt_serial_numb.setEnabled(false);
                    edt_qt_fis.setEnabled(true);
                    btn_brcode.setEnabled(false);
                    edt_qt_fis.requestFocus();
                    input_qty();
                    //Toast.makeText(Act_stok_opn.this,"Input Physical Quantity...",Toast.LENGTH_LONG).show();
                }


            }
            catch (Exception e){
                item_not_found();
                edt_kode_barang.requestFocus();
                tx_nama_barang.setText(".....");
                edt_qt_syst.setText("0");
            }
        }

    }
    private void simpan1(String s_serial_numbz,String s_kode_barangz,String s_nama_barangz,String s_qty_systemz,String s_qty_fisikz,String s_userz) {

        class SendPostReqAsyncTask extends AsyncTask < String, Void, String > {
            @
                    Override
            protected void onPreExecute() {
                super.onPreExecute();
                dd_dialog = new ProgressDialog(Act_stok_opn.this);
                dd_dialog.setMessage("Saving process.....");
                dd_dialog.setIndeterminate(false);
                dd_dialog.show();
            }
            @
                    Override
            protected String doInBackground(String...params) {
                String s_serial_numby = params[0];
                String s_kode_barangy = params[1];
                String s_nama_barangy = params[2];
                String s_qtyy = params[3];
                String s_qtyy2 = params[4];
                String s_usery = params[5];
                List<NameValuePair> nameValuePairs = new ArrayList< NameValuePair >();
                nameValuePairs.add(new BasicNameValuePair("serial_numb", s_serial_numby));
                nameValuePairs.add(new BasicNameValuePair("kode_barang", s_kode_barangy));
                nameValuePairs.add(new BasicNameValuePair("nama_barang", s_nama_barangy));
                nameValuePairs.add(new BasicNameValuePair("qty", s_qtyy));
                nameValuePairs.add(new BasicNameValuePair("qty2", s_qtyy2));
                nameValuePairs.add(new BasicNameValuePair("username", s_usery));
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(url_simpan_sementara);
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();

                } catch (ClientProtocolException e) {

                } catch (IOException e) {
                }
                return "success";
            }

            @
                    Override
            protected void onPostExecute(String result) {
                dd_dialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(Act_stok_opn.this, "Saved data", Toast.LENGTH_LONG).show();
                new refresh_tabel().execute();
                kosong();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(s_serial_numb,s_kode_barang, s_nama_barang, s_qty_system, s_qty_fisik, s_user);
    }



    public class refresh_tabel extends AsyncTask < String, String, String > {
        ArrayList< HashMap < String, String >> contactList = new ArrayList < HashMap < String, String >> ();@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn.this);
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
            //JSONObject json = jParser.ambilURL(url_refresh_log_inventory + "?username="+aa);
            JSONObject json = jParser.ambilURL(url_refresh_stok_opm + "?username="+s_user);
            try {
                contacts = json.getJSONArray("LOG_INVENTORY");
                for (i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    HashMap< String, String > map = new HashMap < String, String > ();
                    String kod_barang = c.getString("kdbr").trim();
                    String nam_barang = c.getString("nmbr").trim();
                    String qty_brg = c.getString("qtybr").trim();
                    String keter = c.getString("keter").trim();
                    String qty_brg2 = c.getString("qtybr2").trim();

                    map.put("KODE_BARANGx", kod_barang);
                    if (nam_barang.length()>12) {
                        nam_barang = nam_barang.substring(0, 12) + ".....";
                    }
                    else{
                        nam_barang = nam_barang;
                    }
                    if (keter.length()>20) {
                        keter = keter.substring(0, 20) + ".....";
                    }
                    else{
                        keter = keter;
                    }
                    map.put("NAMA_BARANGx", nam_barang);
                    map.put("NO_SERIx", keter);
                    map.put("QTYx", qty_brg);
                    map.put("QTYx2", qty_brg2);
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
                    R.layout.lay_stok_opm_list, new String[] {"KODE_BARANGx", "NAMA_BARANGx", "QTYx", "QTYx2", "NO_SERIx"},
                    new int[] {R.id.tx_code10, R.id.tx_name10, R.id.tx_qty10, R.id.tx_qty102, R.id.tx_noseri102});
            lv.setAdapter(adapter2);
            lv.setVisibility(View.VISIBLE);
            ln_detail.setVisibility(View.VISIBLE);
            //   jumlah_data = i;
            //   Toast.makeText(Act_peminjaman.this,"Jumlah Data: "+jumlah_data.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public class hapus_satu_item extends AsyncTask < String, String, String > {
        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn.this);
            dd_dialog.setMessage("Deleting.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", str_username_del));
                params.add((NameValuePair) new BasicNameValuePair("kod_barang", str_kode_barang_del));
                str_kode_barang_del = str_kode_barang_del.replace(' ','+');
                url_delete_satu_data = url_delete_satu_data+ "?usname=" + str_username_del+ "&kod_barang=" + str_kode_barang_del;
                JSONObject json = jParser.makeHttpRequest(url_delete_satu_data, "POST", params);
                success = json.getString("success");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            dd_dialog.dismiss();
            if (success.equals("1")) {
                Toast.makeText(getApplicationContext(), "Successfully delete item code.....", Toast.LENGTH_LONG).show();
                // Toast.makeText(getApplicationContext(), url_delete_satu_data, Toast.LENGTH_LONG).show();
                kosong();
                new refresh_tabel().execute();
            } else {
                //   Toast.makeText(getApplicationContext(), "Password Gagal diupdate", Toast.LENGTH_LONG).show();

            }
        }
    }
    public class hapus_semua_item extends AsyncTask < String, String, String > {
        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn.this);
            dd_dialog.setMessage("Deleting.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.show();
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", str_username));
                url_delete_semua_data = url_delete_semua_data+ "?usname=" + str_username;
                JSONObject json = jParser.makeHttpRequest(url_delete_semua_data, "POST", params);
                success = json.getString("success");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            dd_dialog.dismiss();
            if (success.equals("1")) {
                Toast.makeText(getApplicationContext(), "Successfully clear all.....", Toast.LENGTH_LONG).show();
                kosong();
                new refresh_tabel().execute();
            } else {

            }
        }
    }
    private void scan_serial_number() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
        dd_dialog.setTitle("Info");
        dd_dialog.setIcon(R.drawable.info);
        dd_dialog.setMessage("Item name found. Click Button BARCODE, to scan Serial Number...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
    private void input_qty() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn.this).create();
        dd_dialog.setTitle("Info");
        dd_dialog.setIcon(R.drawable.info);
        dd_dialog.setMessage("Item name found. Input Physical Quantity...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }

    public class AmbilNamaBarang10 extends AsyncTask< String, String, String > {
        ArrayList<HashMap< String,
                String >> contactList = new ArrayList < HashMap < String, String >> ();
        HashMap < String, String > map = new HashMap < String, String > ();
        String aaz;
        int countz = 0;
        String[] str1;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();

            damz_log = new ProgressDialog(Act_stok_opn.this);
            damz_log.setMessage("Loading Data ...");
            damz_log.setIndeterminate(false);
            damz_log.setCancelable(false);
            damz_log.show();

        }

        @
                Override
        protected String doInBackground(String...arg0) {
            JSONParser jParser = new JSONParser();
            //JSONObject json = jParser.ambilURL(url_lihat_nama_barang);
            JSONObject json = jParser.ambilURL(url_lihat_nama_barang + "?username=" + s_user+"&kode_barang="+edt_kode_barang.getText().toString());
            try {
                contacts = json.getJSONArray("INVENTORY");
                str1 = new String[contacts.length()];
                for (int aai = 0; aai < contacts.length(); aai++) {
                    JSONObject c = contacts.getJSONObject(aai);
                    str1[aai] = c.getString("nama_barang");
                }
            } catch (JSONException e) {

            }
            return null;
        }
        @
                Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            damz_log.dismiss();
            for (int i = 0; i < str1.length; i++) {
                list.add(str1[i]);
            }
            Collections.sort(list);
            ArrayAdapter< String > dataAdapter = new ArrayAdapter < String >
                    (getApplicationContext(), android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            edt_kode_barang.setThreshold(1);
            edt_kode_barang.setAdapter(dataAdapter);
        }

    }
}