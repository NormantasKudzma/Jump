package com.nk.jump.utils;

import java.util.Locale;

public class Timestamp {
    private Timestamp(){}

    public static String formatDuration(long durationMs){
        long durationSeconds = durationMs / 1000;
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
