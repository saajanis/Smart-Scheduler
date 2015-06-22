package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.util.Arrays;
import java.util.List;


public class EventActivity extends ActionBarActivity{
    public static final String LOG_TAG = EventFragment.class.getSimpleName();
    public static DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static DateTimeFormatter PrettyDATEFORMAT = DateTimeFormat.forPattern("MMM d");
    public static final String PREFRENCES_NAME = "myprefrences";
    public static String userEmail;
    public static SharedPreferences settings;
    public static String[] EventsListStringArrayGlobal;
    public static ArrayList<ListItem> ListItemsListGlobal;
    public static ConnectivityManager conMgr;

    public static MyAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EventFragment())
                    .commit();
        }
        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        settings = getSharedPreferences(
                PREFRENCES_NAME, Context.MODE_PRIVATE);//user storage
        userEmail = settings.getString("useremail", "");

        // Add one tab to the Action Bar for display
        //ab.addTab(ab.newTab().setText("Events").setTabListener(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
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
        else if (id == R.id.logout) {
            SharedPreferences.Editor editor = settings.edit();// clear login details
            editor.clear();
            editor.commit();

            Intent intent = new Intent(this, UserRegistration.class);//Logout and redirect to registration
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }









    /**
     * Created by apoorva on 4/4/15.
     */
    public static class EventFragment extends Fragment {

        public static MyAdapter adapter = null;
        public EventFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Add this line in order for this fragment to handle menu events.
            setHasOptionsMenu(true);
            //setContentView(R.layout.fragment_main);
            //MyAdapter adapter = new MyAdapter(getActivity(), generateData());
            //ListView listView = (ListView) findViewById(R.id.listView_event);
            //listView.setAdapter(adapter);

        }

        private ArrayList<Model> generateData(){
            ArrayList<Model> models = new ArrayList<Model>();
            models.add(new Model(R.drawable.dot_icon,"MAS group meeting 03/24/2015"));
            models.add(new Model(R.drawable.check_mark_icon,"DVA group meeting 03/26/2015"));
            models.add(new Model(R.drawable.question_mark_icon,"C4G group meeting 03/27/2015"));
            models.add(new Model(-1,"Lunch at Esther's 03/28/2015"));
            models.add(new Model(R.drawable.cross_mark_icon,"Graduation Party Dinner 01/03/2015"));
            return models;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.eventfragment, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.action_new) {
                Intent intent = new Intent(getActivity(), CreateEvent.class);
                startActivity(intent);
                return true;
            }

            // add settings here

            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Default
            String[] eventsArray = {
                    "MAS group meeting 03/24/2015",
                    "DVA group meeting 03/26/2015",
                    "C4G group meeting 03/27/2015",
                    "Lunch at Estlalaher's 03/27/2015"
            };

//            SharedPreferences settings = getSharedPreferences(
//                    PREFRENCES_NAME, Context.MODE_PRIVATE);

            System.out.println("Email in EventActivity: "+userEmail);
            if (userEmail.equals("")){
                Intent intent = new Intent(getActivity(), UserRegistration.class);//ask to login first
                startActivity(intent);
            }


            // ARE WE CONNECTED TO THE NET
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
                String[] params = {userEmail};
                new getEventList().execute(params);
            }
            else{
                System.out.println("Not connected!");
            }

            System.out.println(EventsListStringArrayGlobal);

            ArrayList<ListItem> currentMeetingsList = new ArrayList<ListItem>();
            List<String> weekEvents = new ArrayList<String>(Arrays.asList(eventsArray));
            List<String> eventsList = new ArrayList<String>(Arrays.asList(eventsArray));
            adapter = new MyAdapter(getActivity(), eventsList);
            ArrayAdapter<String> mEventsAdapter;

            //setListAdapter(adapter);
            mEventsAdapter =
                    new ArrayAdapter<String>(
                            getActivity(),
                            R.layout.list_item,
                            R.id.event_text,
                            weekEvents);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //final MyAdapter adapter = new MyAdapter(getActivity(), generateData());
            //rootView.setAdapter(adapter);
            //final ImageView imageView = (ImageView) rootView.findViewById(R.id.event_logo);
            ListView listView = (ListView) rootView.findViewById(R.id.listView_event);
            listView.setAdapter(adapter);
            //listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String event = adapter.getItem(position);
                    //Model event = adapter.getItem(position);
                    //Toast.makeText(getActivity(), event, Toast.LENGTH_SHORT).show();
                    String title = "MAS Meeting \n 03/24/15";
                    String time = "4:30 PM";
                    String duration = "45 Minutes";
                    String attendees = "Jon Smith, Jane Doe, George Burdell";
                    String subject = "MAS Presentation";
                    String message = "Can we meet up to plan for the third presentation at Klaus 3rd floor?";


                    String listItemIdStr = null;
                    String listItemStatus = null;
                    if (ListItemsListGlobal!=null) {
                        if (ListItemsListGlobal.size() > position) {
                            listItemIdStr = Integer.toString(ListItemsListGlobal.get(position).getId());
                            listItemStatus = ListItemsListGlobal.get(position).getStatus();
                        }

                        Intent intent = new Intent(getActivity(), MeetingDetails.class) //default
                                .putExtra(Intent.EXTRA_TEXT, event);
                        if (listItemStatus.equals("new") || listItemStatus.equals("declined")) {
                            intent = new Intent(getActivity(), MeetingInvite.class)
                                    .putExtra(Intent.EXTRA_TEXT, event);
                        } else if (listItemStatus.equals("accepted")) {
                            intent = new Intent(getActivity(), MeetingDetails.class)
                                    .putExtra(Intent.EXTRA_TEXT, event);
                        }

                        intent.putExtra("position", listItemIdStr);

                        startActivity(intent);
                    }


//                if (position == 0){
//                    //imageView.setImageResource(R.drawable.check_mark_icon);//already scheduled
//                    Intent intent = new Intent(getActivity(), MeetingDetails.class)
//                            .putExtra(Intent.EXTRA_TEXT, event);
//                    intent.putExtra("title", title);
//                    intent.putExtra("time", time);
//                    intent.putExtra("duration", duration);
//                    intent.putExtra("attendees", attendees);
//                    intent.putExtra("subject", subject);
//                    intent.putExtra("message", message);
//                    startActivity(intent);
//                }
//                else if (position == 1) {
//                    //imageView.setImageResource(R.drawable.dot_icon);
//                    Intent intent = new Intent(getActivity(), MeetingInvite.class)
//                            .putExtra(Intent.EXTRA_TEXT, event);
//                    intent.putExtra("title", title);
//                    intent.putExtra("time", time);
//                    intent.putExtra("duration", duration);
//                    intent.putExtra("attendees", attendees);
//                    intent.putExtra("subject", subject);
//                    intent.putExtra("message", message);
//                    startActivity(intent);
//                }
//                else if (position == 2){
//                    //imageView.setImageResource(R.drawable.question_mark_icon);//time to be decided
//                    Intent intent = new Intent(getActivity(), TimeSlots.class);
//                    startActivity(intent);
//                }
//                else if (position == 3){
//                    Intent intent = new Intent(getActivity(), CreateEvent.class);
//                    startActivity(intent);
//                }
//                else if (position == 4){
//                    //imageView.setImageResource(R.drawable.cross_mark_icon);//declined
//                }

                }
            });
            return rootView;

        }

        public void seeMeetingInvitation(View view) {
            //Intent intent1 = new Intent(this, MeetingInvite.class);
            Intent myIntent = new Intent(getActivity(), MeetingInvite.class);
            String title = "MAS Meeting";
            String time = "4:30PM - 3/6/15";
            String duration = "45 Minutes";
            String attendees = "Jon Smith, Jane Doe, George Bundle";
            String subject = "Hi all,\n" +
                    "Can we meet up to plan for the third presentation at \n" +
                    "                \"Klaus 3rd floor? Hi all,\n" +
                    "Can we meet up to plan for the third presentation at \n" +
                    "                \"Klaus 3rd floor?";
            myIntent.putExtra("title", title);
            myIntent.putExtra("time", time);
            myIntent.putExtra("duration", duration);
            myIntent.putExtra("attendees", attendees);
            myIntent.putExtra("subject", subject);

            startActivity(myIntent);
        }

        public void seeMeetingDetails(View view) {
            //Intent intent1 = new Intent(this, MeetingInvite.class);
            Intent myIntent = new Intent(getActivity(), MeetingDetails.class);
            String title = "MAS Meeting";
            String time = "4:30PM - 3/6/15";
            String duration = "45 Minutes";
            String attendees = "Jon Smith, Jane Doe, George Bundle";
            String subject = "Hi all,\n" +
                    "Can we meet up to plan for the third presentation at \n" +
                    "                \"Klaus 3rd floor? Hi all,\n" +
                    "Can we meet up to plan for the third presentation at \n" +
                    "                \"Klaus 3rd floor?";
            myIntent.putExtra("title", title);
            myIntent.putExtra("time", time);
            myIntent.putExtra("duration", duration);
            myIntent.putExtra("attendees", attendees);
            myIntent.putExtra("subject", subject);

            startActivity(myIntent);
        }



        //NETWORK ACTIVITY
        static class getEventList extends AsyncTask<String, Void, String[]> {

            private Exception exception;

            @Override
            protected String[] doInBackground(String... params) {
                //SAAJAN
                String[] returnEventsListStringArray = getEventsString(params[0]);
                //
                EventsListStringArrayGlobal = returnEventsListStringArray;

                for(String item: returnEventsListStringArray){
                    System.out.println("Final Array: " + item);
                }

                return returnEventsListStringArray;
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (result != null){

                    adapter.clear();
                    for (String meetingName : result) {

                        adapter.add(meetingName);
                    }
                }
                adapter.notifyDataSetChanged();

            }
        }

        static String[] getEventsString (String email){
            HttpURLConnection urlConnection = null;
            String[] EventsListStringArray = null;

            try {
                String GET_EVENTS_URL =
                        Utility.BASE_URL+"/api/events/";
                //GET_EVENTS_URL = GET_EVENTS_URL+"?creator_email="+email; //get everything and manually filter

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(GET_EVENTS_URL);
                //httpget.setHeader("email", email);
                HttpResponse response = httpclient.execute(httpget);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                Object obj= JSONValue.parse(builder.toString());
                JSONArray finalResult=(JSONArray)obj;

                ArrayList<ListItem> ListItemsList = new ArrayList<ListItem>();
                ArrayList<String> returnEventsListStringList = new ArrayList<String>();

                Log.e(LOG_TAG, "JSON Array: " + finalResult.toString(), new Exception());
                for (int i=0; i<finalResult.size(); i++){

                    JSONObject curr_JSON = (JSONObject) finalResult.get(i);
                    Log.e(LOG_TAG, "JSON Object: "+curr_JSON.toString(), new Exception());

                    String creator_email = curr_JSON.get("creator_email").toString();
                    String inviteeStr = curr_JSON.get("invitee").toString();
                    String[] inviteeArray = null;
                    if (inviteeStr!=null){inviteeArray = inviteeStr.split(",");}

                    if (creator_email.equals(email) || Arrays.asList(inviteeArray).contains(email)) {
                        ListItem currentItem = new ListItem(Integer.parseInt(curr_JSON.get("id").toString()), curr_JSON.get("name").toString(),
                                curr_JSON.get("status") == null ? "N/A" : curr_JSON.get("status").toString());
                        ListItemsList.add(currentItem);

                        String startDate = curr_JSON.get("start_date") == null ? "N/A" : curr_JSON.get("start_date").toString().split(",")[0];
                        String endDate = curr_JSON.get("end_date") == null ? "N/A" : curr_JSON.get("end_date").toString().split(",")[0];

                        String startDatePretty = "", endDatePretty = "";
                        //prettify display dates
                        if (!startDate.equals("N/A")) {
                            DateTime fieldStartDate = ISO8601DATEFORMAT.parseDateTime(startDate);
                            startDatePretty = PrettyDATEFORMAT.print(fieldStartDate);
                        }
                        if (!endDate.equals("N/A")) {
                            DateTime fieldEndDate = ISO8601DATEFORMAT.parseDateTime(endDate);
                            endDatePretty = PrettyDATEFORMAT.print(fieldEndDate);
                        }

                        returnEventsListStringList.add(curr_JSON.get("name").toString() + " (" + startDatePretty + " - " + endDatePretty + ")");
                        System.out.println(curr_JSON.get("duration").toString());
                    }
                }

                String[] returnEventsListStringArray = new String[returnEventsListStringList.size()];
                for (int i=0; i<returnEventsListStringList.size(); i++){
                    returnEventsListStringArray[i] = returnEventsListStringList.get(i);
                }

                // set Globals before returning
                ListItemsListGlobal = ListItemsList;
//                for (ListItem item: ListItemsListGlobal){
//                    System.out.println("List Item: " + item.getId());
//                }
                EventsListStringArray = returnEventsListStringArray.clone();
                //Log.e(LOG_TAG, "Name Array: "+returnEventsListStringArrayList.toString(), new Exception());
                //System.out.println(returnEventsListStringArrayList.toString());


                //return EventsListStringArray;
                //return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error I/O", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                //return EventsListStringArray;
            }

            try {
                return EventsListStringArray;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }



    }



}






