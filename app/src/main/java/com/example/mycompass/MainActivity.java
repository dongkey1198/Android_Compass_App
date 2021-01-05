package com.example.mycompass;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compass_spit;
    private TextView orientation, location;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currenctAzimuth = 0f;
    private SensorManager mSensorManager;

    private Sensor accSensor;
    private Sensor megSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass_spit = (ImageView)findViewById(R.id.compass_spit);
        orientation = (TextView)findViewById(R.id.orientation);
        location = (TextView)findViewById(R.id.location);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        megSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, megSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpah = 0.97f;

        synchronized (this){
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpah * mGravity[0] + (1-alpah) * event.values[0];
                mGravity[1] = alpah * mGravity[1] + (1-alpah) * event.values[1];
                mGravity[2] = alpah * mGravity[2] + (1-alpah) * event.values[2];
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic[0] = alpah * mGeomagnetic[0] + (1-alpah) * event.values[0];
                mGeomagnetic[1] = alpah * mGeomagnetic[1] + (1-alpah) * event.values[1];
                mGeomagnetic[2] = alpah * mGeomagnetic[2] + (1-alpah) * event.values[2];
            }

            float[] I = new float[9];
            float[] R = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

            }
        }

        Animation animation = new RotateAnimation(-currenctAzimuth, -azimuth, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        SystemClock.sleep(10);
        currenctAzimuth = azimuth;

        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);


        compass_spit.startAnimation(animation);
        orientation.setText((360 - (int)azimuth) + "°");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}