package com.example.ronay.androidgyromovedetection;

import java.util.Calendar;

/**
 * Created by ronay on 5/6/2017.
 */
public class VelocityCalculator {
    public float[] Velocities = new float[3];
    public float Threshold = 1;

    private int[] delays = new int[3];
    private int resetTimes = 10;
    private Calendar c;
    private long previousSend;
    public void VelocitiesCalculator()
    {
        for (int i = 0 ; i< Velocities.length; i++)
        {
            Velocities[i] = 0;
        }
        c = Calendar.getInstance();
    }

    public void add_normalized(float[] values) {
        for (int i = 0 ; i< Velocities.length; i++) {
            if( Math.abs(values[i]) > Threshold)
            {
                Velocities[i] += values[i];
            }
            else
            {
                delays[i]++;
                if(delays[i] >= resetTimes)
                {
                    delays[i] = 0;
                    Velocities[i] = 0;
                }
            }
        }
    }
}
