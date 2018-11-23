package com.example.jol.testing;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ModelSensor> dataSensor = new ArrayList<>();
    double firstLng = 0, firstLat = 0, lastLng = 0, lastLat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        dataSensor = getIntent().getParcelableArrayListExtra("data");

        //dapatin posisi latlng pertama kali saat start di tekan (yg nilai latlng nya tidak sama dengan 0)
        for (ModelSensor sensor : dataSensor) {
            if (sensor.getLatitude() != 0 && sensor.getLongitude() != 0) {
                firstLat = sensor.getLatitude();
                firstLng = sensor.getLongitude();
                break;
            }
        }

        //dapatin latlng terakhir (posisi terakhir saat stop ditekan
        lastLat = dataSensor.get(dataSensor.size() - 1).getLatitude();
        lastLng = dataSensor.get(dataSensor.size() - 1).getLongitude();

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

        // Add a marker in start and stop position
        LatLng start = new LatLng(firstLat, firstLng);
        LatLng stop = new LatLng(lastLat, lastLng);
        mMap.addMarker(new MarkerOptions().position(start).title("Start Position"));
        mMap.addMarker(new MarkerOptions().position(stop).title("Stop Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

        // Add a thin red line from start to stop.
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(start, stop)
                .width(5)
                .color(Color.RED));
    }
}
