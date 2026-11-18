package com.example.trainerlink.activity;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.trainerlink.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity extends FragmentActivity implements OnMapReadyCallback {

    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng location = new LatLng(lat, lng);

        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Training Location"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
    }
}