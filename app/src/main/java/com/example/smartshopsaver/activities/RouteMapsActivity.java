package com.example.smartshopsaver.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartshopsaver.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;

import java.util.List;

public class RouteMapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap map;

    private MarkerOptions place1;
    private MarkerOptions place2;
    private MarkerOptions place3;

    Button btnGetDirection, backBtn;
    private Polyline currentPolyline;

    double destLatitude1, destLongitude1;
    double destLatitude2, destLongitude2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_maps);
        btnGetDirection = findViewById(R.id.btnGetDirection);
//        ImageButton backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        destLatitude1 = intent.getDoubleExtra("latitudeR1", 0.00);
        destLongitude1 = intent.getDoubleExtra("longitudeR1", 0.00);

        destLatitude2 = intent.getDoubleExtra("latitudeR2", 0.00);
        destLongitude2 = intent.getDoubleExtra("longitudeR2", 0.00);

        btnGetDirection.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                // Check if we have permission to access the user's location
                if (ContextCompat.checkSelfPermission(RouteMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Request permission to access the user's location
                    ActivityCompat.requestPermissions(RouteMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    // Permission has already been granted, get the user's location and use it as the origin of the directions
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(RouteMapsActivity.this);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(RouteMapsActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        // Use the user's location as the origin of the directions
                                        place1 = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Current Location");
                                        new FetchURL(RouteMapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(),
                                                "driving"), "driving", "#FF0000");
                                        new FetchURL(RouteMapsActivity.this).execute(getUrl(place1.getPosition(), place3.getPosition(),
                                                "driving"), "driving", "#0000FF");

                                    }
                                }
                            });
                }
            }
        });

//        backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });

        place2 = new MarkerOptions().position(new LatLng(destLatitude1, destLongitude1)).title("Destination 1");
        place3 = new MarkerOptions().position(new LatLng(destLatitude2, destLongitude2)).title("Destination 2");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        Log.d("mylog", "Added Markers...");
        if (place1 != null) { // check if place1 is not null before adding it to the map
            map.addMarker(place1);
        }
        map.addMarker(place2);
        map.addMarker(place3);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBlLM7-SsPvYtZ755wuPw2-LRY00DgQxl8";
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
//        if (currentPolyline != null)
//            currentPolyline.remove();
        PolylineOptions polylineOptions = (PolylineOptions) values[0];

        int color = Color.BLUE; // Default color
        if (values.length > 1 && values[1] instanceof String) {
            try {
                color = Color.parseColor((String) values[1]); // Use the provided color value
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // Customize the polylineOptions with the desired color
        polylineOptions.color(color);

        currentPolyline = map.addPolyline((PolylineOptions) values[0]);

        int num = 1;
        int attempt = 1;

        List<LatLng> points = currentPolyline.getPoints();
        double totalDistance = 0;
        double totalDistance2 = 0;
        if (num == 1 && attempt == 1) {
            for (int i = 0; i < points.size() - 1; i++) {
                LatLng p1 = points.get(i);
                LatLng p2 = points.get(i + 1);
                totalDistance += SphericalUtil.computeDistanceBetween(p1, p2);
                totalDistance2 = totalDistance + 2590;
                attempt++;
            }
        } else if (num == 1 && attempt == 2) {
            for (int i = 1; i < points.size() - 1; i++) {
                LatLng p1 = points.get(i);
                LatLng p2 = points.get(i + 1);
                totalDistance2 += SphericalUtil.computeDistanceBetween(p1, p2);
            }
        }
        Log.d("mylog", "Total Distance: " + totalDistance + " meters");
        Log.d("mylog", "Total Distance: " + totalDistance2 + " meters");
        Log.d("mynumlog", "onTaskDone: NUMBER = " + num);
        Log.d("myattemptlog", "onTaskDone: ATTEMPT " + attempt);


        // Update the total distance TextView1
        TextView tvDistance1 = findViewById(R.id.tvDistance1);
        tvDistance1.setText("Destination 1: " + String.format("%.2f", totalDistance / 1000) + " km");

        //Update the total distance TextView2
        TextView tvDistance2 = findViewById(R.id.tvDistance2);
        tvDistance2.setText("Destination 2: " + String.format("%.2f", totalDistance2 / 1000) + " km");

        // Make the trip details layout visible
        LinearLayout layoutTripDetails = findViewById(R.id.layoutTripDetails);
        layoutTripDetails.setVisibility(View.VISIBLE);
    }


}