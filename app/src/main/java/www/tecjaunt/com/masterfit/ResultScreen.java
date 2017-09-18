package www.tecjaunt.com.masterfit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import www.tecjaunt.com.masterfit.networkConnection.ConnectionDetector;

import static www.tecjaunt.com.masterfit.OCRScreen.pref;

public class ResultScreen extends AppCompatActivity {

    LinearLayout ll_op1,ll_op2,ll_op3;

    ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);

        findViewBy();
        cd=new ConnectionDetector(this);

        ll_op1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cd.isConnectingToInternet()) {
                    pref.setOpen_link(pref.getLink_01());
                    startActivity(new Intent(getApplicationContext(), YoutubeDialogFragment.class));
                }else{
                    Toast.makeText(ResultScreen.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ll_op2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cd.isConnectingToInternet()) {
                    pref.setOpen_link(pref.getLink_02());
                    startActivity(new Intent(getApplicationContext(), YoutubeDialogFragment.class));
                }else{
                    Toast.makeText(ResultScreen.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ll_op3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cd.isConnectingToInternet()) {
                    pref.setOpen_link(pref.getLink_03());
                    startActivity(new Intent(getApplicationContext(), YoutubeDialogFragment.class));
                }else{
                    Toast.makeText(ResultScreen.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findViewBy(){
        ll_op1=(LinearLayout)findViewById(R.id.ll_result_screen_op_01);
        ll_op2=(LinearLayout)findViewById(R.id.ll_result_screen_op_02);
        ll_op3=(LinearLayout)findViewById(R.id.ll_result_screen_op_03);

    }
}
