package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by apoorva on 4/4/15.
 */
public class MeetingInvite extends ActionBarActivity {

    public static final String LOG_TAG = MeetingInvite.class.getSimpleName();
    static View rootView;
    DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final String PREFRENCES_NAME = "myprefrences";
    public static SharedPreferences settings;
    static TextView nameView, hostView, durationView, descriptionView, inviteesView;
    static String position, name, host, duration, description, invitees, timeslots, start_date, end_date;
    public static ConnectivityManager conMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_invite);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        settings = getSharedPreferences(
                PREFRENCES_NAME, Context.MODE_PRIVATE);//user storage
        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);

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
            System.out.println("Not connected!");
        }


        System.out.println(position);
    }


    public class TimeStringComparator implements Comparator<String> {
        DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        @Override
        public int compare(String time1, String time2) {
            //System.out.println("For Times: |"+time1.trim() + "| |" + time2.trim()+"|");
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
        getMenuInflater().inflate(R.menu.menu_meeting_invite, menu);
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

    public String getCurrentCalendarTimeSlots(){
        //

        String dummycal = new calendarFunctions().readCalendarEvent(getApplicationContext())[2].toString();

        // Calculate Slots
        ArrayList<String> startDates = new calendarFunctions().readCalendarEvent(getApplicationContext())[1];
        ArrayList<String> endDates = new calendarFunctions().readCalendarEvent(getApplicationContext())[2];
        //Log.e(LOG_TAG, "StartDatesPased: " + startDates.toString(), new Exception());
        //Log.e(LOG_TAG, "EndDatesPased: " + endDates.toString(), new Exception());

        // Rectify input dates
        // Rectify input dates
        DateTimeFormatter fieldFormat = DateTimeFormat.forPattern("MMM d, yyyy, hh:mm:ss aa");
        DateTimeFormatter androidFormat = DateTimeFormat.forPattern("dd/MM/yyyy");

        DateTime fieldStartDate = ISO8601DATEFORMAT.parseDateTime(start_date);
        String eventStartDateInputString = androidFormat.print(fieldStartDate);
        DateTime fieldEndDate = ISO8601DATEFORMAT.parseDateTime(end_date);
        String eventEndDateInputString = androidFormat.print(fieldEndDate);

        String startTimeAppend = settings.getString("starttimepreference", "");
        String endTimeAppend = settings.getString("endtimepreference", "");
        if (startTimeAppend.equals("")){startTimeAppend=" 08:00:00 AM";} //default
        if (endTimeAppend.equals("")){endTimeAppend=" 08:00:00 PM";} //default

        ArrayList<String> finalTimeSlots = Utility.getTimeSlots(eventStartDateInputString+startTimeAppend, eventEndDateInputString+endTimeAppend, duration, startDates, endDates);
        Log.e(LOG_TAG, "AvailableTimeSlotsMeetingAccept: " + finalTimeSlots.toString(), new Exception());
        String finalTimeSlotsString = "";
        for (String s : finalTimeSlots){
            finalTimeSlotsString += s + ",";
        }

        return finalTimeSlotsString;
    }

    public void goToEventActivityDecline(View view) { //update status to declined

        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            //update status
            String[] params = {position, "declined", timeslots};
            new updateEventStatus().execute(params);

            Toast.makeText(getApplicationContext(), "Invitation Declined!", Toast.LENGTH_SHORT).show();
            //go to meetings list
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
            System.out.println("Not connected to the internet!");
        }
    }

    public void goToEventActivityAccept(View view) { //update status to accepted

        //update status
        String currentCalendarTimeSlots = getCurrentCalendarTimeSlots();// get currentCalendarTimeSlots

        // ARE WE CONNECTED TO THE NET
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            String[] params = {position, "accepted", currentCalendarTimeSlots};

            new updateEventStatus().execute(params);

            Toast.makeText(getApplicationContext(), "Invitation Accepted!", Toast.LENGTH_SHORT).show();
            //go to meetings list
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
            System.out.println("Not connected to the internet!");
        }
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
            rootView = inflater.inflate(R.layout.fragment_meeting_invite, container, false);
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
                start_date = details_JSON.get("start_date").toString();
                end_date = details_JSON.get("end_date").toString();

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

    static class updateEventStatus extends AsyncTask<String, Void, String[]> {

        private Exception exception;

        @Override
        protected String[] doInBackground(String... params) {
            //SAAJAN
            System.out.println("Updating Status\n\n\n\n\n\n");
            String idStr = params[0];
            String updatedStatus = params[1];
            String timeSlots = params[2];


            updateStatusByID(idStr, updatedStatus, timeSlots); //updates text views too

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            // do nothing
        }


        //Actual Network Activity
        static String[] updateStatusByID (String idStr, String updatedStatus, String timeSlots){
            HttpURLConnection urlConnection = null;
            String[] EventsListStringArray = null;

            System.out.println("Updating event: "+idStr+" with status: "+updatedStatus);
            urlConnection = null;
            try {
                final String FORECAST_BASE_URL =
                        Utility.BASE_URL+"/api/events/";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("id", idStr));
                nameValuePairs.add(new BasicNameValuePair("status", updatedStatus));
                nameValuePairs.add(new BasicNameValuePair("timeSlots", timeSlots));

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



            // This will only happen if there was an error getting or parsing the forecast.
            //return null;
        }
    }

    //CALENDARS
    public class calendarFunctions {
        public ArrayList<String> nameOfEvent = new ArrayList<String>();
        public ArrayList<String> startDates = new ArrayList<String>();
        public ArrayList<String> endDates = new ArrayList<String>();
        public ArrayList<String> descriptions = new ArrayList<String>();

        public ArrayList[] returnLists = {nameOfEvent, startDates, endDates, descriptions};



        public ArrayList[] readCalendarEvent(Context context) {
            Cursor cursor = context.getContentResolver()
                    .query(
                            Uri.parse("content://com.android.calendar/events"),
                            new String[]{"calendar_id", "title", "description",
                                    "dtstart", "dtend", "eventLocation"}, null,
                            null, null);
            cursor.moveToFirst();
            // fetching calendars name
            String CNames[] = new String[cursor.getCount()];

            // fetching calendars id
            nameOfEvent.clear();
            startDates.clear();
            endDates.clear();
            descriptions.clear();
            for (int i = 0; i < CNames.length; i++) {

                nameOfEvent.add(cursor.getString(1));
                int start_field_index = cursor.getColumnIndex("DTSTART");
                int titleIndex = cursor.getColumnIndex("title");
                //System.out.println("Event Name: "+ cursor.getString(titleIndex));

                if (cursor.getString(start_field_index)!=null) {
                    startDates.add(getDate(Long.parseLong(cursor.getString(start_field_index))));
                }
                else{
                    endDates.add(getDate(0l));
                }
                int end_field_index = cursor.getColumnIndex("DTEND");
                if (cursor.getString(end_field_index)!=null) {
                    endDates.add(getDate(Long.parseLong(cursor.getString(end_field_index))));
                }
                else{
                    endDates.add(getDate(0l));
                }
                descriptions.add(cursor.getString(2));
                CNames[i] = cursor.getString(1);
                cursor.moveToNext();

            }

            for (int i=0; i<returnLists[0].size(); i++){
                //System.out.println(returnLists[0].get(i) + " " +returnLists[1].get(i) + " " +
                //returnLists[2].get(i) + " " +returnLists[3].get(i) );
            }
            return returnLists;
        }

        public String getDate(long milliSeconds) {

            DateTimeFormatter formatter = DateTimeFormat.forPattern(
                    "dd/MM/yyyy hh:mm:ss aa");
            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = calendar.getTimeZone();

            //Log.d("Time zone","="+timeZone.getDisplayName());
            calendar.setTimeInMillis(milliSeconds); //System.out.print("Calendar time slot Old: " + calendar.getTime() + "New From: "+formatter.print(calendar.getTimeInMillis()) + " Time Zone: "+timeZone.getDisplayName());
            return formatter.print(calendar.getTimeInMillis());
        }
    }
}

