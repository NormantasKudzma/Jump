package com.nk.jump.fragments;

import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nk.jump.Counter;
import com.nk.jump.Graph;
import com.nk.jump.R;

import java.util.Locale;

public class WorkoutTab extends Fragment {
    private Counter mCounter;
    private TextView mText;
    private Graph mGraph;

    public WorkoutTab() {
        super(R.layout.tab_workout);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCounter = new Counter(getActivity());

        mText = getView().findViewById(R.id.text_jumps);
        mCounter.setOnJumpListener(total -> {
            mText.setText(String.format(Locale.getDefault(), "%d", total));
        });

        /*mGraph = new Graph(getView().findViewById(R.id.chart1), Counter.AXES, Counter.ROLLOVER);
        mCounter.setDataPointListener((axis, value) -> {
            mGraph.addPoint(axis, value);
        });*/

        Button button = getView().findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (!mCounter.isStarted()) {
                button.setText(R.string.stop);
                if (mGraph != null) { mGraph.cleanup(); }
                mCounter.start();
            }
            else {
                button.setText(R.string.start);
                mCounter.stop();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCounter.stop();
    }
}
