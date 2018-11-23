package com.example.jol.testing;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ModelSensor> dataSensor = new ArrayList<>();
    private ArrayList<LatLng> datalatLng = new ArrayList<>();
    PolylineOptions lineOptions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataSensor = getIntent().getParcelableArrayListExtra("data");

        //dapatin posisi latlng
        for (ModelSensor sensor : dataSensor) {
            if (sensor.getLatitude() != 0 && sensor.getLongitude() != 0) {
                datalatLng.add(new LatLng(sensor.getLatitude(), sensor.getLongitude()));
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        lineOptions = new PolylineOptions();
        lineOptions.addAll(datalatLng);
        lineOptions.width(10);
        lineOptions.color(Color.RED);

        if (lineOptions != null) {
            mMap.addPolyline(lineOptions);
            // Add a marker in start and stop position and zoom
            CameraUpdate center = CameraUpdateFactory.newLatLng(datalatLng.get(0));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }

    }

}
