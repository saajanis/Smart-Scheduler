package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


public class UserRegistration extends ActionBarActivity {

    public static TextView FirstName, LastName, UserName, UserEmail, UserPassword;
    private final String LOG_TAG = UserRegistration.class.getSimpleName();
    public static ConnectivityManager conMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);

        FirstName = (EditText) findViewById(R.id.FirstName);
        LastName = (EditText) findViewById(R.id.LastName);
        UserName = (EditText) findViewById(R.id.UserName);
        UserEmail = (EditText) findViewById(R.id.UserEmail);
        UserPassword = (EditText) findViewById(R.id.UserPassword);

    }


    public void parseResponseTakeAction(String responseString){
        Intent intent = new Intent(this, EventActivity.class); //Redirect to login
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_registration, menu);
        return true;
    }

    public void initiateUserRegistration(View view) {
        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            String[] params = {FirstName.getText().toString(), LastName.getText().toString(), UserName.getText().toString(),
                    UserEmail.getText().toString(), UserPassword.getText().toString()};
            new PostUserRegistration().execute(params);
        }
        else{
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
            System.out.println("Not connected to the internet!");
        }

    }

    public void goToLoginPage(View view) {
        Intent intent = new Intent(this, UserLogin.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    //NETWORK ACTIVITY
    class PostUserRegistration extends AsyncTask<String, Void, String[]> {

        private Exception exception;

        protected String[] doInBackground(String... params) {
            //SAAJAN
            HttpURLConnection urlConnection = null;
            try {
                final String FORECAST_BASE_URL =
                        Utility.BASE_URL+"/api/registration/";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                String FirstName = params[0].toString();
                String LastName = params[1].toString();
                String UserName = params[2].toString();
                String UserEmail = params[3].toString();
                String UserPassword = params[4].toString();

                nameValuePairs.add(new BasicNameValuePair("username", UserName));
                nameValuePairs.add(new BasicNameValuePair("email", UserEmail));
                nameValuePairs.add(new BasicNameValuePair("password", UserPassword));
                nameValuePairs.add(new BasicNameValuePair("first_name", FirstName));
                nameValuePairs.add(new BasicNameValuePair("last_name", LastName));

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(FORECAST_BASE_URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);

                String encoding = EntityUtils.getContentCharSet(response.getEntity());
                encoding = encoding == null ? "UTF-8" : encoding;
                InputStream stream = AndroidHttpClient.getUngzippedContent(response.getEntity());
                InputStreamEntity unzEntity = new InputStreamEntity(stream,-1);
                String RegistrationAttemptResponse = EntityUtils.toString(unzEntity, encoding);

                System.out.println("Registration attempt response: "+RegistrationAttemptResponse);

                if (RegistrationAttemptResponse.length()>90) {
                    parseResponseTakeAction(RegistrationAttemptResponse);
                }

                return null;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return null;
            }
            //
        }

        protected void onPostExecute() {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}
