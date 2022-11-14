package app.miso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    /** Called when the activity is first created. */
    @
            Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        /* layout splashscreen dengan background gambar */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {@
                Override
        public void run() {
            Intent mainIntent = null;

            mainIntent = new Intent(MainActivity.this, Act_login.class);

            MainActivity.this.startActivity(mainIntent);
            MainActivity.this.finish();
        }
        }, SPLASH_DISPLAY_LENGTH);
    }

}