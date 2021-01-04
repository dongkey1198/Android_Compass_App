package com.example.mycompass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class Compass implements SensorEventListener {

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;

    private SensorManager mSensorManager;

    private Sensor accSensor;
    private Sensor megSensor;

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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
