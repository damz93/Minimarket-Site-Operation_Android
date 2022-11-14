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

import com.google.zxing.integration.android.IntentIntegrator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import app.miso.bantuan.Act_set_get;
import app.miso.bantuan.ConnectionDetector;
import app.miso.bantuan.JSONP2;
import app.miso.bantuan.JSONParser;
import app.miso.bantuan.RequestHandler;

public class Act_stok_opn2 extends Activity implements View.OnClickListener {
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
    String s_user,kode_stok,cek,TAG;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button mCaptureBtn;
    ImageView img_view_btn, img_view;
    boolean check = true;
    private static final String TAG_SUCCESS = "success";
    int success;
    private static final String TAG_MESSAGE = "message";
    TextView t_user2;
    private static String url_refresh_stok_opm = "http://minisiteoperation.com/json/all-stok-opname.php";
    private static String url_simpan_stok_opm =  "http://minisiteoperation.com/json/save-stok.php";
    private static String url_simpan_foto_stok_opm =  "http://minisiteoperation.com/json/save-img-stok.php";
    Uri image_uri;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lay_stok_opm2);
        lv = (ListView) findViewById(R.id.list_inventory_log20);
        btn_back = findViewById(R.id.btn_back20);
        btn_save = findViewById(R.id.btn_save20);

        btn_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        t_user2 = findViewById(R.id.tx_user20);
        Act_set_get stg = new Act_set_get();
        String level = stg.getLevel();
        String nama = stg.getusnme();
        String semua = nama+"("+level+")";
        t_user2.setText(semua);
        s_user = nama;

        ln_detail2 = findViewById(R.id.ln_detail20);
        ln_detail2.setVisibility(View.INVISIBLE);

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        if (isInternetPresent) {
            new refresh_tabel2().execute();
        }
        else{
            inet_mati();
        }
        img_view_btn = findViewById(R.id.im_kamera20);
        img_view = findViewById(R.id.im_kameraS20);
        img_view_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //if system os is >= marshmallow, request runtime permission
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
                        openCamera();

                        /*Intent intent = new Intent();

                        intent.setType("image/*");

                        intent.setAction(Intent.ACTION_GET_CONTENT);

                        startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);*/
                    }
                }
                else {
                    //system os < marshmallow
                    openCamera();
                  /*  Intent intent = new Intent();

                    intent.setType("image/*");

                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);*/

                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v == btn_back){
            Intent peminjaman = new Intent(Act_stok_opn2.this, Act_stok_opn.class);
            finish();
            startActivity(peminjaman);
        }
        else if(v == btn_save){
            try {
                if (null != img_view.getDrawable()) {
                    new simpan_2().execute();
                }
                else{
                    Toast.makeText(Act_stok_opn2.this,"Take a Picture First, press the camera button above",Toast.LENGTH_LONG).show();
                }
                //ImageUploadToServerFunction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void openCamera() {
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
                    openCamera();

                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //called when image was captured from camera

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
    /*
    @Override
    protected void onActivityResult(int RC, int RQC, Intent I) {
        super.onActivityResult(RC, RQC, I);

        if (RC == 1 && RQC == RESULT_OK && I != null && I.getData() != null) {

            Uri uri = I.getData();

            try {

                bitmapz2 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                img_view.setImageBitmap(bitmapz2);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        else{
            /*
          //  Uri uri = I.getData();
            File photoFile = null;
            String url_foto;
            url_foto = mCurrentPhotoPath;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                int targetH = img_view.getHeight() * 2;
                int targetW = img_view.getWidth() * 2;
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


            try {
                bitmapz2 = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            img_view.setImageBitmap(bitmapz2);

        }

    }
*/

    public void upd_gambar(Bitmap bitmap) throws Exception {
        /*MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        BitmapDrawable drawable = (BitmapDrawable) img_view.getDrawable();
        rotateBMP = drawable.getBitmap();*/
        new upload2().execute(bitmap);
        //   ImageUploadToServerFunction();
    }



    private class upload2 extends AsyncTask < Bitmap, Void, Void > {
        RequestHandler rh = new RequestHandler();
        String uploadImage;
        @
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn2.this);
            dd_dialog.setMessage("Saving Process.....");
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
                data.put("kode_stok", kode_stok);
                String result = rh.sendPostRequest(url_simpan_foto_stok_opm, data);
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
            Toast.makeText(getApplicationContext(), "Data Saved.....", Toast.LENGTH_LONG).show();
            finish();
            Act_set_get ab = new Act_set_get();
            ab.setkode_stok(kode_stok);
            Intent abc = new Intent(Act_stok_opn2.this,Act_print_pdf.class);
            startActivity(abc);
        }
    }
    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;

    }


    @SuppressLint("StaticFieldLeak")
    public class refresh_tabel2 extends AsyncTask < String, String, String > {
        ArrayList< HashMap < String, String >> contactList = new ArrayList < HashMap < String, String >> ();@
                Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd_dialog = new ProgressDialog(Act_stok_opn2.this);
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
                        keter =keter.substring(0, 20) + ".....";
                    }
                    else{
                        keter = keter;
                    }
                    map.put("NAMA_BARANGx", nam_barang);
                    map.put("QTYx", qty_brg);
                    map.put("NO_SERIx", keter);
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
                    R.layout.lay_stok_opm_list2, new String[] {"KODE_BARANGx", "NAMA_BARANGx", "QTYx", "QTYx2", "NO_SERIx"},
                    new int[] {R.id.tx_code20, R.id.tx_name20, R.id.tx_qty20, R.id.tx_qty202, R.id.tx_noseri202});
            lv.setAdapter(adapter2);
            lv.setVisibility(View.VISIBLE);
            ln_detail2.setVisibility(View.VISIBLE);
            //   jumlah_data = i;
            //   Toast.makeText(Act_peminjaman.this,"Jumlah Data: "+jumlah_data.toString(),Toast.LENGTH_LONG).show();
        }
    }

    private void inet_mati() {
        AlertDialog dd_dialog = new AlertDialog.Builder(Act_stok_opn2.this).create();
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
        }

        @
                Override
        protected String doInBackground(String...arg0) {
            try {
                List<NameValuePair> params = new ArrayList< NameValuePair >();
                params.add((NameValuePair) new BasicNameValuePair("usname", s_user));
                url_simpan_stok_opm = url_simpan_stok_opm+ "?usname=" + s_user;
                JSONObject json = jParser.makeHttpRequest(url_simpan_stok_opm, "POST", params);
                success = json.getString("success");
                kode_stok = json.getString("get_kode");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errornya: " + e, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            kode_stok = kode_stok;
            if (success.equals("1")) {
                try {
                    upd_gambar(bitmapz2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {


            }
        }

    }
    public void onBackPressed() {
        dd_kembali();
    }
    public void dd_kembali(){
        Intent i = new Intent(Act_stok_opn2.this,Act_stok_opn.class);
        finish();
        startActivity(i);
    }

}