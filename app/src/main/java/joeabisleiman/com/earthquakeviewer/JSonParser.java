package joeabisleiman.com.earthquakeviewer;

/**
 * Created by Joe Abi Sleiman on 4/30/2018.
 */


import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import org.json.JSONException;
import org.json.JSONObject;

public class JSonParser {
    final String TAG = "JsonParser.java";
    static JSONObject jObj = null;
    static String json = "";

    public JSONObject getJSONFromUrl(String url) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL urlAddress = new URL(url);
            connection = (HttpURLConnection) urlAddress.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " +line);
            }

            json = buffer.toString();
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(connection != null)
                connection.disconnect();
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "Error Parsing JSON data " + e.toString());
        }
        return jObj;
    }
}
