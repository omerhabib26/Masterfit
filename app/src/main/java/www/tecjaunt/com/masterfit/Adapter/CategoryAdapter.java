package www.tecjaunt.com.masterfit.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import www.tecjaunt.com.masterfit.Models.Categories;
import www.tecjaunt.com.masterfit.R;

/**
 * Created by Omer Habib on 9/5/2017.
 */

public class CategoryAdapter extends BaseAdapter {

    List<Categories> list;
    int type=0;

    public CategoryAdapter(List<Categories> list, int type) {
        this.list = list;
        this.type=type;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            if(type==0) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_view, parent, false);
            }else{
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_link_view, parent, false);
            }
        }

        ImageView imageView=(ImageView) convertView.findViewById(R.id.imageview);


        String link=list.get(position).getImage();
        if(type ==0){
//            TextView textView1=(TextView) convertView.findViewById(R.id.textView1);
//            textView1.setText(list.get(position).getDescription());
            link="http://app.masterfitlife.com" +link;
        }else{
            TextView textView=(TextView) convertView.findViewById(R.id.textView);
            textView.setText(list.get(position).getTitle());
        }
        try {
            Log.d("Imageurl", list.get(position).getImage());

            Glide.with(convertView.getContext())
                    .load(link)
                    .centerCrop()
                    .into(imageView);

        }catch (Exception e){
            Log.d("Imageurl", e.getMessage());
        }


        return convertView;
    }
}
