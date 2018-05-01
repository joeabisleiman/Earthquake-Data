package joeabisleiman.com.earthquakeviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    Button retrieveData;
    TextView txtJson;
    ProgressBar pb;
    ProgressDialog pd;

    private ListView lv;
    ArrayList<HashMap<String, String>> eqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveData = (Button) findViewById(R.id.retrieveData);
        //txtJson = (TextView) findViewById(R.id.textView);

        eqList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listView);

        retrieveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected()) {
                    Toast.makeText(getApplicationContext(),"No Connectivity. Please Try again later.", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        new JsonTask().execute("");
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        final String TAG = "AsyncTask.java";

        String jsonURL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";

        JSONArray dataJsonArr = null;

        protected void onPreExecute() {
            super.onPreExecute();

            pb = new ProgressBar(MainActivity.this);
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Fetching Data");
            pd.show();
            pb.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {
            eqList.clear();
            try {
                JSonParser jParser = new JSonParser();
                JSONObject json = jParser.getJSONFromUrl(jsonURL);
                dataJsonArr = json.getJSONArray("features");
                String place = "";
                String link = "";
                String title = "";
                String time = "";

                for(int i = 0; i < dataJsonArr.length(); i++) {
                    JSONObject obj = dataJsonArr.getJSONObject(i);
                    JSONObject properties = obj.getJSONObject("properties");
                    place = properties.getString("place");
                    link = properties.getString("url");
                    title = properties.getString("title");
                    time = properties.getString("time");
                    Log.d(TAG," ---------------- Place is :" + place);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("title", title);
                    //map.put("place", place);
                    map.put("time", epochToDate(time));
                    map.put("link", link);
                    eqList.add(map);
                }
                //return place;

                //Loop over all features
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            pb.setVisibility(View.INVISIBLE);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, eqList, R.layout.list_item, new String[]{"title", "time"}, new int[]{R.id.title,R.id.time});
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String url = eqList.get(i).get("link");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private String epochToDate(String time){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Long t = Long.parseLong(time);
        return sdf.format(new Date(t));
    }
}
