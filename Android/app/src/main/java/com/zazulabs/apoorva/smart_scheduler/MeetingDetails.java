package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by apoorva on 4/4/15.
 */
public class MeetingDetails extends ActionBarActivity {

    public static final String LOG_TAG = MeetingDetails.class.getSimpleName();
    static View rootView;
    static TextView nameView, hostView, durationView, descriptionView, inviteesView;
    static String position, name, host, duration, description, invitees, timeslots;
    public static ConnectivityManager conMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_details);
        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Intent intent = getIntent();
        position = intent.getStringExtra("position");
        //default
        name =  "N/A";
        host = "N/A";
        duration = "N/A";
        description = "N/A";
        invitees = "N/A";
        timeslots = "N/A";

        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            String[] params = {position};
            new getEventList().execute(params);
        }
        else{
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
            System.out.println("Not connected to the internet!");
        }


        System.out.println(position);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meeting_details, menu);
        return true;
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
        else if (id == R.id.action_new) {
            Intent intent = new Intent(this, CreateEvent.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void goToTimeSlotsMeetingDetails(View view) {
        Intent intent = new Intent(this, TimeSlots.class);
        intent.putExtra("timeSlots", timeslots);
        intent.putExtra("id", position);

        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_meeting_details, container, false);
            nameView = (TextView) rootView.findViewById(R.id.name);
            hostView = (TextView) rootView.findViewById(R.id.host);
            durationView = (TextView) rootView.findViewById(R.id.duration);
            descriptionView = (TextView) rootView.findViewById(R.id.description);
            inviteesView = (TextView) rootView.findViewById(R.id.invitees);
            nameView.setText(name);
            hostView.setText(host);
            durationView.setText(duration);
            descriptionView.setText(description);
            inviteesView.setText(invitees);

            return rootView;
        }
    }


    // Network Functions
    static class getEventList extends AsyncTask<String, Void, String[]> {

        private Exception exception;

        @Override
        protected String[] doInBackground(String... params) {
            //SAAJAN
            String idStr = params[0];


            getEventsStringByID(idStr); //updates text views too

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            // do nothing
        }


        //Actual Network Activity
        static String[] getEventsStringByID (String idStr){
            HttpURLConnection urlConnection = null;
            String[] EventsListStringArray = null;

            try {
                String GET_EVENTS_URL =
                        Utility.BASE_URL+"/api/events/";
                GET_EVENTS_URL = GET_EVENTS_URL+"?id="+idStr;

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(GET_EVENTS_URL);
                System.out.println("ID: " + idStr);
                //httpget.setHeader("id", "1");

                HttpResponse response = httpclient.execute(httpget);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                Object obj= JSONValue.parse(builder.toString());
                JSONArray finalResult=(JSONArray)obj;

                JSONObject details_JSON = (JSONObject) finalResult.get(0);

                System.out.println(details_JSON);
                System.out.println(details_JSON.get("name").toString());
                name =  details_JSON.get("name").toString();
                host = details_JSON.get("creator_name").toString();
                duration = details_JSON.get("duration").toString();
                description = details_JSON.get("description").toString();
                invitees = details_JSON.get("invitee").toString();
                timeslots = details_JSON.get("timeSlots").toString();

                // update textViews
                nameView.post(new Runnable() { public void run() {nameView.setText(name);}});
                hostView.post(new Runnable() { public void run() {hostView.setText(host);}});
                durationView.post(new Runnable() { public void run() {durationView.setText(duration);}});
                descriptionView.post(new Runnable() { public void run() {descriptionView.setText(description);}});
                inviteesView.post(new Runnable() { public void run() {inviteesView.setText(invitees);}});

                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error I/O", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }



            // This will only happen if there was an error getting or parsing the forecast.
            //return null;
        }
    }
}

