package www.tecjaunt.com.masterfit.networkConnection;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Omer Habib on 1/23/2017.
 */

public class SetDataInWebService {

    public static String SET(String address, String parms){
        InputStream inputStream = null;
        String result = "";
        HttpURLConnection urlConnection = null;
        Uri.Builder builder = new Uri.Builder();
        try {
            Log.i("URL + Parms",address+"  : "+parms);
            URL url = new URL(address);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(75000);
            urlConnection.setReadTimeout(75000);
            urlConnection.setRequestMethod("POST");
//            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            urlConnection.setRequestProperty("Content-Type", "text/html");
            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(parms);
            writer.flush();
            writer.close();
            outputStream.close();

            JSONObject jsonObject = new JSONObject();
            // get stream
            if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }
            // parse stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp, response = "";
            while ((temp = bufferedReader.readLine()) != null) {
                response += temp;
            }
            Log.i("response",response);
            // put into JSONObject
            jsonObject.put("Content", response);
            jsonObject.put("Message", urlConnection.getResponseMessage());
            jsonObject.put("Length", urlConnection.getContentLength());
            jsonObject.put("Type", urlConnection.getContentType());
            return response;
        } catch (IOException | JSONException e) {
            return e.toString();
        }
    }
}
