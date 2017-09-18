package www.tecjaunt.com.masterfit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import www.tecjaunt.com.masterfit.networkConnection.ConnectionDetector;

public class HomeScreen extends AppCompatActivity {


//    private static final String LINK_TAG = "http://iditch.com/IDitch/services/login.php";
    TextView tv_op1, tv_op2, tv_op3, tv_op4;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        cd=new ConnectionDetector(this);
        findViewBy();

        tv_op1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),OCRScreen.class));
            }
        });
        
        tv_op2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cd.isConnectingToInternet()){
                    new www.tecjaunt.com.masterfit.actions.AsyncTask.Signup(HomeScreen.this,"MainCate").execute("http://app.masterfitlife.com/index.php/api/Data_Storage/getAllCat/");
                }else{
                    Toast.makeText(HomeScreen.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_op3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreen.this, "This Feature is for future use\n Please hold and wait\nSomething new is coming", Toast.LENGTH_SHORT).show();
            }
        });

        tv_op4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreen.this, "This Feature is for future use\n Please hold and wait\nSomething new is coming", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void findViewBy(){
        tv_op1=(TextView)findViewById(R.id.tv_option_01);
        tv_op2=(TextView)findViewById(R.id.tv_option_02);
        tv_op3=(TextView)findViewById(R.id.tv_option_03);
        tv_op4=(TextView)findViewById(R.id.tv_option_04);

    }

}
