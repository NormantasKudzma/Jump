package com.nk.jump;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class Graph {
    private final static int[] AXIS_COLORS = { Color.RED, Color.GREEN, Color.BLUE };

    private final int mAxes;
    private final int mRollover;

    private LineChart mChart;
    private ArrayList<Entry>[] mEntries;
    private LineDataSet[] mDataSet;
    private float[] mNext;

    private boolean mRunning = true;
    private final Object mRefreshSync = new Object();

    public Graph(LineChart chart, int axes, int rollover){
        mAxes = axes;
        mRollover = rollover;
        mChart = chart;

        final YAxis yAxis = mChart.getAxisLeft();
        yAxis.setAxisMinimum(0.0f);
        yAxis.setAxisMaximum(20.0f);

        new Thread() {
            @Override
            public void run() {
                try {
                    while (mRunning){
                        synchronized (mRefreshSync){
                            mRefreshSync.wait();
                        }
                        refreshGraph();
                        Thread.sleep(500);
                    }
                }
                catch (Exception ignored){
                    ignored.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mRunning = false;
    }

    public void cleanup() {
        mNext = new float[mAxes];
        mEntries = new ArrayList[mAxes];
        mDataSet = new LineDataSet[mAxes];
        for (int i = 0; i < mAxes; ++i){
            mEntries[i] = new ArrayList<Entry>();
            mDataSet[i] = new LineDataSet(mEntries[i], String.format("Axis%d", i));
            mDataSet[i].setColor(AXIS_COLORS[i]);
            mDataSet[i].setValueTextColor(Color.BLACK);
        }

        refreshGraph();
    }

    public void addPoint(int axis, float value){
        mEntries[axis].add(new Entry(mNext[axis], value));
        mNext[axis]++;

        if (mEntries[axis].size() > mRollover) {
            mEntries[axis].remove(0);
        }

        synchronized (mRefreshSync) {
            mRefreshSync.notify();
        }
    }

    private void refreshGraph(){
        LineData lineData = new LineData();
        for (int i = 0; i < mAxes; ++i){
            mDataSet[i].setValues(mEntries[i]);
            lineData.addDataSet(mDataSet[i]);
        }
        mChart.setData(lineData);
        mChart.postInvalidate();
    }
}
