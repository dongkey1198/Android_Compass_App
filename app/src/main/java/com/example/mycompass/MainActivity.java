package com.example.mycompass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView compass_spit;
    private TextView orientation;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currenctAzimuth = 0f;
    private SensorManager mSensorManager;

    private Sensor accSensor;
    private Sensor megSensor;

    // 좌표 측정을위한 변수
    private TextView address, lat_lon;
    // Google's API for Location Service
    FusedLocationProviderClient fusedLocationProviderClient;
    // Location Requiest is a config file for all setting related to FUsedLocationProvicerClient.
    LocationRequest locationRequest;
    //LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass_spit = (ImageView)findViewById(R.id.compass_spit);
        orientation = (TextView)findViewById(R.id.orientation);
        lat_lon = (TextView)findViewById(R.id.lat_lon);
        address = (TextView)findViewById(R.id.location);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        megSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //==============Codes for GPS Tracker================
        // Set all properties of LocationRequest
        locationRequest = new LocationRequest();
        // How often does the default location check occur
        locationRequest.setInterval(1000 * 30);
        // How often does the location check occur when set to most frequent update
        locationRequest.setFastestInterval(1000 * 5);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();

    }//end of OnCreate


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

    //현재의 위치를 측정하여 주소를 가져오는 코드
    //위치를 측정하는 센서사용을 허용해줘야만 사용가능하다.
    private  void updateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    lat_lon.setText("위도: " + location.getLatitude() + ", 경도: " + location.getLongitude());

                    Geocoder geocoder = new Geocoder(MainActivity.this);

                    try{
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                        address.setText(addresses.get(0).getAddressLine(0));
                    } catch (Exception e){
                        address.setText("주소를 찾을수 없습니다.");
                    }

                }
            });
        }
        else{
            //permissions not granted
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this, "This app requires permission to be granted in order to work propely", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

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