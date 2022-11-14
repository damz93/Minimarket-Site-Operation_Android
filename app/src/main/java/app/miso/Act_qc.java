package app.miso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

public class Act_qc extends Activity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    String fisik1[] = {
            "ONT",
            "STB",
            "ADD ON"
    };
    String adatidak[] = {
            "Ada",
            "Tidak Ada"
    };
    Spinner spn1, spn2, spn3, spn4,spn_remote;
    ImageView img_view_btn, img_view;
    EditText edt_sernumber;
    TextView t_user2;
    private static String url_refresh_qc = "http://minisiteoperation.com/json/all-qc-temp.php";
    private static String url_delete_satu_data = "http://minisiteoperation.com/json/delete-qc-temp-detail.php";
    private static String url_delete_semua_data = "http://minisiteoperation.com/json/delete-qc-temp.php";


    public static String url_simpan_sementara = "http://minisiteoperation.com/json/save-qc-temp.php";
    public static String url_cek_qc = "http://minisiteoperation.com/json/cek-kode-qc.php";
    private static String url_simpan_foto_qc = "http://minisiteoperation.com/json/save-img-qc.php";
    Uri image_uri;
    Integer jumlah_data=0;
    String TAG,st_cek_br,bt_qrcodestr_serial_number_del, s_user, str_username_del, str_username;
    ListView lv;
    ConnectionDetector cd;
    Boolean isInternetPresent = false;
    JSONP2 jParser = new JSONP2();
    Bitmap rotateBMP, bitmapz2;
    TextView t_user;
    String selectedImagePath;
    Button bt_qrcode, bt_clearqr, bt_next, bt_hapussemua, bt_refresh, btn_backto, btn_addto;
    String str_serial_number_del,str_sernum, str_fis1, str_adaptor, str_str_patch, str_rj, str_remote;
    String mCurrentPhotoPath,kode_qc;
    LinearLayout ln_detail;
    public static final String UPLOAD_KEY = "image";
    JSONArray contacts = null;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    ProgressDialog dd_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_qc);

        lv = (ListView) findViewById(R.id.list_qc);
        spn1 = (Spinner) findViewById(R.id.spn_fisik1);
        spn2 = (Spinner) findViewById(R.id.spn_fisik2);
        spn3 = (Spinner) findViewById(R.id.spn_fisik3);
        spn4 = (Spinner) findViewById(R.id.spn_fisik4);
        spn_remote = (Spinner) findViewById(R.id.spn_remote);
        t_user = (TextView) findViewById(R.id.tx_userqc);
        ln_detail = findViewById(R.id.ln_detailqc);
        ln_detail.setVisibility(View.INVISIBLE);
        edt_sernumber = findViewById(R.id.edt_noseriqc);
        bt_qrcode = findViewById(R.id.btn_brcodeqc);
        bt_clearqr = findViewById(R.id.btn_clear_brcodeqc);
        bt_next = findViewById(R.id.btn_next_qc);
        bt_hapussemua = findViewById(R.id.btn_clearallqc);
        bt_refresh = findViewById(R.id.btn_refreshqc);
        btn_backto = findViewById(R.id.btn_kembaliqc);
        btn_addto = findViewById(R.id.btn_add_to_listqc);

        bt_qrcode.setOnClickListener(this);
        bt_clearqr.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_hapussemua.setOnClickListener(this);
        bt_refresh.setOnClickListener(this);
        btn_backto.setOnClickListener(this);
        btn_addto.setOnClickListener(this);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.template_spinner_segm, fisik1);
        spn1.setAdapter(adapter);
        spn1.setOnItemSelectedListener(this);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.template_spinner_segm, adatidak);
        spn2.setAdapter(adapter2);
        spn2.setOnItemSelectedListener(this);
        spn3.setAdapter(adapter2);
        spn3.setOnItemSelectedListener(this);
        spn4.setAdapter(adapter2);
        spn4.setOnItemSelectedListener(this);
        spn_remote.setAdapter(adapter2);
        spn_remote.setOnItemSelectedListener(this);

        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama + "(" + level + ")";
        t_user.setText(semua);
        s_user = nama;
        str_username_del = stg.getusnme();
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            new refresh_tabel().execute();
        } else {
            inet_mati();
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String serial_number = ((TextView) view.findViewById(R.id.tx_snumber)).getText().toString();

                //Toast.makeText(Act_peminjaman.this, "Kode Barang: "+kode_barang,Toast.LENGTH_LONG).show();
                DialogInterface.OnClickListener dd_dialog = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                str_serial_number_del = serial_number;
                                //Toast.makeText(Act_peminjaman.this, str_kode_barang_del+s_user,Toast.LENGTH_LONG).show();
                                new hapus_satu_item().execute();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Act_qc.this);
                builder.setMessage("are you sure you want to delete the item, with the item code *" + serial_number + "*???").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
            }
        });
        img_view_btn = findViewById(R.id.im_kameraqc);
        img_view = findViewById(R.id.im_kameravqc);
        img_view.setVisibility(View.INVISIBLE);

        img_view_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED){
                        //permission not enabled, request it
                        String[] permission = {android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        //permission already granted
                        st_cek_br = "non";
                        openCamera();
                    }
                }
                else {
                    st_cek_br = "non";
                    openCamera();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        //str1 = edt_psbaru.getText().toString();
        Act_set_get a = new Act_set_get();
        str_username = a.getusnme();
        s_user = str_username;

        str_sernum = edt_sernumber.getText().toString();
        str_fis1 = spn1.getSelectedItem().toString();
        str_adaptor = spn2.getSelectedItem().toString();
        str_remote = spn_remote.getSelectedItem().toString();
        str_str_patch = spn3.getSelectedItem().toString();
        str_rj = spn4.getSelectedItem().toString();
        if (v == btn_addto) {
            img_view.setVisibility(View.VISIBLE);
            if ((edt_sernumber.length() > 0) && (str_fis1.length() > 0) && (str_adaptor.length() > 0) && (str_str_patch.length() > 0) && (str_rj.length() > 0)) {

                if (isInternetPresent) {
                    if (null != img_view.getDrawable()) {


                        simpan1(str_sernum, str_fis1, str_adaptor,str_remote, str_str_patch, str_rj, s_user);

                        //Toast.makeText(Act_peminjaman2.this,"Adaji isi gambarnya",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(Act_qc.this,"Take a Picture First, press the camera button above",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    inet_mati();
                }
            }
            else {
                try {
                    field_kosong();
                } catch (Exception e) {
                    Toast.makeText(Act_qc.this, "Ini errornya: " + e, Toast.LENGTH_LONG).show();
                }
            }
        } else if (v == btn_backto) {
            onBackPressed();
        }
        else if (v == bt_qrcode) {
            st_cek_br = "brcode";
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }

        else if(v == bt_refresh){
            if (isInternetPresent) {
                kosong();
                new refresh_tabel().execute();
            }
            else{
                inet_mati();
            }

        }
        else if(v == bt_clearqr){
            edt_sernumber.setText("");
            edt_sernumber.requestFocus();
        }
        else if (v == bt_hapussemua){
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Act_qc.this);
            builder.setMessage("are you sure you want to delete all item???").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
        }
        else if (v== bt_next){
            if (jumlah_data>0) {
                new refresh_tabel().execute();
                finish();
                //masih perlu diubah
                Intent peminjaman2 = new Intent(Act_qc.this, Act_qc2.class);
                startActivity(peminjaman2);
            }
            else{
                AlertDialog dd_dialog = new AlertDialog.Builder(Act_qc.this).create();
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
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (st_cek_br == "brcode") {
            if (scanningResult != null) {
                String scanContent = scanningResult.getContents();
                String s = edt_sernumber.getText().toString();
                if (s.equals("")){
                    edt_sernumber.setText(scanContent);
                    st_cek_br = "non";
                }
                else {
                    edt_sernumber.setText(s+" | "+scanContent+"");
                    st_cek_br = "non";
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                // if (requestCode == 1 && resultCode == RESULT_OK) {

                try {
                    bitmapz2 = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                img_view.setImageBitmap(bitmapz2);

                //    Uri imageUri = data.getData();
                // rotateBMP = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);


            }
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_qc.this).create();
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

    public class refresh_tabel extends AsyncTask< String, String, String > {
        ArrayList<HashMap< String, String >> contactList = new ArrayList < HashMap < String, String >> ();@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc.this);
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
                    R.layout.lay_qc_list, new String[] {"serial_numberx", "fisik1x", "adaptorx", "remotex", "patchcorex", "rj45x"},
                    new int[] {R.id.tx_snumber, R.id.tx_fisik1, R.id.tx_fisik2, R.id.tx_remote, R.id.tx_fisik3,R.id.tx_fisik4});
            lv.setAdapter(adapter2);
            lv.setVisibility(View.VISIBLE);
            ln_detail.setVisibility(View.VISIBLE);
            //   jumlah_data = i;
            //   Toast.makeText(Act_peminjaman.this,"Jumlah Data: "+jumlah_data.toString(),Toast.LENGTH_LONG).show();
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
    public void kosong() {
        edt_sernumber.setText("");
        img_view.setImageDrawable(null);
        img_view.setVisibility(View.INVISIBLE);
        spn1.setSelection(0);
        spn2.setSelection(0);
        spn3.setSelection(0);
        spn4.setSelection(0);
        spn_remote.setSelection(0);
    }

    public void dd_kembali(){
        Intent i = new Intent(Act_qc.this,Act_utama.class);
        finish();
        startActivity(i);
    }
    public class hapus_semua_item extends AsyncTask < String, String, String > {
        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc.this);
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


    public class hapus_satu_item extends AsyncTask < String, String, String > {
        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc.this);
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
                params.add((NameValuePair) new BasicNameValuePair("serial_number", str_serial_number_del));
                url_delete_satu_data = url_delete_satu_data+ "?usname=" + str_username_del+ "&serial_number=" + str_serial_number_del;
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
                Toast.makeText(getApplicationContext(), "Successfully Delete.....", Toast.LENGTH_LONG).show();
                // Toast.makeText(getApplicationContext(), url_delete_satu_data, Toast.LENGTH_LONG).show();
                kosong();
                new refresh_tabel().execute();
            } else {
                //   Toast.makeText(getApplicationContext(), "Password Gagal diupdate", Toast.LENGTH_LONG).show();

            }
        }
    }
    private void simpan1(String str_sernumz,String str_fis1z,String str_adaptorz,String str_remotez,String str_str_patchz,String str_rjz,String s_userz) {
        class SendPostReqAsyncTask extends AsyncTask < String, Void, String > {
            @
                    Override
            protected void onPreExecute() {
                super.onPreExecute();
                dd_dialog = new ProgressDialog(Act_qc.this);
                dd_dialog.setMessage("Saving data.....");
                dd_dialog.setIndeterminate(false);
                dd_dialog.show();
            }
            @
                    Override
            protected String doInBackground(String...params) {
                String str_sernumy = params[0];
                String str_fis1y = params[1];
                String str_adaptory = params[2];
                String str_remotey = params[3];
                String str_str_patchy = params[4];
                String str_rjy = params[5];
                String s_usery = params[6];
                List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > ();
                nameValuePairs.add(new BasicNameValuePair("str_sernum", str_sernumy));
                nameValuePairs.add(new BasicNameValuePair("str_fis1", str_fis1y));
                nameValuePairs.add(new BasicNameValuePair("str_adaptor", str_adaptory));
                nameValuePairs.add(new BasicNameValuePair("str_remote", str_remotey));
                nameValuePairs.add(new BasicNameValuePair("str_str_patch", str_str_patchy));
                nameValuePairs.add(new BasicNameValuePair("str_rj", str_rjy));
                nameValuePairs.add(new BasicNameValuePair("s_user", s_usery));
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
                Toast.makeText(Act_qc.this, "Saved data", Toast.LENGTH_LONG).show();
                new cek_qc().execute();


            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(str_sernum,str_fis1,str_adaptor,str_remote,str_str_patch, str_rj, s_user);
    }
        private void field_kosong(){
            AlertDialog dyam_dialog = new AlertDialog.Builder(Act_qc.this).create();
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


    private class upload2 extends AsyncTask < Bitmap, Void, Void > {
        RequestHandler rh = new RequestHandler();
        String uploadImage;
        @
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_qc.this);
            dd_dialog.setMessage("Uploading Photo.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.show();
        }

        @
                Override
        protected Void doInBackground(Bitmap...paramss) {
            if (paramss[0] == null)
                return null;
            setProgress(0);
            Bitmap bitmapaaa = paramss[0];
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //  bitmapaaa.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            InputStream in = new ByteArrayInputStream(stream.toByteArray());
            try {
                uploadImage = getStringImage(bitmapaaa);
                HashMap < String, String > data = new HashMap < String, String > ();
                data.put("imagez", uploadImage);
                data.put("kode_qc", kode_qc);
                data.put("sn",edt_sernumber.getText().toString());
                String result = rh.sendPostRequest(url_simpan_foto_qc, data);
            } finally {

            }


            if ( in != null) {
                try { in .close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @
                Override
        protected void onProgressUpdate(Void...values) {
            super.onProgressUpdate(values);
        }

        @
                Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dd_dialog.dismiss();
            //  if (result.equals("Successfully Uploaded")){
            new refresh_tabel().execute();
            kosong();
            Toast.makeText(getApplicationContext(), "Uploaded photos success..", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory() + "/log-qc";
        File dir = new File(storageDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File image = new File(storageDir + "/" + imageFileName + ".jpg");
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i(TAG, "photo path = " + mCurrentPhotoPath);
        return image;
    }


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;

    }

    @
            SuppressLint("ShowToast")
    private void setpic() throws IOException {
        int targetH = img_view.getHeight() * 2;
        int targetW = img_view.getWidth() * 2;
        String url_foto;
     /*   if (cek.equals("galery")) {
            url_foto = selectedImagePath;
            if (url_foto == null) {
                Toast.makeText(getApplicationContext(), "URL FOTONYA NULL", Toast.LENGTH_LONG);
                url_foto = "/storage/emulated/0/DCIM/Facebook/FB_IMG_1442847270929.jpg";
            } else {
                Toast.makeText(getApplicationContext(), "URL : " + url_foto, Toast.LENGTH_LONG);
                Log.d("url_foto", url_foto);
            }
        } else {
            url_foto = mCurrentPhotoPath;
        }*/
        url_foto = mCurrentPhotoPath;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url_foto, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor << 1;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(url_foto, bmOptions);
        Matrix mtx = new Matrix();
        ExifInterface ei = new ExifInterface(url_foto);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                mtx.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                mtx.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                mtx.postRotate(270);
                break;
        }
        rotateBMP = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, true);
        img_view.setImageBitmap(rotateBMP);

    }
    public class cek_qc extends AsyncTask < String, String, String > {

        String success;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  dd_dialog = new ProgressDialog(Act_peminjaman2.this);
            dd_dialog.setMessage("Saving Process.....");
            dd_dialog.setIndeterminate(false);
            dd_dialog.show();*/
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", s_user));
                url_cek_qc= url_cek_qc+ "?usname=" + s_user;
                JSONObject json = jParser.makeHttpRequest(url_cek_qc, "POST", params);
                success = json.getString("success");
                kode_qc = json.getString("get_qc");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            kode_qc = kode_qc;
            if (success.equals("1")) {
                try {
                    Act_set_get abc = new Act_set_get();
                    abc.setkode_qc(kode_qc);
                    upd_gambar(bitmapz2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    upd_gambar(bitmapz2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
    public void upd_gambar(Bitmap bitmap) throws Exception {
        /*MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        BitmapDrawable drawable = (BitmapDrawable) img_view.getDrawable();
        rotateBMP = drawable.getBitmap();*/
        new upload2().execute(bitmap);
        //   ImageUploadToServerFunction();
    }

    private void openCamera() {
        img_view.setVisibility(View.VISIBLE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }
    //handling permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    //openCamera();



                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}