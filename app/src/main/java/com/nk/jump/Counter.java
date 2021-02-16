package com.nk.jump;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

public class Counter implements SensorEventListener {
    public interface JumpListener {
        void onJump(int total);
    }

    public interface DataPointListener {
        void onNewDataPoint(int axis, float value);
    }

    public final static int AXES = 3;              // Amount of tracked axes
    public final static int ROLLOVER = 60;         // Amount of graphed points at one time
    private final static float THRESHOLD_UP = 11.1f;    // Required up-acceleration threshold // TODO: add big jump modes? (up14; down6) & duration
    private final static float THRESHOLD_DOWN = 8.0f;   // Required down-acceleration threshold
    private final static long THRESHOLD_JUMPTIME_MS = 170;  // Maximum allowed duration between jump up and land
    private final static int SMOOTH_AMOUNT = ROLLOVER / 10; // Rolling average frames

    private Activity mActivity;
    private boolean mStarted = false;
    private Workout mWorkout;

    private float[][] mValues;
    private float[][] mAvg;
    private int mFrame = 0;
    private int mMajorAxis = 0;

    private long mJumpStartMs = 0;
    private boolean mJumpedUp = false;

    private JumpListener mJumpListener;
    private DataPointListener mDataPointListener;

    public Counter(Activity activity){
        mActivity = activity;
    }

    public void setOnJumpListener(JumpListener listener){
        mJumpListener = listener;
    }

    public void setDataPointListener(DataPointListener listener){
        mDataPointListener = listener;
    }

    private void cleanup(){
        mFrame = 0;

        mValues = new float[AXES][ROLLOVER];
        mAvg = new float[AXES][ROLLOVER];

        mWorkout = new Workout();
        if (mJumpListener != null) { mJumpListener.onJump(mWorkout.mJumps); }
    }

    public void start(){
        if (mStarted) { return; }
        mStarted = true;

        cleanup();
        mWorkout.mStart = System.currentTimeMillis();

        final SensorManager sensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    public void stop(){
        if (!mStarted) { return; }
        mStarted = false;

        final SensorManager sensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);

        if (mWorkout.mJumps > 0){
            mWorkout.mEnd = System.currentTimeMillis();
            Logger.logWorkout(mActivity, mWorkout);
        }
    }

    public boolean isStarted(){
        return mStarted;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < AXES; ++i) {
            mValues[i][mFrame] = Math.abs(event.values[i]);
            mAvg[i][mFrame] = smoothedValue(mFrame, mValues[i], mValues[i][mFrame]);
        }

        if (mDataPointListener != null) {
            for (int i = 0; i < AXES; ++i) {
                mDataPointListener.onNewDataPoint(i, mAvg[i][mFrame]);
            }
        }

        if (mAvg[mMajorAxis][mFrame] > THRESHOLD_UP){
            mJumpStartMs = System.currentTimeMillis();
            mJumpedUp = true;
        }
        else if (mJumpedUp && mAvg[mMajorAxis][mFrame] < THRESHOLD_DOWN){
            final long now = System.currentTimeMillis();
            if (now - mJumpStartMs < THRESHOLD_JUMPTIME_MS){
                mWorkout.mJumps++;
                if (mJumpListener != null) { mJumpListener.onJump(mWorkout.mJumps); }
            }
            mJumpedUp = false;
        }

        mMajorAxis = maxAtIndex(mAvg, mFrame);
        mFrame = (mFrame + 1) % ROLLOVER;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

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
}
