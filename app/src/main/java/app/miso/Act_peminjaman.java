package app.miso;

import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Config;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.*;
import app.miso.bantuan.*;
import app.miso.bantuan.ConnectionDetector;
import app.miso.bantuan.JSONP2;
import app.miso.bantuan.JSONParser;
import app.miso.bantuan.RequestHandler;


import com.google.android.gms.common.api.Response;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class Act_peminjaman extends Activity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    String level[] = {
            "Assurance",
            "Konstruksi",
            "PSB"
    };
    String pilih_segment,pilih_warehouse;
    Spinner sp_segmen,sp_warehouse;
    ListView lv;
    final List< String > list = new ArrayList < String > ();
    final List< String > list_wh = new ArrayList < String > ();
    String str1,str_username;
    ProgressDialog damz_log,damz_log1;
    public static String url_cari_barang = "http://minisiteoperation.com/json/cari-barang2.php";
    public static String url_simpan_sementara = "http://minisiteoperation.com/json/save-log-inventory-temp.php";
    private static String url_refresh_log_inventory = "http://minisiteoperation.com/json/all-inventory-temp.php";
    private static String url_delete_satu_data =  "http://minisiteoperation.com/json/delete-inv-log-temp-detail.php";
    private static String url_delete_semua_data = "http://minisiteoperation.com/json/delete-inv-log-temp.php";
    private static String url_lihat_nama_barang  = "http://minisiteoperation.com/json/nama-barang2.php";
    private static String url_all_wh  = "http://minisiteoperation.com/json/all-warehouse.php";
    String s_warehouse,s_gi_number,s_kode_barang, s_nama_barang, s_qty, s_user, s_noseri, s_segment, s_notes;
    LinearLayout ln_detail;
    AutoCompleteTextView at_kode_barang;
    TextView tx_nama_barang,tx_qty_barang,tx_detail_barang;
    EditText edt_gi_number,edt_kode_barang,ed_qty_pinjam,ed_no_seri,ed_notes;
    Button btn_clear_sn,btn_brcode,btn_add_to,btn_kembali,btn_qrcode,btn_cari_kodb,btn_refresh,btn_clearall,btn_next,btn_clear_kode;
    String str_kode_barang;
    String str_kode_barang_del, str_username_del;
    Integer jumlah_data=0, juml_qty=0;
    TextView t_user;
    ConnectionDetector cd;
    String st_cek_br;
    Boolean isInternetPresent = false;
    JSONP2 jParser = new JSONP2();
    JSONArray contacts = null;
    ProgressDialog dd_dialog;
    Act_set_get abcc = new Act_set_get();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_peminjaman);
        lv = (ListView) findViewById(R.id.list_inventory_log);
        t_user = findViewById(R.id.tx_user3);

        sp_segmen = (Spinner) findViewById(R.id.spn_segment);
        sp_warehouse = (Spinner) findViewById(R.id.spn_warehouse);
        sp_warehouse.setOnItemSelectedListener(this);

        //ArrayAdapter < String > adapter = new ArrayAdapter < String > (this, android.R.layout.simple_spinner_dropdown_item, level);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.template_spinner_segm,level);
        sp_segmen.setAdapter(adapter);
        sp_segmen.setOnItemSelectedListener(this);

        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama+"("+level+")";
        t_user.setText(semua);
        s_user = nama;
        str_username_del = stg.getusnme();
        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            new tampil_spinner().execute();
            try{
                new refresh_tabel().execute();
            }catch (Exception ignored){

            }
        }
        else{
            inet_mati();
        }


        at_kode_barang = findViewById(R.id.edt_kode_barang2);
        tx_nama_barang = findViewById(R.id.txt_nama_barang);
        tx_qty_barang = findViewById(R.id.txt_qty_barang);
        tx_detail_barang = findViewById(R.id.txt_detail_barang);
        edt_kode_barang = findViewById(R.id.edt_kode_barang);
        ed_qty_pinjam = findViewById(R.id.edt_qty_pinjam);
        ed_no_seri = findViewById(R.id.edt_noseri);
        ed_notes = findViewById(R.id.edt_notes);
        edt_gi_number = findViewById(R.id.edt_gi_number);
        ln_detail = findViewById(R.id.ln_detail);
        ln_detail.setVisibility(View.INVISIBLE);

        btn_clear_kode = findViewById(R.id.btn_clear_kode);
        btn_add_to = findViewById(R.id.btn_add_to_list);
        btn_kembali = findViewById(R.id.btn_kembali2);
        btn_cari_kodb = findViewById(R.id.btn_cari_kode_barang);
        btn_next = findViewById(R.id.btn_next);
        btn_brcode = findViewById(R.id.btn_brcode);
        btn_clear_sn = findViewById(R.id.btn_clear_serial);
        btn_clear_kode.setOnClickListener(this);
        btn_add_to.setOnClickListener(this);
        btn_cari_kodb.setOnClickListener(this);
        btn_kembali.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_brcode.setOnClickListener(this);
        btn_brcode.setEnabled(false);
        btn_clear_sn.setOnClickListener(this);

        btn_qrcode = (Button) findViewById(R.id.btn_qr_code1);
        btn_qrcode.setOnClickListener(this);

        btn_refresh = (Button) findViewById(R.id.btn_refrs);
        btn_refresh.setOnClickListener(this);
        btn_clearall = (Button) findViewById(R.id.btn_clear);
        btn_clearall.setOnClickListener(this);
//        disable_comp();
        ed_qty_pinjam.setEnabled(false);
        ed_no_seri.setEnabled(false);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String kode_barang = ((TextView) view.findViewById(R.id.tx_code)).getText().toString();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(Act_peminjaman.this);
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
                String scanContent = scanningResult.getContents();
                String s = ed_no_seri.getText().toString();
                //String scanFormat = scanningResult.getFormatName();
                //ed_no_seri.setText("FORMAT: " + scanFormat);
                //ed_no_seri.setText("CONTENT: " + scanContent);
                juml_qty = juml_qty + 1;
                if (s.equals("")){
                    ed_no_seri.setText(scanContent);
                    ed_qty_pinjam.setText(juml_qty.toString());
                }
                else {
                    ed_no_seri.setText(s+" | "+scanContent+"");
                    ed_qty_pinjam.setText(juml_qty.toString());
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
                androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(Act_peminjaman.this).create();
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

                //edt_kode_barang.setText(result.getContents().toString());
                at_kode_barang.setText(result.getContents().toString());
                //str_kode_barang = edt_kode_barang.getText().toString();
                str_kode_barang = at_kode_barang.getText().toString();
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

        //s_kode_barang = edt_kode_barang.getText().toString();
        s_kode_barang = at_kode_barang.getText().toString();
        s_nama_barang = tx_nama_barang.getText().toString();
        s_qty = ed_qty_pinjam.getText().toString();
        s_noseri = ed_no_seri.getText().toString();
        s_segment = sp_segmen.getSelectedItem().toString();
        s_warehouse = sp_warehouse.getSelectedItem().toString();
        s_notes = ed_notes.getText().toString();
        s_gi_number = edt_gi_number.getText().toString();
        s_user = str_username;


        try {
            if (v== btn_next){
                if (jumlah_data>0) {
                    new refresh_tabel().execute();
                    finish();
                    Intent peminjaman2 = new Intent(Act_peminjaman.this, Act_peminjaman2.class);
                    startActivity(peminjaman2);
                }
                else{
                    AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
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
            else if(v == btn_clear_kode){
                at_kode_barang.setText("");
                at_kode_barang.requestFocus();
            }
            else if(v == btn_clear_sn){
                ed_no_seri.setText("");
                ed_qty_pinjam.setText("0");
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Act_peminjaman.this);
                builder.setMessage("are you sure you want to delete all item???").setPositiveButton("No", dd_dialog).setNegativeButton("Yes", dd_dialog).show();
            }
            else if (v == btn_add_to) {
                if ((ed_notes.length() > 0)&&(edt_gi_number.length() > 0)&&(ed_no_seri.length() > 0) &&(at_kode_barang.length() > 0) && (ed_qty_pinjam.length()>0)&&(tx_nama_barang.getText().toString()!=".....")) {
                        if (isInternetPresent) {
                            simpan1(s_warehouse,s_gi_number,s_notes,s_segment,s_noseri,s_kode_barang, s_nama_barang, s_qty, s_user);
                        }
                        else{
                            inet_mati();
                        }

                } else {
                    try {
                        field_kosong();
                    } catch (Exception e) {
                        Toast.makeText(Act_peminjaman.this, "Ini errornya: " + e, Toast.LENGTH_LONG).show();
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
                  //  str_kode_barang = edt_kode_barang.getText().toString();
                    str_kode_barang = at_kode_barang.getText().toString();
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
            Toast.makeText(Act_peminjaman.this, "Errornya: " + e, Toast.LENGTH_LONG).show();
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
        AlertDialog dyam_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
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
        Intent i = new Intent(Act_peminjaman.this,Act_utama.class);
        finish();
        startActivity(i);
    }


    private void kosong() {
        btn_brcode.setEnabled(false);
        //edt_kode_barang.setText("");
        sp_segmen.setSelection(0);
        at_kode_barang.setText("");
        juml_qty=0;
        ed_notes.setText("");
        edt_gi_number.setText("");
        ed_qty_pinjam.setText("0");
        ed_no_seri.setText("-");
        tx_nama_barang.setText(".....");
        tx_qty_barang.setText("0");
        tx_detail_barang.setText("");
        at_kode_barang.requestFocus();
        //edt_kode_barang.requestFocus();
    }
    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
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
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
        dd_dialog.setTitle("Warning");
        dd_dialog.setIcon(R.drawable.warning);
        dd_dialog.setMessage("Item not found or has not been entered in the system...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
    private void scan_serial_number() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
        dd_dialog.setTitle("Info");
        dd_dialog.setIcon(R.drawable.info);
        dd_dialog.setMessage("Items found. Click Button BARCODE, to scan Serial Number...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
    private void input_qty() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_peminjaman.this).create();
        dd_dialog.setTitle("Info");
        dd_dialog.setIcon(R.drawable.info);
        dd_dialog.setMessage("Item name found. Input Qty...");
        dd_dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dd_dialog.show();
    }
    private void item_found() {
        Toast.makeText(Act_peminjaman.this,"Item name found......",Toast.LENGTH_LONG).show();
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

    @Override
    public void onItemSelected(AdapterView< ? > parent, View arg1, int arg2, long arg3) {
        pilih_segment = sp_segmen.getSelectedItem().toString();
        parent.getItemAtPosition(arg2);
        switch (parent.getId()) {
            case R.id.spn_level:
                if (pilih_segment.equals("Assurance")) {
                     Toast.makeText(getApplicationContext(), "Assurance",Toast.LENGTH_LONG).show();
                    s_segment = "Assurance";
                } else if (pilih_segment.equals("PSB")) {
                    Toast.makeText(getApplicationContext(), "PSB",Toast.LENGTH_LONG).show();
                    s_segment = "PSB";
                }else if (pilih_segment.equals("Konstruksi")) {
                    Toast.makeText(getApplicationContext(), "Konstruksi",Toast.LENGTH_LONG).show();
                    s_segment = "Konstruksi";
                }
            case R.id.spn_warehouse:
                pilih_warehouse = sp_warehouse.getSelectedItem().toString();
                pilih_warehouse = pilih_warehouse.replace(' ','+');
                new AmbilNamaBarang().execute();
                kosong();

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class cari_kode_barang extends AsyncTask < String, String, String > {

        String aaz,nam_bar,qty,keter, spesifikasi;
        int countz = 0;
        String[] str1;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_peminjaman.this);
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
            //JSONObject json = jParser.ambilURL(url_cari_barang+"kode_barang="+edt_kode_barang.getText().toString());
            //JSONObject json = jParser.ambilURL(url_cari_barang + "?kode_barang=" + str_kode_barang+"&username="+s_user);
            JSONObject json = jParser.ambilURL(url_cari_barang + "?kode_barang="+str_kode_barang+"&warehouse=" + pilih_warehouse);
            JSONObject json1 = null;
            try {
                contacts = json.getJSONArray("NAMA");
                str1 = new String[contacts.length()];
                int aai = 0;
                //for (int aai = 0; aai < contacts.length(); aai++) {
                    JSONObject c = contacts.getJSONObject(aai);

                    json1 = contacts.getJSONObject(aai);
                    nam_bar = c.getString("nama_barang");
                    qty = c.getString("qty");
                    keter = c.getString("keter");
                    spesifikasi = c.getString("spesf");
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
                tx_qty_barang.setText(qty.toString());
                tx_detail_barang.setText(spesifikasi.toString());

                //item_found();
                if (keter.equals("NO SERI")){
                    ed_qty_pinjam.setText("0");
                    //ed_no_seri.setText("-");
                    ed_qty_pinjam.setEnabled(false);
                    ed_no_seri.setEnabled(true);
                    btn_brcode.setEnabled(true);
                    ed_no_seri.requestFocus();
                    ed_no_seri.setText("");
                    //Toast.makeText(Act_peminjaman.this,"Input Serial Number...",Toast.LENGTH_LONG).show();
                    scan_serial_number();
                    //btn_brcode.setOnClickListener((View.OnClickListener) this);

                }
                else{
                    ed_no_seri.setText("-");
                    ed_qty_pinjam.setText("1");
                    ed_no_seri.setEnabled(false);
                    btn_brcode.setEnabled(false);
                    ed_qty_pinjam.setEnabled(true);
                    ed_qty_pinjam.requestFocus();
                    input_qty();
                }
            }
            catch (Exception e){
                item_not_found();
                //edt_kode_barang.requestFocus();
                at_kode_barang.requestFocus();
                tx_nama_barang.setText(".....");
                tx_qty_barang.setText("0");
                tx_detail_barang.setText("-");
            }
        }

    }
    private void simpan1(String s_warehousez,String s_gi_numberz,String s_segmentz,String s_notesz,String s_noseriz,String s_kode_barangz,String s_nama_barangz,String s_qtyz,String s_userz) {
        class SendPostReqAsyncTask extends AsyncTask < String, Void, String > {
            @
                    Override
            protected void onPreExecute() {
                super.onPreExecute();
                dd_dialog = new ProgressDialog(Act_peminjaman.this);
                dd_dialog.setMessage("Saving process.....");
                dd_dialog.setIndeterminate(false);
                dd_dialog.show();
            }
            @
                    Override
            protected String doInBackground(String...params) {
                String s_warehousey = params[0];
                String s_gi_numbery = params[1];
                String s_segmenty = params[2];
                String s_notesy = params[3];
                String s_noseriy = params[4];
                String s_kode_barangy = params[5];
                String s_nama_barangy = params[6];
                String s_qtyy = params[7];
                String s_usery = params[8];
                List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > ();
                nameValuePairs.add(new BasicNameValuePair("warehouse", s_warehousey));
                nameValuePairs.add(new BasicNameValuePair("gi_number", s_gi_numbery));
                nameValuePairs.add(new BasicNameValuePair("segment", s_segmenty));
                nameValuePairs.add(new BasicNameValuePair("notes_segment", s_notesy));
                nameValuePairs.add(new BasicNameValuePair("no_seri", s_noseriy));
                nameValuePairs.add(new BasicNameValuePair("kode_barang", s_kode_barangy));
                nameValuePairs.add(new BasicNameValuePair("nama_barang", s_nama_barangy));
                nameValuePairs.add(new BasicNameValuePair("qty", s_qtyy));
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
                Toast.makeText(Act_peminjaman.this, "Saved data", Toast.LENGTH_LONG).show();
                new refresh_tabel().execute();
                kosong();
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(s_warehouse,s_gi_number,s_segment,s_notes,s_noseri,s_kode_barang, s_nama_barang, s_qty, s_user);
    }



    public class refresh_tabel extends AsyncTask < String, String, String > {
        ArrayList< HashMap < String, String >> contactList = new ArrayList < HashMap < String, String >> ();@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_peminjaman.this);
            dd_dialog.setMessage("Loading Data Log Inventory ...");
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
            JSONObject json = jParser.ambilURL(url_refresh_log_inventory + "?username="+s_user);
            try {
                contacts = json.getJSONArray("LOG_INVENTORY");
                for (i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    HashMap< String, String > map = new HashMap < String, String > ();
                    String kod_barang = c.getString("kdbr").trim();
                    String nam_barang = c.getString("nmbr").trim();
                    String nom_ser = c.getString("noser").trim();
                    if (nam_barang.length()>12) {
                        nam_barang = nam_barang.substring(0, 12) + ".....";
                    }
                    else{
                        nam_barang = nam_barang;
                    }
                    if (nom_ser.length()>20) {
                        nom_ser = nom_ser.substring(0, 20) + ".....";
                    }
                    else{
                        nom_ser = nom_ser;
                    }
                    String qty_brg = c.getString("qtybr").trim();

                    map.put("KODE_BARANGx", kod_barang);
                    map.put("NAMA_BARANGx", nam_barang);
                    map.put("QTYx", qty_brg);
                    map.put("NO_SERIx", nom_ser);
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
                    R.layout.lay_peminjaman_list, new String[] {"KODE_BARANGx", "NAMA_BARANGx", "QTYx", "NO_SERIx"},
                    new int[] {R.id.tx_code, R.id.tx_name, R.id.tx_qty, R.id.tx_serialnumb});
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
            dd_dialog = new ProgressDialog(Act_peminjaman.this);
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
            dd_dialog = new ProgressDialog(Act_peminjaman.this);
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
                //   Toast.makeText(getApplicationContext(), "Password Gagal diupdate", Toast.LENGTH_LONG).show();

            }
        }
    }

    public class AmbilNamaBarang extends AsyncTask< String, String, String > {
        ArrayList<HashMap< String,
                String >> contactList = new ArrayList < HashMap < String, String >> ();
        HashMap < String, String > map = new HashMap < String, String > ();
        String aaz;
        int countz = 0;
        String[] str1;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();

            damz_log = new ProgressDialog(Act_peminjaman.this);
            damz_log.setMessage("Loading Data Items...");
            damz_log.setIndeterminate(false);
            damz_log.setCancelable(false);
            damz_log.show();

        }

        @
                Override
        protected String doInBackground(String...arg0) {
            JSONParser jParser = new JSONParser();
            //JSONObject json = jParser.ambilURL(url_lihat_nama_barang);
           //JSONObject json = jParser.ambilURL(url_lihat_nama_barang + "?username=" + s_user+"&kode_barang="+edt_kode_barang.getText().toString());
           JSONObject json = jParser.ambilURL(url_lihat_nama_barang + "?warehouse="+pilih_warehouse);
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
            at_kode_barang.setThreshold(1);
            at_kode_barang.setAdapter(dataAdapter);
        }

    }

    public class tampil_spinner extends AsyncTask< String, String, String > {
        ArrayList<HashMap< String,
                String >> contactList = new ArrayList < HashMap < String, String >> ();
        HashMap < String, String > map = new HashMap < String, String > ();
        String aaz;
        int countz = 0;
        String[] str1;@
                Override
        protected void onPreExecute() {
            super.onPreExecute();

            damz_log1 = new ProgressDialog(Act_peminjaman.this);
            damz_log1.setMessage("Loading Data Warehouse...");
            damz_log1.setIndeterminate(false);
            damz_log1.setCancelable(false);
            damz_log1.show();

        }

        @
                Override
        protected String doInBackground(String...arg0) {
            JSONParser jParser = new JSONParser();
            @SuppressLint("WrongThread") JSONObject json = jParser.ambilURL(url_all_wh);
            try {
                contacts = json.getJSONArray("ALL_WH");
                str1 = new String[contacts.length()];
                for (int aai = 0; aai < contacts.length(); aai++) {
                    JSONObject c = contacts.getJSONObject(aai);
                    str1[aai] = c.getString("regional");
                }
            } catch (JSONException e) {

            }
            return null;
        }
        @
                Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            damz_log1.dismiss();
            for (int i = 0; i < str1.length; i++) {
                list_wh.add(str1[i]);
            }
            Collections.sort(list_wh);
            ArrayAdapter< String > dataAdapter = new ArrayAdapter < String >
                    (getApplicationContext(), android.R.layout.simple_spinner_item, list_wh);
            //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_warehouse.setAdapter(dataAdapter);
            pilih_warehouse = sp_warehouse.getSelectedItem().toString();


        }

    }
    private void disable_comp(){
        btn_qrcode.setEnabled(false);
        //edt_kode_barang.setEnabled(false);
        btn_cari_kodb.setEnabled(false);
        btn_clear_kode.setEnabled(false);
        btn_clear_sn.setEnabled(false);
        btn_clearall.setEnabled(false);
        btn_brcode.setEnabled(false);
        btn_next.setEnabled(false);
        btn_add_to.setEnabled(false);
        btn_refresh.setEnabled(false);
        ed_qty_pinjam.setEnabled(false);
        ed_no_seri.setEnabled(false);
        ed_notes.setEnabled(false);
    }
    private void enable_comp(){
        btn_qrcode.setEnabled(true);
        edt_kode_barang.setEnabled(true);
        btn_cari_kodb.setEnabled(true);
        btn_clear_kode.setEnabled(true);
        btn_clear_sn.setEnabled(true);
        btn_clearall.setEnabled(true);
        btn_brcode.setEnabled(true);
        btn_next.setEnabled(true);
        btn_add_to.setEnabled(true);
        btn_refresh.setEnabled(true);
        ed_qty_pinjam.setEnabled(true);
        ed_no_seri.setEnabled(true);
        ed_notes.setEnabled(true);
    }

}