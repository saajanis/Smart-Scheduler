package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by apoorva on 4/4/15.
 */
public class TimeSlots extends ActionBarActivity implements
        OnClickListener {

    DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    DateTimeFormatter HumanReadableDATEFORMAT = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss Z");

    private final String LOG_TAG = CreateEvent.class.getSimpleName();
    String id;
    Button button;
    ListView listView;
    ArrayAdapter<String> adapter;
    public static ConnectivityManager conMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_time_slots);


        listView = (ListView) findViewById(R.id.listView_timeSlots);
        button = (Button) findViewById(R.id.acceptTimeSlotsButton);
        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);

        //Get time Slots
        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        String timeSlotsGet = intent.getStringExtra("timeSlots");
        String[] timeSlots;
        if(timeSlotsGet.contains(",")) {
            timeSlots = intent.getStringExtra("timeSlots").split(",");
        }
        else{
            timeSlots = new String[]{intent.getStringExtra("timeSlots")};
        }

        List<String> timeSlotsList = Arrays.asList(timeSlots);
        Collections.sort(timeSlotsList, new TimeStringComparator());
        timeSlots = timeSlotsList.toArray(new String[timeSlotsList.size()]);

        for (int i=0; i<timeSlots.length; i++){
            DateTime ISODateTimeDT;
            if (!timeSlots[i].equals("")) {
                ISODateTimeDT = ISO8601DATEFORMAT.parseDateTime(timeSlots[i]);
            }
            else{
                int j=0;
                for (j=0; i<timeSlots.length; j++){ //find an empty dummy string
                    if (!timeSlots[j].equals("")){
                        break;
                    }
                }
                ISODateTimeDT = ISO8601DATEFORMAT.parseDateTime(timeSlots[j]);
            }
            timeSlots[i] = HumanReadableDATEFORMAT.print(ISODateTimeDT);
        }

                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_multiple_choice, timeSlots);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for (int i=0; i<timeSlots.length; i++){//default checked
            listView.setItemChecked(i, true);
        }
        listView.setAdapter(adapter);

        button.setOnClickListener(this);


    }


    public void onClick(View v) {

        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                selectedItems.add(adapter.getItem(position));
        }

        String[] outputStrArr = new String[selectedItems.size()];

        for (int i = 0; i < selectedItems.size(); i++) {
            String humanReadableSelectedDateStr = selectedItems.get(i);
            DateTime humanReadableSelectedDateDT = HumanReadableDATEFORMAT.parseDateTime(humanReadableSelectedDateStr);
            outputStrArr[i] = ISO8601DATEFORMAT.print(humanReadableSelectedDateDT);
        }
        //build comma separated string
        StringBuilder sb = new StringBuilder();
        for (String slot : outputStrArr) {
            if (sb.length() > 0) sb.append(',');
            sb.append(slot);
        }
        String slotsCommaSeparated = sb.toString();


        for (String item: outputStrArr) {
            System.out.println("Selected Time Slots: " + item);
        }

        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            //update time slots post to server
            String[] params = {id, slotsCommaSeparated};
            new UpdateSlots().execute(params);
            Toast.makeText(getApplicationContext(), "Time Slots Updated!", Toast.LENGTH_SHORT).show();

            //redirect to eventfragment
            Intent myIntent = new Intent(this, EventActivity.class);
            startActivity(myIntent);
        }
        else{
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
            System.out.println("Not connected to the internet!");
        }
    }


    public class TimeStringComparator implements Comparator<String> {


        @Override
        public int compare(String time1, String time2) {
            System.out.println("For Times: |"+time1.trim() + "| |" + time2.trim()+"|");
            if (time2.trim().equals("")){
                return 1;
            }
            else if (time1.trim().equals("")){
                return -1;
            }
            else if (time2.trim().equals("") && time1.trim().equals("")){
                return 0;
            }
            else {
                if (time2.trim().equals("") || time1.trim().equals("")){
                    return 0;
                }
                DateTime dateTime1 = ISO8601DATEFORMAT.parseDateTime(time1.trim()); //trim trailing spaces
                DateTime dateTime2 = ISO8601DATEFORMAT.parseDateTime(time2.trim());
                return dateTime1.compareTo(dateTime2);
            }
        }

    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_time_slots, menu);
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

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    //NETWORK ACTIVITY
    class UpdateSlots extends AsyncTask<String, Void, String[]> {

        private Exception exception;

        protected String[] doInBackground(String... params) {
            //SAAJAN
            String eventID = params[0].toString();
            String updatedTimeSlots = params[1].toString();
            System.out.println("Updating event: "+eventID+" with: "+updatedTimeSlots);
            HttpURLConnection urlConnection = null;
            try {
                final String FORECAST_BASE_URL =
                        Utility.BASE_URL+"/api/events/";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("id", eventID));
                nameValuePairs.add(new BasicNameValuePair("timeSlots", updatedTimeSlots));

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPut httpput = new HttpPut(FORECAST_BASE_URL);
                httpput.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httpput);

                System.out.println(response.toString());

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

