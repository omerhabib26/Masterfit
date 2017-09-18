package www.tecjaunt.com.masterfit.networkConnection;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GetDataFromWebservice {
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        HttpURLConnection urlConnection = null;
        try {
            URL link = new URL(url);
            Log.d("HTTP Link",url);
            urlConnection = (HttpURLConnection) link.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.setRequestMethod("GET");

            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        finally {
            urlConnection.disconnect();
        }
        Log.i("InputStreamResult", result);
        return result;
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
