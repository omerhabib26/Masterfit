package www.tecjaunt.com.masterfit.actions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import www.tecjaunt.com.masterfit.MainCategories;
import www.tecjaunt.com.masterfit.OCRScreen;
import www.tecjaunt.com.masterfit.SubCateLinks;
import www.tecjaunt.com.masterfit.SubCategories;
import www.tecjaunt.com.masterfit.networkConnection.GetDataFromWebservice;

import static www.tecjaunt.com.masterfit.OCRScreen.parseJSON;


/**
 * Created by Omer Habib on 5/20/2017.
 */

public class AsyncTask {

    public static class Signup extends android.os.AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        Context context;
        String call_from;

        public Signup(Context context,String call_from) {

            this.context=context;
            this.call_from=call_from;
        }

        @Override
        protected void onPreExecute() {
            progressDialog=new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected String doInBackground(String... params) {
                return GetDataFromWebservice.GET(params[0]);
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            progressDialog.dismiss();
            Log.i("Testing HTTP Image", "POST\n" + result);
            try {
                JSONObject object = new JSONObject(result);
                if(call_from.equalsIgnoreCase("OCR")) {
                    String updateStatus = object.getString("Status");
                    if(updateStatus.equalsIgnoreCase("Success")) {
                        ((OCRScreen) context).searchResult(object.getJSONObject("server_response"));
                    }else{
                        Toast.makeText(context, parseJSON(object, "message"), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Boolean updateStatus = object.getBoolean("status");
                    if (updateStatus) {
                        if (call_from.equalsIgnoreCase("MainCate")) {
                            Intent intent = new Intent(context, MainCategories.class);
                            intent.putExtra("list", object.getJSONArray("data").toString());
                            context.startActivity(intent);
                        } else if (call_from.equalsIgnoreCase("SubCate")) {
                            Intent intent = new Intent(context, SubCategories.class);
                            intent.putExtra("list", object.getJSONArray("data").toString());
                            context.startActivity(intent);
                        } else if (call_from.equalsIgnoreCase("Link")) {
                            Intent intent = new Intent(context, SubCateLinks.class);
                            intent.putExtra("list", object.getJSONArray("data").toString());
                            context.startActivity(intent);
                        }
                    } else if (!updateStatus) {
                        Toast.makeText(context, parseJSON(object, "message"), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON PARSING Error : ",e.getMessage());
                e.printStackTrace();
            }

        }

    }

}
