package www.tecjaunt.com.masterfit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import www.tecjaunt.com.masterfit.Adapter.CategoryAdapter;
import www.tecjaunt.com.masterfit.Models.Categories;
import www.tecjaunt.com.masterfit.networkConnection.ConnectionDetector;

public class MainCategories extends AppCompatActivity {


    ConnectionDetector cd;
    JSONArray jsonArray;
    List<Categories> list;
    CategoryAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_categories);

        cd=new ConnectionDetector(this);

        String result=getIntent().getExtras().getString("list",null);
        try {
            jsonArray=new JSONArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(result.length()>4){
            list=new Gson().fromJson(result, new TypeToken<List<Categories>>(){}.getType());
        }else{
            list=new ArrayList<>();
        }

        listView = (ListView)findViewById(R.id.recyclerView);
        adapter = new CategoryAdapter(list,0);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(cd.isConnectingToInternet()){
                    new www.tecjaunt.com.masterfit.actions.AsyncTask.Signup(MainCategories.this,"SubCate").execute("http://app.masterfitlife.com/index.php/api/Data_Storage/getallsubcat/?cid="+list.get(position).getUid());
                }else{
                    Toast.makeText(MainCategories.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
}
