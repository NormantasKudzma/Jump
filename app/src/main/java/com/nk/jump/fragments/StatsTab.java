package com.nk.jump.fragments;

import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;

import com.nk.jump.R;
import com.nk.jump.utils.Logger;
import com.nk.jump.utils.Timestamp;
import com.nk.jump.utils.Workout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class StatsTab extends Fragment {
    private final static long DAY_MS = 86_400_000;

    public StatsTab() {
        super(R.layout.tab_stats);
    }

    @Override
    public void onStart() {
        super.onStart();

        ArrayList<Workout> lifetimeWorkouts = Logger.getAll(Objects.requireNonNull(getActivity()));

        displayStats(getTodaysWorkouts(lifetimeWorkouts), R.id.today_duration, R.id.today_jumps);
        displayStats(getWeeksWorkouts(lifetimeWorkouts), R.id.weekly_duration, R.id.weekly_jumps);
        displayStats(lifetimeWorkouts, R.id.lifetime_duration, R.id.lifetime_jumps);
    }

    private void displayStats(ArrayList<Workout> workouts, @IdRes int duration, @IdRes int jumps){
        try {
            TextView todayDuration = Objects.requireNonNull(getView()).findViewById(duration);
            todayDuration.setText(Timestamp.formatDuration(durationOf(workouts)));
            TextView todayJumps = getView().findViewById(jumps);
            todayJumps.setText(String.format(Locale.getDefault(), "%d", jumpsOf(workouts)));
        }
        catch (Exception ignored) {}
    }

    private long durationOf(ArrayList<Workout> workouts){
        long duration = 0;
        for (Workout w : workouts) {
            duration += (w.mEnd - w.mStart);
        }
        return duration;
    }

    private int jumpsOf(ArrayList<Workout> workouts){
        int jumps = 0;
        for (Workout w : workouts) {
            jumps += w.mJumps;
        }
        return jumps;
    }

    private ArrayList<Workout> getTodaysWorkouts(ArrayList<Workout> workouts){
        final long now = System.currentTimeMillis();
        final long dayStart = (now / DAY_MS) * DAY_MS;
        return filterByTimestamp(workouts, dayStart, now);
    }

    private ArrayList<Workout> getWeeksWorkouts(ArrayList<Workout> workouts){
        final long now = System.currentTimeMillis();
        final long sevenDaysAgo = (now / DAY_MS) * DAY_MS - 6 * DAY_MS;
        return filterByTimestamp(workouts, sevenDaysAgo, now);
    }

    private ArrayList<Workout> filterByTimestamp(ArrayList<Workout> workouts, long from, long to){
        ArrayList<Workout> filtered = new ArrayList<>();
        for (Workout w : workouts) {
            if (w.mStart >= from && w.mEnd <= to) {
                filtered.add(w);
            }
        }
        return filtered;
    }
}
