package com.example.ronay.androidgyromovedetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.Matrix;
import android.widget.TextView;

import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerationSensor;
    private Sensor mLinearAccelerationSensor;
    private Sensor mGravitySensor;
    private Sensor mMagneticSensor;
    private float[] mVelocity = new float[3];

    private float[] mInputsAcceleration = new float[4];
    private float[] mInputsGravity = new float[4];
    private float[] mInputsMagnetic = new float[4];

    private boolean mInputsAccelerationInitialized;
    private boolean mInputsGravityInitialized;
    private boolean mInputsMagneticInitialized;

    private UdpClient udpClient;
    private TextView mArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        try {
            udpClient = new UdpClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        mArray = new TextView[6];
        mArray[0] = (TextView)findViewById(R.id.m1);
        mArray[1] = (TextView)findViewById(R.id.m2);
        mArray[2] = (TextView)findViewById(R.id.m3);
        mArray[3] = (TextView)findViewById(R.id.m4);
        mArray[4] = (TextView)findViewById(R.id.m5);
        mArray[5] = (TextView)findViewById(R.id.m6);

    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mInputsMagneticInitialized = true;
            System.arraycopy(event.values,0,mInputsMagnetic,0,3);
        }
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mInputsAccelerationInitialized = true;
            System.arraycopy(event.values,0,mInputsAcceleration,0,3);
        }
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mInputsGravityInitialized = true;
            System.arraycopy(event.values,0,mInputsGravity,0,3);
        }

        if(mInputsMagneticInitialized && mInputsGravityInitialized && mInputsAccelerationInitialized)
        {
            float[] rotationMatrix = new float[16];
            float[] irrelevant = new float[16];
            float[] invertedRotationMatrix = new float[16];
            float[] trueGravityVector = new float[4];
            float[] trueAccelerationVector = new float[4];

            SensorManager.getRotationMatrix(rotationMatrix, irrelevant, mInputsGravity, mInputsMagnetic);
            Matrix.invertM(invertedRotationMatrix, 0, rotationMatrix, 0);

            Matrix.multiplyMV(trueGravityVector, 0, invertedRotationMatrix, 0, mInputsGravity, 0);
            Matrix.multiplyMV(trueAccelerationVector, 0, invertedRotationMatrix, 0, mInputsAcceleration, 0);
            mArray[0].setText(String.format("trueGravityVector[0] Is :%f", trueGravityVector[0]));
            mArray[1].setText(String.format("trueGravityVector[1] Is :%f", trueGravityVector[1]));
            mArray[2].setText(String.format("trueGravityVector[2] Is :%f", trueGravityVector[2]));

            mArray[3].setText(String.format("trueAccelerationVector[0] Is :%f", trueAccelerationVector[0]));
            mArray[4].setText(String.format("trueAccelerationVector[1] Is :%f", trueAccelerationVector[1]));
            mArray[5].setText(String.format("trueAccelerationVector[2] Is :%f", trueAccelerationVector[2]));

            udpClient.Message = new float[3];
            System.arraycopy(trueAccelerationVector,0,udpClient.Message,0,3);
            udpClient.SendMessage();


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,mAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mGravitySensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mLinearAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
