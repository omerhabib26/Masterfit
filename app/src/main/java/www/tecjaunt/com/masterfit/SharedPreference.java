package www.tecjaunt.com.masterfit;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Omer Habib on 5/31/2017.
 */

public class SharedPreference {


    private static final String PREF_NAME = "MasterFitLife_SP";
    SharedPreferences pref;
    SharedPreferences.Editor editor ;
    Context context;
    int PRIVATE_MODE = 0;

    String link_01=null;
    String link_02=null;
    String link_03=null;
    String open_link=null;

    public SharedPreference(Context ctx) {
        this.context=ctx;
        pref=context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor=pref.edit();
    }

    public void clearData(){
        editor.putString("link_01",null);
        editor.putString("link_02",null);
        editor.putString("link_03",null);
        editor.putString("open_link",null).commit();
    }

    public String getLink_01() {
        return pref.getString("link_01",link_01);
    }

    public void setLink_01(String link_01) {
        editor.putString("link_01",link_01)
                .commit();
    }

    public String getLink_02() {
        return pref.getString("link_02",link_02);
    }

    public void setLink_02(String link_02) {
        editor.putString("link_02",link_02)
                .commit();
    }

    public String getLink_03() {
        return pref.getString("link_03",link_03);
    }

    public void setLink_03(String link_03) {
        editor.putString("link_03",link_03)
                .commit();
    }

    public String getOpen_link() {
        return pref.getString("open_link",open_link);
    }

    public void setOpen_link(String open_link) {
        editor.putString("open_link",open_link)
                .commit();
    }
}
