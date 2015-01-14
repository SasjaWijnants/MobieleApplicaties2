package be.pxl.rest.testjsonrest;

import android.os.Bundle;
import android.view.View;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import com.google.gson.Gson;

public class MainActivity extends Activity {

   TextView tvIsConnected;
   TextView timeTV;
   TextView dateTV;
   TextView millisecondsTV;
   TextView originalTV;
   TextView md5TV;
   EditText test;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

       // get reference to the views
       tvIsConnected = (TextView) findViewById(R.id.tvIsConnected);
       timeTV = (TextView) findViewById(R.id.time);
       dateTV = (TextView) findViewById(R.id.date);
       millisecondsTV = (TextView) findViewById(R.id.milliseconds);
       originalTV = (TextView) findViewById(R.id.original);
       md5TV = (TextView) findViewById(R.id.md5);
       test = (EditText) findViewById(R.id.Text);
       
       // check if you are connected or not
       isConnected();

   }

   public void buttonHandler(View v){
       // call AsynTask to perform network operation on separate thread
	   if(isConnected()){
       new HttpAsyncTask().execute("http://date.jsontest.com/");
       String url = "http://md5.jsontest.com/?text="+test.getText();
       url = url.replace(" ", "%20");
       Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();
       new HttpAsyncTask().execute(url);
	   }

   }
   
   public static String GET(String url){
       InputStream inputStream = null;
       String result = "";
       try {

           // create HttpClient
           HttpClient httpclient = new DefaultHttpClient();

           // make GET request to the given URL
           HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

           // receive response as inputStream
           inputStream = httpResponse.getEntity().getContent();

           // convert inputstream to string
           if(inputStream != null)
               	result = convertInputStreamToString(inputStream);
           else
               result = "Did not work!";

       } catch (Exception e) {
           Log.d("InputStream", e.getLocalizedMessage());
       }

       return result;
   }

   private static String convertInputStreamToString(InputStream inputStream) throws IOException{
       BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
       String line = "";
       String result = "";
       while((line = bufferedReader.readLine()) != null)
           result += line;

       inputStream.close();
       return result;

   }

   public boolean isConnected(){
       ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
           NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
           if (networkInfo != null && networkInfo.isConnected()){
        	   tvIsConnected.setBackgroundColor(0xFF00CC00);
           	   tvIsConnected.setText("You are conncted");
               return true;
           }else{
        	   tvIsConnected.setBackgroundColor(0xFFFF0000);
               tvIsConnected.setText("You are NOT conncted");
               return false;
           }
   }
   
   private class HttpAsyncTask extends AsyncTask<String, Void, String> {
       @Override
       protected String doInBackground(String... urls) {
           return GET(urls[0]);
       }
       
       // onPostExecute displays the results of the AsyncTask.
       @Override
       protected void onPostExecute(String result) {
           //Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
    	   
    	   //Toast toas = Toast.makeText(getBaseContext(), result, 1000);
    	   //toas.show();
    	      	   
           Gson gson = new Gson();
           
           if(result.startsWith("{   \"md5")){
        	   md5Class md5 =gson.fromJson(result, md5Class.class);
        	   md5TV.setText(md5.getMd5());
        	   originalTV.setText(md5.getOriginal());
           }else if(result.startsWith("{   \"time")){
               TimeDateEntity indy = gson.fromJson(result, TimeDateEntity.class);
               timeTV.setText(indy.getTime());
               dateTV.setText(indy.getDate());
               millisecondsTV.setText(""+indy.getMilliseconds_since_epoch());
           }
           
           
           
      }
   }
}