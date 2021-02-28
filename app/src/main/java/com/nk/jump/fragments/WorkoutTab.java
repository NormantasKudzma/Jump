package com.nk.jump.fragments;

import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nk.jump.R;
import com.nk.jump.utils.Counter;
import com.nk.jump.utils.Timestamp;
import com.nk.jump.utils.Updater;

import java.util.Locale;

public class WorkoutTab extends Fragment {
    private Counter mCounter;
    private TextView mTextJumps;
    private TextView mTextDuration;
    private Updater mDurationUpdater;

    public WorkoutTab() {
        super(R.layout.tab_workout);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCounter = new Counter(getActivity());

        mTextJumps = getView().findViewById(R.id.text_jumps);
        mCounter.setOnJumpListener(total -> {
            mTextJumps.setText(String.format(Locale.getDefault(), "%d", total));
        });

        mTextDuration = getView().findViewById(R.id.text_duration);
        mDurationUpdater = new Updater(() -> {
            mTextDuration.post(() -> mTextDuration.setText(Timestamp.formatDuration(System.currentTimeMillis() - mCounter.getWorkout().mStart)));
        }, 1000);

        Button button = getView().findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (!mCounter.isStarted()) {
                button.setText(R.string.stop);
                button.setBackgroundColor(getResources().getColor(R.color.teal_200));
                mCounter.start();
                mDurationUpdater.start();
            }
            else {
                button.setText(R.string.start);
                button.setBackgroundColor(getResources().getColor(R.color.purple_500));
                mCounter.stop();
                mDurationUpdater.stop();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCounter != null) { mCounter.stop(); }
        mCounter = null;

        if (mDurationUpdater != null) { mDurationUpdater.stop(); }
        mDurationUpdater = null;
    }
}
