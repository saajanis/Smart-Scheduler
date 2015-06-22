package com.zazulabs.apoorva.smart_scheduler;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by apoorva on 4/4/15.
 */
public class CreateEvent extends ActionBarActivity {

    EditText eventName, eventStart, eventEnd, duration, invitees, message, decideByDate;
    DateTimeFormatter fieldFormat = DateTimeFormat.forPattern("MMM d, yyyy, hh:mm:ss aa");
    DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static String selectedEmails = "";
    private Exception exception;
    public static final String PREFRENCES_NAME = "myprefrences";
    public static SharedPreferences settings;
    private final String LOG_TAG = CreateEvent.class.getSimpleName();
    Activity globalActivity;
    //List<Groups> Groups = new ArrayList<Groups>();
    ListView GroupsListView;
    Button createBtn;
    SimpleDateFormat dateFormatter;
    public static ConnectivityManager conMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        globalActivity = this;
        settings = getSharedPreferences(
                PREFRENCES_NAME, Context.MODE_PRIVATE);//user storage
        conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);

        eventName = (EditText) findViewById(R.id.name);
        eventStart = (EditText) findViewById(R.id.startDate);
        eventStart.setInputType(InputType.TYPE_NULL);
        eventEnd = (EditText) findViewById(R.id.endDate);
        eventEnd.setInputType(InputType.TYPE_NULL);
        duration = (EditText) findViewById(R.id.duration);
        decideByDate = (EditText) findViewById(R.id.decideByDate);
        decideByDate.setInputType(InputType.TYPE_NULL);
        invitees = (EditText) findViewById(R.id.invitees);
        invitees.setInputType(InputType.TYPE_NULL);
        message = (EditText) findViewById(R.id.msg);

        dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        eventStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentDate=Calendar.getInstance();
                int mYear, mMonth, mDay;
                mYear=currentDate.get(Calendar.YEAR);
                mMonth=currentDate.get(Calendar.MONTH);
                mDay=currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(selectedYear, selectedMonth, selectedDay);
                        eventStart.setText(dateFormatter.format(newDate.getTime()));
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();  }
        });

        eventEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentDate=Calendar.getInstance();
                int mYear, mMonth, mDay;
                mYear=currentDate.get(Calendar.YEAR);
                mMonth=currentDate.get(Calendar.MONTH);
                mDay=currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(selectedYear, selectedMonth, selectedDay);
                        eventEnd.setText(dateFormatter.format(newDate.getTime()));
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();  }
        });

        decideByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentDate=Calendar.getInstance();
                int mYear, mMonth, mDay;
                mYear=currentDate.get(Calendar.YEAR);
                mMonth=currentDate.get(Calendar.MONTH);
                mDay=currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(selectedYear, selectedMonth, selectedDay);
                        decideByDate.setText(dateFormatter.format(newDate.getTime()));
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();  }
        });

        ///to remove if trial succeeds
        createBtn = (Button) findViewById(R.id.btnAdd);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //SAAJAN CALENDARS





                //

                String dummycal = new calendarFunctions().readCalendarEvent(getApplicationContext())[2].toString();

                // Calculate Slots
                ArrayList<String> startDates = new calendarFunctions().readCalendarEvent(getApplicationContext())[1];
                ArrayList<String> endDates = new calendarFunctions().readCalendarEvent(getApplicationContext())[2];
                //Log.e(LOG_TAG, "StartDatesPased: " + startDates.toString(), new Exception());
                //Log.e(LOG_TAG, "EndDatesPased: " + endDates.toString(), new Exception());

                // Rectify input dates
                DateTimeFormatter fieldFormat = DateTimeFormat.forPattern("MM/dd/yyyy");
                DateTimeFormatter androidFormat = DateTimeFormat.forPattern("dd/MM/yyyy");
                DateTime fieldStartDate = fieldFormat.parseDateTime(eventStart.getText().toString());
                String eventStartDateInputString = androidFormat.print(fieldStartDate);
                DateTime fieldEndDate = fieldFormat.parseDateTime(eventEnd.getText().toString());
                String eventEndDateInputString = androidFormat.print(fieldEndDate);
                //


                String startTimeAppend = settings.getString("starttimepreference", "");
                String endTimeAppend = settings.getString("endtimepreference", "");
                if (startTimeAppend.equals("")){startTimeAppend=" 08:00:00 AM";} //default
                if (endTimeAppend.equals("")){endTimeAppend=" 08:00:00 PM";} //default

                ArrayList<String> finalTimeSlots = Utility.getTimeSlots(eventStartDateInputString+startTimeAppend, eventEndDateInputString+endTimeAppend, duration.getText().toString(), startDates, endDates);
                Collections.sort(finalTimeSlots, new TimeStringComparator()); //sort time slots
                Log.e(LOG_TAG, "AvailableTimeSlots: " + finalTimeSlots.toString(), new Exception());
                String finalTimeSlotsString = "";
                for (String s : finalTimeSlots){
                    if (!s.trim().equals("")) {
                        finalTimeSlotsString += s + ",";
                    }
                }


                String userEmail = settings.getString("useremail", "");
                String[] params = {eventName.getText().toString(), message.getText().toString(), duration.getText().toString(),
                        userEmail, decideByDate.getText().toString(), eventStart.getText().toString(),
                        eventEnd.getText().toString(), selectedEmails, "Gokul Raghuraman", finalTimeSlotsString};

                // ARE WE CONNECTED TO THE NET
                if (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable()
                        && conMgr.getActiveNetworkInfo().isConnected()) {
                    new PostEventServer().execute(params);

                    Toast.makeText(getApplicationContext(), "Your event " + eventName.getText().toString() + " is now created. Sending out invitations...", Toast.LENGTH_SHORT).show();
                    //addGroups(eventName.getText().toString(), eventStart.getText().toString(), eventEnd.getText().toString(), invitees.getText().toString(), message.getText().toString());
                    //populateGroups();
                    //go back to meetings list
                    Intent intent = new Intent(globalActivity, EventActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "You are not connected to the internet. Get connected and try again!", Toast.LENGTH_SHORT).show();
                    System.out.println("Not connected to the internet!");
                }
            }
        });
        ///

        eventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                createBtn.setEnabled(!eventName.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }



    public class TimeStringComparator implements Comparator<String> {
        DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

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

    /*private void populateGroups(){
        ArrayAdapter<Groups> adapter = new GroupsListAdapter();
        GroupsListView.setAdapter(adapter);
    }
    private void addGroups(String eventName, String eventStart, String eventEnd, String invitees, String message){
        Groups.add(new Groups(eventName, eventStart, eventEnd, invitees, message));
    }

    private class GroupsListAdapter extends ArrayAdapter<Groups>{
        public GroupsListAdapter(){
            super (MainActivity.this, R.layout.goups_listview_item, Groups);
        }

        @Override
        public View getView(int position, View view, ViewGroup Parent){
            if (view==null)
                view = getLayoutInflater().inflate(R.layout.goups_listview_item, Parent, false);
            Groups currentGroup = Groups.get(position);
            TextView name = (TextView) view.findViewById(R.id.eventName);
            name.setText(currentGroup.getName());
            TextView members =  (TextView) view.findViewById(R.id.members);
            members.setText(currentGroup.getInvitees());
            TextView date =  (TextView) view.findViewById(R.id.date);
            date.setText(currentGroup.getStart().toString());

            return view;
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    private final int PICK_CONTACT = 1;

    //Load contacts list
    public void callContacts(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_CONTACT){
            if (resultCode == ActionBarActivity.RESULT_OK){
                Uri contactData = data.getData();
                String id = contactData.getLastPathSegment();
                Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,  null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[] { id }, null);
                //Cursor c = getContentResolver().query(contactData, PROJECTION, filter, null, order);
                int nameId = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                int emailIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                // let's just get the first email
                if (c.moveToFirst()) {
                    String email = c.getString(emailIdx);
                    String name = c.getString(nameId);
                    showSelectedNumber(name, email);

                /*if (c.moveToFirst()){
                    do {
                        //String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        String name = c.getString(1);
                        String email = c.getString(3);
                        //Toast.makeText(this, "You picked" + name, Toast.LENGTH_LONG).show();
                        showSelectedNumber(name, email);
                    }while (c.moveToNext());*/
                }
            }
        }
    }

    public void showSelectedNumber(String name, String email) {
        if(invitees != null && invitees.getText().toString().length()==0){
            invitees.setText(name + "<" +email + ">");
            selectedEmails+=email+",";
        }
        else{
            if(invitees != null) {
                invitees.append(name + "<" +email + ">"+", ");
                selectedEmails+=email+",";
            }
        }


    }


    //NETWORK ACTIVITY
    class PostEventServer extends AsyncTask<String, Void, String[]> {

        private Exception exception;

        protected String[] doInBackground(String... params) {
            //SAAJAN
            HttpURLConnection urlConnection = null;
            try {
                final String FORECAST_BASE_URL =
                        Utility.BASE_URL+"/api/events/";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                // rectify date formats for server
                String start_date = params[5].toString();
                String end_date = params[6].toString();
                String decide_by_date = params[4].toString();
                DateTimeFormatter inputFormat = DateTimeFormat.forPattern("MM/dd/yyyy");
                DateTimeFormatter ISOoutputFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                String start_date_rectified = ISOoutputFormat.print(inputFormat.parseDateTime(start_date));
                String end_date_rectified = ISOoutputFormat.print(inputFormat.parseDateTime(end_date));
                String decide_by_date_rectified = ISOoutputFormat.print(inputFormat.parseDateTime(decide_by_date));


                nameValuePairs.add(new BasicNameValuePair("name", params[0].toString()));
                nameValuePairs.add(new BasicNameValuePair("description", params[1].toString()));
                nameValuePairs.add(new BasicNameValuePair("duration", params[2].toString()));
                nameValuePairs.add(new BasicNameValuePair("creator_email", params[3].toString()));
                nameValuePairs.add(new BasicNameValuePair("decide_by_date", decide_by_date_rectified));
                nameValuePairs.add(new BasicNameValuePair("start_date", start_date_rectified));
                nameValuePairs.add(new BasicNameValuePair("end_date", end_date_rectified));
                nameValuePairs.add(new BasicNameValuePair("invitee", params[7].toString()));
                nameValuePairs.add(new BasicNameValuePair("creator_name", params[8].toString()));
                nameValuePairs.add(new BasicNameValuePair("timeSlots", params[9].toString()));
                nameValuePairs.add(new BasicNameValuePair("status", "new"));

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(FORECAST_BASE_URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);

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

