package com.zazulabs.apoorva.smart_scheduler;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saajan on 4/10/15.
 */
public class Utility {

    public static String BASE_URL = "http://szheng60.pythonanywhere.com";

    // time slot functions
    static String getISO8601StringForDate(DateTime date) {
        DateTimeFormatter ISO8601DATEFORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return ISO8601DATEFORMAT.print(date);
    }


    static ArrayList<String> getTimeSlots(String eventStart, String eventEnd, String duration, List<String> startDates, List<String> endDates){

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm:ss aa");

        DateTime eventStartDate = formatter.parseDateTime(eventStart);
        DateTime eventEndDate = formatter.parseDateTime(eventEnd);

        // slots within duration
        ArrayList<DateTime> availableStartTimes = new ArrayList<DateTime>();
        ArrayList<DateTime> availableEndTimes = new ArrayList<DateTime>();

        for (int i=0; i<startDates.size(); i++){
            DateTime curr_start_date = formatter.parseDateTime(startDates.get(i));
            DateTime curr_end_date = formatter.parseDateTime(endDates.get(i));

            // eliminate nulls
            if (curr_start_date.compareTo(formatter.parseDateTime("31/12/1969 07:00:00 PM")) != 0 &&
                    curr_end_date.compareTo(formatter.parseDateTime("31/12/1969 07:00:00 PM")) != 0	){

                // add slots within duration
                if ((curr_start_date.getMillis()>eventStartDate.getMillis()) && curr_end_date.getMillis()<eventEndDate.getMillis()){
                    availableStartTimes.add(curr_start_date);
                    availableEndTimes.add(curr_end_date);
                    System.out.println("Slots within range: "+ formatter.print(curr_start_date) + formatter.print(curr_end_date));
                }
            }
        }

        ArrayList<String> timeSlotsReturnList = new ArrayList<String>();
        // function
        ArrayList<DateTime> startTimeSlots = generateTimeSlots(eventStartDate, eventEndDate, duration, availableStartTimes, availableEndTimes);

        // convert to ISO8601
        for (int i=0; i< startTimeSlots.size(); i++){
            timeSlotsReturnList.add(getISO8601StringForDate(startTimeSlots.get(i)));
            System.out.println(getISO8601StringForDate(startTimeSlots.get(i)));
        }

        return timeSlotsReturnList;
    }


    static ArrayList<DateTime> generateTimeSlots(DateTime eventStartDate, DateTime eventEndDate,
                                                 String duration, ArrayList<DateTime> availableStartTimes, ArrayList<DateTime> availableEndTimes){

        ArrayList<DateTime> returnStartTimeSlots = new ArrayList<DateTime>();
        DateTimeFormatter fullFormatter = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm:ss aa");
        DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("hh:mm:ss aa");
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy hh");

        String currentStartDateString = dateFormatter.print(eventStartDate);
        currentStartDateString += ":00:00 AM";

        DateTime startHour = hourFormatter.parseDateTime(hourFormatter.print(eventStartDate));
        DateTime endHour = hourFormatter.parseDateTime(hourFormatter.print(eventEndDate));


        // half hour time slots (end time has duration added)
        for (DateTime currentStartSlot = fullFormatter.parseDateTime(currentStartDateString);
             currentStartSlot.compareTo(eventEndDate) == -1; currentStartSlot = currentStartSlot.plusMinutes(30)){

            DateTime currentStartHour = hourFormatter.parseDateTime(hourFormatter.print(currentStartSlot));

            if (currentStartHour.compareTo(startHour) == -1 || currentStartHour.compareTo(endHour) == 1){
                continue;
            }

            //       currentStartSlot
            DateTime currentEndSlot = currentStartSlot.plusMinutes(Integer.parseInt(duration));

            int flag = 0;
            for (int i=0; i<availableStartTimes.size(); i++){// calendar time slots
                DateTime availableStartTimeCurrent = availableStartTimes.get(i);
                DateTime availableEndTimeCurrent = availableEndTimes.get(i);

                Interval interval = new Interval(availableStartTimeCurrent, availableEndTimeCurrent);

                if ( (interval.contains(currentStartSlot) || interval.contains(currentEndSlot))){
                    flag = 1;
                }
            }
            if (flag == 0){
                returnStartTimeSlots.add(currentStartSlot);
                //System.out.println(fullFormatter.print(currentStartSlot));
            }
        }

        return returnStartTimeSlots;
    }

}
