package com.themiezz.lms.helper_classes;

import com.themiezz.lms.user_defined_classes.Application;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Helper {
    private static final String TAG = "ZQ";

    private static boolean isInRange(int number, int from, int to) {
        if (number == from || number == to) return true;
        if (number > from && number < to) return true;
        return false;
    }

    public static int DateToInt(String date) {
        // Returns in yyyyMMdd format
        int nDate;
        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(3, 5));
        int year = Integer.parseInt(date.substring(6));
        nDate = (((year * 100) + month) * 100 + day);
        return nDate;
    }

    public static int getDays(String fromDate, String toDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date from, to;
        try {
            from = format.parse(fromDate);
            to = format.parse(toDate);
        } catch (Exception e) {
            return 0;
        }
        if (from == null || to == null)
            return 0;
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance();
        toCal.setTime(to);
        int workDays = 0;
        while (fromCal.getTimeInMillis() <= toCal.getTimeInMillis()) {
            if (fromCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && fromCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                workDays++;
            }
            fromCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return workDays;
    }

    public static boolean isHoliday(String date) {
        SimpleDateFormat defFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        Date tempDate = null;
        try {
            tempDate = defFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (tempDate == null)
            return true;
        String day = dayFormat.format(tempDate);
        return day.equals("Sat") || day.equals("Sun");
    }

    public static boolean isBetween(String strFromDate, String strToDate, ArrayList<Application> applications) {
        String currDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        int fromDate = DateToInt(strFromDate), toDate = DateToInt(strToDate);
        for (Application application : applications) {
            String status = application.getStatus();
            if (status.equals("Declined")) continue;

            if (application.getStatus().equals("Cancelled") && DateToInt(application.getToDate()) >= DateToInt(currDate))
                continue;
            int from = DateToInt(application.getFromDate());
            int to = DateToInt(application.getToDate());
            if (isInRange(fromDate, from, to) || isInRange(toDate, from, to)) return true;
            if (isInRange(from, fromDate, toDate) || isInRange(to, fromDate, toDate)) return true;
        }
        return false;
    }

}
