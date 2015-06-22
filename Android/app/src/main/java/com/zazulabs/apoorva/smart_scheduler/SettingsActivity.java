package com.zazulabs.apoorva.smart_scheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


public class SettingsActivity extends ActionBarActivity {

    public static final String PREFRENCES_NAME = "myprefrences";
    public static SharedPreferences settings;
    public static EditText startTimePreferenceView, endTimePreferenceView;
    public static String startHour, startMinute, startSecond,
                            endHour, endMinute, endSecond, startTimeString, endTimeString;
    private TimePicker timePickerStart, timePickerEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getSharedPreferences(
                PREFRENCES_NAME, Context.MODE_PRIVATE);//user storage

        timePickerStart = (TimePicker) findViewById(R.id.startTimePreference);
        timePickerEnd = (TimePicker) findViewById(R.id.endTimePreference);

        String startTimePreference = settings.getString("starttimepreference", "");
        String endTimePreference = settings.getString("endtimepreference", "");

        if (!(startTimePreference.equals("") && endTimePreference.equals(""))) {
            startHour = startTimePreference.substring(1, 3);
            startMinute = startTimePreference.substring(4, 6);
            endHour = endTimePreference.substring(1, 3);
            endMinute = endTimePreference.substring(4, 6);
        }
        else{
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            startHour = Integer.toString(hour);
            startMinute = Integer.toString(min);
            endHour = Integer.toString(hour);
            endMinute = Integer.toString(min);

        }

        timePickerStart.setCurrentHour(Integer.parseInt(startHour));
        timePickerStart.setCurrentMinute(Integer.parseInt(startMinute));
        timePickerEnd.setCurrentHour(Integer.parseInt(endHour));
        timePickerEnd.setCurrentMinute(Integer.parseInt(endMinute));

    }


    public void setUserPreferences(View view) {

        startHour = timePickerStart.getCurrentHour().toString();
        startMinute = timePickerStart.getCurrentMinute().toString();
        endHour = timePickerEnd.getCurrentHour().toString();
        endMinute = timePickerEnd.getCurrentMinute().toString();

        startTimeString = get12HourTime(Integer.parseInt(startHour), Integer.parseInt(startMinute));
        endTimeString = get12HourTime(Integer.parseInt(endHour), Integer.parseInt(endMinute));


        settings.edit().putString("starttimepreference", startTimeString) //update prefs
                .putString("endtimepreference", endTimeString).commit();

        System.out.println("Start Time:"+startTimeString);
        System.out.println("End Time:"+endTimeString);

        Toast.makeText(this, "Settings Updated", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, EventActivity.class);//Logout and redirect to registration
        startActivity(intent);

    }

    public String get12HourTime (Integer hour, Integer min){
        String format, returnTimeString;
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        returnTimeString = " "+pad(hour)+":"+pad(min)+":00 "+format;

        return returnTimeString;

    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
