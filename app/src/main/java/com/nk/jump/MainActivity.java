package com.nk.jump;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static int AXES = 3;              // Amount of tracked axes
    private final static int ROLLOVER = 60;         // Amount of graphed points at one time
    private final static float THRESHOLD_UP = 11.1f;    // Required up-acceleration threshold // TODO: add big jump modes? (up14; down6) & duration
    private final static float THRESHOLD_DOWN = 8.0f;   // Required down-acceleration threshold
    private final static long THRESHOLD_JUMPTIME_MS = 170;  // Maximum allowed duration between jump up and land
    private final static int SMOOTH_AMOUNT = ROLLOVER / 10; // Rolling average frames
    private final static int[] AXIS_COLORS = { Color.RED, Color.GREEN, Color.BLUE };

    private TextView mText;

    private float[][] mValues = new float[AXES][ROLLOVER];
    private float[][] mAvg = new float[AXES][ROLLOVER];
    private int mFrame = 0;
    private int mMajorAxis = 0;

    private int mJumps = 0;
    private long mJumpStartMs = 0;
    private boolean mJumpedUp = false;

    LineChart mChart;
    ArrayList<Entry>[] mEntries = new ArrayList[AXES];
    LineDataSet mDataSet[] = new LineDataSet[AXES];
    float mNext = 0;

    private int maxAtIndex(float[][] values, int index) {
        int maxIndex = 0;
        for (int i = 0; i < values.length; ++i){
            if (values[i][index] > values[maxIndex][index]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private float smoothedValue(int index, float[] values, float newValue){
        float sum = 0;
        for (int i = index - SMOOTH_AMOUNT; i < index; i++){
            int at = i % ROLLOVER;
            if (at < 0) { at += ROLLOVER; }
            sum += values[at];
        }
        return sum / SMOOTH_AMOUNT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = findViewById(R.id.text);
        mChart = findViewById(R.id.chart1);
        final YAxis yAxis = mChart.getAxisLeft();
        yAxis.setAxisMinimum(0.0f);
        yAxis.setAxisMaximum(20.0f);

        for (int i = 0; i < AXES; ++i){
            mEntries[i] = new ArrayList<Entry>();
            mDataSet[i] = new LineDataSet(mEntries[i], String.format("Axis%d", i));
            mDataSet[i].setColor(AXIS_COLORS[i]);
            mDataSet[i].setValueTextColor(Color.BLACK);
        }

        final SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                for (int i = 0; i < AXES; ++i) {
                    mValues[i][mFrame] = Math.abs(event.values[i]);
                    mAvg[i][mFrame] = smoothedValue(mFrame, mValues[i], mValues[i][mFrame]);
                }

                for (int i = 0; i < AXES; ++i) {
                    mEntries[i].add(new Entry(mNext, mAvg[i][mFrame]));
                    if (mNext > ROLLOVER) {
                        mEntries[i].remove(0);
                    }
                }

                if (mAvg[mMajorAxis][mFrame] > THRESHOLD_UP){
                    mJumpStartMs = System.currentTimeMillis();
                    mJumpedUp = true;
                }
                else if (mJumpedUp && mAvg[mMajorAxis][mFrame] < THRESHOLD_DOWN){
                    final long now = System.currentTimeMillis();
                    if (now - mJumpStartMs < THRESHOLD_JUMPTIME_MS){
                        Log.w("accel", String.format("Logged jump with duration %d", now - mJumpStartMs));
                        mJumps++;
                    }
                    mJumpedUp = false;
                }

                mMajorAxis = maxAtIndex(mAvg, mFrame);

                refreshGraph();

                mFrame = (mFrame + 1) % ROLLOVER;
                mNext++;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.w("accel", String.format("sensor accuracy changed: %d", accuracy));
            }
        };

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        ((Button)findViewById(R.id.button)).setOnClickListener(v -> sensorManager.unregisterListener(listener));
    }

    private void refreshGraph(){
        mText.setText(String.format("major axis: %d with avg %2.2f\ntotal jumps: %d\nstart @ %d", mMajorAxis, mAvg[mMajorAxis][mFrame], mJumps, mJumpStartMs));

        LineData lineData = new LineData();
        for (int i = 0; i < AXES; ++i){
            mDataSet[i].setValues(mEntries[i]);
            lineData.addDataSet(mDataSet[i]);
        }
        mChart.setData(lineData);
        mChart.invalidate();
    }
}