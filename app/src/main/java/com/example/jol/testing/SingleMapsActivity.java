package com.example.jol.testing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class SingleMapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener, IBaseGpsListener {

    private GoogleMap mMap;
    private TextView xText, yText, zText, tvDistance;
    private Sensor SensorKu;
    private SensorManager SM;
    private Button btStart;
    private String nextState = "start", X, Y, Z;
    Chronometer mChronometer;
    int timerCount = 1;
    String strUnits = "miles/hour";
    String strCurrentSpeed, strSpeed;
    LocationManager locationManager;
    double longitude = 0, latitude = 0;
    private ArrayList<ModelSensor> dataSensor = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;
    private BottomSheetBehavior bottomSheetBehavior;
    private Polyline gpsTrack;
    private List<LatLng> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_maps);

        // membuat sensor
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sensor accelerometer
        SensorKu = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // menambahkan textViewnya
        xText = findViewById(R.id.xText);
        yText = findViewById(R.id.yText);
        zText = findViewById(R.id.zText);
        tvDistance = findViewById(R.id.txtDistance);

        tvDistance.setVisibility(View.GONE);

        // menghubungkan view button dengan controllernya
        btStart = findViewById(R.id.btnStart);
        mChronometer = findViewById(R.id.chronometer);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //action ketika button start diklik
        btStart.setOnClickListener(view -> {
            if (nextState.equals("start")) {
                btStart.setText("stop");
                initLocation();
                timerCount = 1;
                nextState = "stop";
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                //Jalankan sensor ketika tombol start diklik
                dataSensor = new ArrayList<>();
                SM.registerListener(SingleMapsActivity.this, SensorKu, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(SingleMapsActivity.this, "Sensor Running..!", Toast.LENGTH_SHORT).show();

            } else if (nextState.equals("stop")) {
                btStart.setText("start");
                nextState = "start";
                timerCount = 1;
                mChronometer.stop();
                //Stop Sensor saat tombol stop diklik
                SM.unregisterListener(SingleMapsActivity.this, SensorKu);
                locationManager.removeUpdates(SingleMapsActivity.this);
                Toast.makeText(SingleMapsActivity.this, "Sensor Stopped...!", Toast.LENGTH_SHORT).show();
                tvDistance.setText("Distance = " + String.valueOf(getDistancBetweenTwoPoints()) + " meter");
                tvDistance.setVisibility(View.VISIBLE);
                mMap.addMarker(new MarkerOptions().position(points.get(0)).title("Start Position"));
                mMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1)).title("Stop Position"));
            }
        });

        mChronometer.setOnChronometerTickListener(chronometer -> {
            long timeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
            int hours = (int) (timeElapsed / 3600000);
            int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
            int second = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;
            System.out.println("second : " + (second + 1));
            timerCount = second + 1;
        });

        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            //get Location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
            updateSpeed(null);
        }
    }

    private boolean useMetricUnits() {
        // return true will make the speed in meters/second, false in miles/hour
        return true;
    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

//        If you want to convert Meters/Second to kmph-1 then you need to multipl the Meters/Second answer from 3.6
//
//        Speed from kmph-1 = 3.6 * (Speed from ms-1)

        if (location != null) {
            location.setUseMetricunits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("speed", String.valueOf(nCurrentSpeed));
            Log.d("latlng", latitude + ", " + longitude);
            if (dataSensor.get(dataSensor.size() - 1).getLatitude() != 0) {

            }
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        if (this.useMetricUnits()) {
            strUnits = "meters/second";
        }
        strSpeed = strCurrentSpeed + " " + strUnits;
        TextView txtCurrentSpeed = this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strSpeed);
    }

    private String getTimeRecord() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }

    private float getDistancBetweenTwoPoints() {
        double lat1 = dataSensor.get(0).getLatitude();
        double lon1 = dataSensor.get(0).getLongitude();
        double lat2 = dataSensor.get(dataSensor.size() - 1).getLatitude();
        double lon2 = dataSensor.get(dataSensor.size() - 1).getLongitude();

        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1,
                lat2, lon2, distance);

        return distance[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Failed To init Location, please Restart the Application !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mMap = googleMap;

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(4);
        gpsTrack = mMap.addPolyline(polylineOptions);

        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        X = String.valueOf(event.values[0]);
        Y = String.valueOf(event.values[1]);
        Z = String.valueOf(event.values[2]);
        xText.setText("X = " + X);
        yText.setText("Y = " + Y);
        zText.setText("Z = " + Z);
        if (dataSensor.size() < (timerCount * 10)) {
            dataSensor.add(new ModelSensor(getTimeRecord(), X, Y, Z, strCurrentSpeed, latitude, longitude));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            points = gpsTrack.getPoints();
            points.add(new LatLng(location.getLatitude(), location.getLongitude()));
            gpsTrack.setPoints(points);
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
