package com.sds.driver.ui.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AppUtility {

    private static Calendar calendar=Calendar.getInstance();;

    public static Date getDutyReportingDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date dt = sdf.parse(dateStr);
            return dt;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date getDutyReportingTime(String dutytime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date dt=  sdf.parse(dutytime);
            return dt;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String formatReportingDateTime(Date reportingDate, Date reportingTime) {
        String formattedDateTime=null;
        try {
            if (reportingDate != null && reportingTime != null) {
                // Combine reportingDate and reportingTime into one Calendar
                Calendar combinedCalendar = Calendar.getInstance();
                combinedCalendar.setTime(reportingDate);

                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(reportingTime);

                combinedCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                combinedCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                combinedCalendar.set(Calendar.SECOND, 0); // Optional: reset seconds

                // Now format combinedCalendar
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                formattedDateTime = sdf.format(combinedCalendar.getTime());
            }
        } catch (Exception e) {
            return null;
        }
        return formattedDateTime;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatDate(Date selectedDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }


    public static String formatTime(Date selectedDate){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(selectedDate.getTime());
    }

    public static Date getCurrentDateTime(){
        return Calendar.getInstance().getTime(); // get today
    }

    public static String formatDateTime(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formatter.format(date);
    }

    private static final Random random = new Random();

    public static String generatePin(){
        Integer pin = 1000 + random.nextInt(9000);
        return pin.toString();
    }

    public static String removeEmojis(String text) {
        return text.replaceAll("[^\\p{ASCII}\\p{L}\\p{N}\\p{P}\\p{Z}\\s]", "");
    }



}
