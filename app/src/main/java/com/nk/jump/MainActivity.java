package com.nk.jump;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Counter mCounter;
    private TextView mText;
    private Graph mGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCounter = new Counter(this);

        mText = findViewById(R.id.text);
        mCounter.setOnJumpListener(total -> {
            mText.setText(String.format("Total jumps %d", total));
        });

        mGraph = new Graph(findViewById(R.id.chart1), Counter.AXES, Counter.ROLLOVER);
        mCounter.setDataPointListener((axis, value) -> {
            mGraph.addPoint(axis, value);
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (!mCounter.isStarted()) {
                button.setText(R.string.stop);
                mGraph.cleanup();
                mCounter.start();
            }
            else {
                button.setText(R.string.start);
                mCounter.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCounter.stop();
        super.onDestroy();
    }
}
