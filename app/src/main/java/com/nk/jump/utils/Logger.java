package com.nk.jump.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

public class Logger {
    private static final String KEY_WORKOUTS = "workouts";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final String KEY_JUMPS = "jumps";

    public static void logWorkout(Activity activity, Workout workout){
        SharedPreferences prefs = activity.getSharedPreferences(KEY_WORKOUTS, Context.MODE_PRIVATE);

        JSONArray entries;
        try {
            entries = new JSONArray(prefs.getString(KEY_WORKOUTS, ""));
        }
        catch (Exception ignored) {
            entries = new JSONArray();
        }

        try {
            JSONObject json = new JSONObject();
            json.put(KEY_START, workout.mStart);
            json.put(KEY_END, workout.mEnd);
            json.put(KEY_JUMPS, workout.mJumps);
            entries.put(json.toString());
            prefs.edit().putString(KEY_WORKOUTS, entries.toString()).apply();
        }
        catch (Exception ignored) { }
    }
}
