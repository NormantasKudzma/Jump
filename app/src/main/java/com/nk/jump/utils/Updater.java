package com.nk.jump.utils;

import java.util.Timer;
import java.util.TimerTask;

public class Updater {
    public interface Callback {
        void onUpdate();
    }

    private Callback mCallback;
    private int mIntervalMs;
    private boolean mRunning;
    private Timer mTimer;

    public Updater(Callback callback, int intervalMs){
        mCallback = callback;
        mIntervalMs = intervalMs;
        mRunning = false;
    }

    public void start(){
        if (mRunning) { return; }
        mRunning = true;

        mTimer = new Timer(true);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mCallback.onUpdate();
            }
        }, 0, mIntervalMs);
    }

    public void stop(){
        if (!mRunning) { return; }
        mRunning = false;

        mTimer.cancel();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }
}
