package com.example.howard.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    EditText cityName;
    ImageView background;
    TextView resultTextView;
    ImageView icon;
    String iconUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = (EditText) findViewById(R.id.cityName);
        background = (ImageView) findViewById(R.id.imageView2);
        background.setImageResource(R.drawable.background);
        resultTextView = (TextView) findViewById(R.id.resultText);
        icon = (ImageView) findViewById(R.id.icon);

    }

    public void findWeather(View view) {

        Log.i("cityName",cityName.getText().toString());

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityName.getWindowToken(),0);

        try {
            String encodedCityName = URLEncoder.encode(cityName.getText().toString(),"UTF-8");
            DownloadTask task = new DownloadTask();
            task.execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName
                    + "&APPID=3924274447dd3a7de482c03b0ad989e5\n");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Could not find weather", Toast.LENGTH_LONG);
        }

    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }

                return result;

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Could not find weather", Toast.LENGTH_LONG);
            }

            return null;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                String msg = "";
                String iconTxt = "";
                JSONObject jsonObject = new JSONObject(result);
                String weatherInfo = jsonObject.getString("weather");
                Log.i("Weather content", weatherInfo);
                JSONArray arr = new JSONArray(weatherInfo);
                for (int i = 0; i < arr.length(); i++) {

                    JSONObject jsonPart = arr.getJSONObject(i);
                    String main = "";
                    String description = "";
                    main = jsonPart.getString("main");
                    description = jsonPart.getString("description");
                    String iconLoop = "";
                    iconLoop = jsonPart.getString("icon");

                    if(main != "" && description != ""){
                        msg += main + ": " + description + "\r\n";
                    }
                    iconTxt = iconLoop;

                }

                iconUrl = "http://openweathermap.org/img/w/" + iconTxt + ".png";
                System.out.println(iconUrl);

                if(iconUrl != ""){

                    ImageDownloader task = new ImageDownloader();
                    Bitmap myImage;
                    try {
                        myImage = task.execute(iconUrl).get();
                        icon.setImageBitmap(myImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("running");
                } else{
                    Toast.makeText(getApplicationContext(),"Could not find weather", Toast.LENGTH_LONG);

                }

                if(msg != ""){
                    resultTextView.setText(msg);
                } else {
                    Toast.makeText(getApplicationContext(),"Could not find weather", Toast.LENGTH_LONG);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
