package com.example.ronay.androidgyromovedetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.Matrix;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.UnknownHostException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private UdpClient udpClient;
    private VelocityCalculator velocityCalculator;

    private Calendar c;

    private long nowInMilliseconds;
    private long lastCalculated;
    private long lastSend = 0;


    private SensorManager mSensorManager;
    private Sensor mAccelerationSensor;
    private Sensor mLinearAccelerationSensor;
    private Sensor mGravitySensor;
    private Sensor mMagneticSensor;
    private float[] prevVector = new float[3];

    private float[] mInputsAcceleration = new float[4];
    private float[] mInputsGravity = new float[4];
    private float[] mInputsMagnetic = new float[4];
    private float[] sumsVector = new float[4];

    private boolean mInputsAccelerationInitialized;
    private boolean mInputsGravityInitialized;
    private boolean mInputsMagneticInitialized;

    private TextView mArray[];
    private EditText ipInput;
    private ToggleButton shouldRun;
    private Button startGame;
    private GridLayout grid;

    private long delayTime = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        c = Calendar.getInstance();
        lastCalculated = c.getTimeInMillis();

        for (int i=0; i<sumsVector.length; i++)
        {
            sumsVector[i]= 0;
        }
        try {
            udpClient = new UdpClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        velocityCalculator = new VelocityCalculator();

        setContentView(R.layout.activity_main);

        mArray = new TextView[6];
        mArray[0] = (TextView)findViewById(R.id.m1);
        mArray[1] = (TextView)findViewById(R.id.m2);
        mArray[2] = (TextView)findViewById(R.id.m3);
        mArray[3] = (TextView)findViewById(R.id.m4);
        mArray[4] = (TextView)findViewById(R.id.m5);
        mArray[5] = (TextView)findViewById(R.id.m6);
        ipInput = (EditText) findViewById(R.id.input_id);
        shouldRun = (ToggleButton)findViewById(R.id.should_run);
        startGame = (Button)findViewById(R.id.start_game);
        grid = (GridLayout) findViewById(R.id.grid1);

        startGame.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                udpClient.SendCommand(2);
            }
        });

        startGame.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (grid.getVisibility() == View.VISIBLE) {
                    grid.setVisibility(View.INVISIBLE);
                }
                else {
                    grid.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        mSensorManager.registerListener(this,mAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mGravitySensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mLinearAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);

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
            System.arraycopy(event.values, 0, mInputsAcceleration, 0, 3);
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
            float[] smoothedVector = new float[4];
            SensorManager.getRotationMatrix(rotationMatrix, irrelevant, mInputsGravity, mInputsMagnetic);
            Matrix.invertM(invertedRotationMatrix, 0, rotationMatrix, 0);

            Matrix.multiplyMV(trueGravityVector, 0, invertedRotationMatrix, 0, mInputsGravity, 0);
            Matrix.multiplyMV(trueAccelerationVector, 0, invertedRotationMatrix, 0, mInputsAcceleration, 0);

            velocityCalculator.add_normalized(trueAccelerationVector);
            if(shouldRun.isChecked())
            {
                try {
                    udpClient.updateIp(ipInput.getText().toString());
                }
                catch (UnknownHostException e)
                {

                }
            }
            mArray[0].setText(String.format("V[0] Is :%f", velocityCalculator.Velocities[0]));
            mArray[1].setText(String.format("V[1] Is :%f", velocityCalculator.Velocities[1]));
            mArray[2].setText(String.format("V[2] Is :%f", velocityCalculator.Velocities[2]));

            mArray[3].setText(String.format("A[0] Is :%f", trueAccelerationVector[0]));
            mArray[4].setText(String.format("A[1] Is :%f", trueAccelerationVector[1]));
            mArray[5].setText(String.format("A[2] Is :%f", trueAccelerationVector[2]));

            udpClient.Message = new float[6];
            System.arraycopy(trueAccelerationVector, 0, smoothedVector, 0, 3);
            lowPass(prevVector, smoothedVector);
            System.arraycopy(smoothedVector, 0, udpClient.Message, 0, 3);
            System.arraycopy(velocityCalculator.Velocities,0,udpClient.Message,3,3);
            c = Calendar.getInstance();
            nowInMilliseconds = c.getTimeInMillis();


            //send twenty times Per Second
            
            if(nowInMilliseconds - lastSend >= delayTime && shouldRun.isChecked()) {
                lastSend = nowInMilliseconds;
                udpClient.SendMessage();
            }

            System.arraycopy(trueAccelerationVector,0,prevVector,0,3);
        }
    }

    private void updateSumsVector(float[] vector) {
        for (int i=0; i<sumsVector.length; i++) {
            long diff = nowInMilliseconds - lastCalculated;
            if(diff == 0){
                diff = 1;
            }
            sumsVector[i] += diff * vector[i];
            lastCalculated = nowInMilliseconds;
        }
    }

    static final float ALPHA = 0.15f;

    protected float[] lowPass( float[] input, float[] output ) {

        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {

            output[i] = output[i] + ALPHA * (input[i] - output[i]);

        }

        return output;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
}
