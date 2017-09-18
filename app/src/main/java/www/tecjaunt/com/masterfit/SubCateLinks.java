package www.tecjaunt.com.masterfit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static www.tecjaunt.com.masterfit.OCRScreen.pref;

public class SubCateLinks extends AppCompatActivity {

    ConnectionDetector cd;
    JSONArray jsonArray;
    List<Categories> list;
    CategoryAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_cate_links);

        cd=new ConnectionDetector(this);
        pref=new SharedPreference(this);

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
        adapter = new CategoryAdapter(list,1);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(cd.isConnectingToInternet()){
                    pref.setOpen_link(list.get(position).getUid());
                    startActivity(new Intent(getApplicationContext(), YoutubeDialogFragment.class));
                    //new YoutubeDialogFragment().show(getSupportFragmentManager(),"YoutubeDialog");
                }else{
                    Toast.makeText(SubCateLinks.this, "No Internet Connection Available", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
