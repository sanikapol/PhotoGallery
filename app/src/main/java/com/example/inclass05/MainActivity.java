//Assignment Inclass 05
//File Name: Group12_InClass05
//Sanika Pol
//Snehal Kekan
package com.example.inclass05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "demo";
    Button btnGo;
    public ArrayList<String> urls  = new ArrayList<String>();
    ProgressBar progressBar;
    int curr = 0;
    ImageView iv_prev;
    ImageView iv_next;
    TextView tv_select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        Log.d(TAG,"Connected or not: " + isConnected());
        btnGo = findViewById(R.id.btnGo);
        tv_select = findViewById(R.id.tv_selectKeyword);
        iv_prev = findViewById(R.id.iv_prev);
        iv_next = findViewById(R.id.iv_next);
        iv_next.setVisibility(ImageView.INVISIBLE);
        iv_prev.setVisibility(ImageView.INVISIBLE);

        if (isConnected()) {
            //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            btnGo.setEnabled(true);
            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"In on click listner");
                    new GetKeywords().execute();
                }
            });


            iv_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   int size = urls.size();
                   if(curr==size-1){
                       curr = 0;
                   }
                   else {
                       curr = curr +1;
                   }
                    Log.d(TAG,"Next for no:  " + curr);

                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    new GetImage((ImageView) findViewById(R.id.iv_image)).execute(urls.get(curr));

                }
            });

            iv_prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int size = urls.size();

                    if(curr==0){

                        curr = size-1;
                    }
                    else {

                        curr = curr - 1;
                    }
                    Log.d(TAG,"Prev for no:  " + curr);
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    new GetImage((ImageView) findViewById(R.id.iv_image)).execute(urls.get(curr));

                }
            });

        } else {
            Log.d(TAG,"Not connected");
            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
            btnGo.setEnabled(false);
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !((NetworkInfo) networkInfo).isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    class GetKeywords extends AsyncTask<Void,Void, String[]>{
        @Override
        protected String[] doInBackground(Void... voids) {

            StringBuilder stringBuilder = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String result = null;
            String[] keywordStrings = null;
            try {
                URL url = new URL("http://dev.theappsdr.com/apis/photos/keywords.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d(TAG, "doInBackground: "+connection.getResponseCode());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result = stringBuilder.toString();
                    Log.d(TAG,"Keyword: " + result);
                    keywordStrings = result.split(";");

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Handle the exceptions
             finally {
                //Close open connections and reader
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return keywordStrings;

        }

        @Override
        protected void onPostExecute(final String[] strings) {
            super.onPostExecute(strings);
            Log.d(TAG,"onPostExecute "+strings[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select a Keyword!")
                    .setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Clicked on: " + strings[which]);
                            tv_select.setText(strings[which]);
                            String keyword = strings[which];
                            new GetImageURLs().execute(keyword);
                        }
                    });

            builder.create().show();

        }
    }

    class GetImageURLs extends AsyncTask<String,Void,ArrayList<String>>{
        @Override
        protected ArrayList<String> doInBackground(String... strings) {

            String strUrl = null;
            String result = null;
            ArrayList<String> results = new ArrayList<>();
            try {
                strUrl = "http://dev.theappsdr.com/apis/photos/index.php" + "?" + "keyword=" + URLEncoder.encode(strings[0], "UTF-8");
                URL url = new URL(strUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result= IOUtils.toString(connection.getInputStream(), "UTF8");

                    String[] substrings = result.split("\n");
                    for (String s:substrings){
                        Log.d(TAG,"Image URLs : " + s);
                        results.add(s.trim());
                    }
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {


            }
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            Log.d(TAG,"strings length" + strings.size());
            if(strings.size()>0) {
                Log.d(TAG, "post Image URLs : " + strings);
                urls = strings;
                progressBar.setVisibility(ProgressBar.VISIBLE);
                new GetImage((ImageView) findViewById(R.id.iv_image)).execute(strings.get(0));
                curr = 0;
                iv_next.setVisibility(ImageView.VISIBLE);
                iv_prev.setVisibility(ImageView.VISIBLE);
            }
            else{
                urls = strings;
                ImageView imageView = findViewById(R.id.iv_image);
                imageView.setImageDrawable(null);
                iv_next.setVisibility(ImageView.INVISIBLE);
                iv_prev.setVisibility(ImageView.INVISIBLE);
                Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    class GetImage extends AsyncTask<String,Void, Bitmap>{

        ImageView imageView;
        Bitmap bitmap = null;

        public GetImage(ImageView imageView) {
            this.imageView = imageView;
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            bitmap = null;

            try {
                URL url = new URL(strings[0]);
                Log.d(TAG, " Image URL " + url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG,"input stream " + connection.getInputStream());
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    Log.d(TAG,"is image there?" + bitmap.toString());
                }else {
                    bitmap = null;
                }

                Log.d(TAG,"Got the image" + bitmap.toString());

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {


            }

            return  bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG,bitmap+"");
            if(bitmap !=null){
                Log.d(TAG,"Setting Image");
                imageView.setImageBitmap(bitmap);
            }
            else {
                Log.d(TAG,"ImageNotFound");
                imageView.setImageDrawable(null);
                Toast.makeText(MainActivity.this, "No Images Found", Toast.LENGTH_SHORT).show();
                iv_next.setVisibility(ImageView.INVISIBLE);
                iv_prev.setVisibility(ImageView.INVISIBLE);
            }
            progressBar.setVisibility(ProgressBar.INVISIBLE);

        }
    }
}
