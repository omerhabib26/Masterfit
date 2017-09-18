package www.tecjaunt.com.masterfit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 105;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(SplashScreen.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
        }else {
            callThread();
        }
//        callThread();
    }


    private void callThread(){


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2500);
                    Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callThread();
            } else {
                ActivityCompat.requestPermissions(SplashScreen.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
            }
        }

    }

}
