package com.example.old2gold.shared;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class UtilFunctions {
    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }
}
