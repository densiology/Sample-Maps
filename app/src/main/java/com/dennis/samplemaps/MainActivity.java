package com.dennis.samplemaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dennis.samplemaps.models.Leg;
import com.dennis.samplemaps.models.Step;
import com.dennis.samplemaps.utils.AppConstants;
import com.dennis.samplemaps.utils.GpsUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback{

    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    private Marker p1marker;
    private Button btnGetDirection;
    private Polyline currentPolyline;
    private List<Step> allSteps;
    private List<LatLng> latlngPolyline;
    private MapFragment mapFragment;
    private boolean isFirstTime = true;
    private boolean isAutoApiCallEnabled;

    private TextView tvLocation;
    private TextView tvBegin;

    // location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isGPS = false;
    private LatLng currentLoc;

    private static final long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private static final long FASTEST_INTERVAL = 3000; /* 3 sec */
    private static final double DEFAUULT_TOLERANCE = 20;    /* 20 meters */
    private static final double MIN_TOLERANCE = 0.1;    /* 0.1 meters */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = findViewById(R.id.txtGetDirection);
        tvBegin = findViewById(R.id.txtBegin);
        btnGetDirection = findViewById(R.id.btnGetDirection);

        currentLoc = new LatLng(0.0, 0.0);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        onLocationChanged();
                    }
                }
            }
        };

        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        getLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        p1marker = mMap.addMarker(place1);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                place2 = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Destination");
                mMap.addMarker(place2);

                tvBegin.setVisibility(View.GONE);
                btnGetDirection.setEnabled(true);
                mMap.setOnMapClickListener(null);
            }
        });

        CameraPosition googlePlex = CameraPosition.builder()
                .target(currentLoc)
                .zoom(12)
                .bearing(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 3000, null);
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
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.api_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

        List<Leg> allLegs = (List<Leg>)values[1];
        allSteps = new ArrayList<>();
        latlngPolyline = new ArrayList<>();
        for (Leg leg : allLegs) {
            allSteps.addAll(leg.steps);
            for (Step step : leg.steps) {
                latlngPolyline.addAll(step.stepPolyline);
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1000) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    public void onLocationChanged() {

        tvLocation.setText(currentLoc.latitude + "    " + currentLoc.longitude);
        checkIfWithinStep();

        if (p1marker != null) {
            p1marker.remove();
        }
        place1 = new MarkerOptions().position(currentLoc).title("Current Location");
        if (mMap != null) {
            p1marker = mMap.addMarker(place1);
        }

        if (isFirstTime){
            //code to draw path on map
            btnGetDirection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // if user clicked the GET DIRECTION button, it will enable auto API call for only once
                    isAutoApiCallEnabled = true;
                    fetchNewRoute();
                }
            });

            //place1 = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Location 1");
            //place2 = new MarkerOptions().position(new LatLng(DEST_LAT, DEST_LON)).title("Destination");
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapNearBy);
            mapFragment.getMapAsync(this);
            isFirstTime = false;
        }
    }

    private void checkIfWithinStep() {

        // Check if current location is inside a Step. If yes, it will display an instruction ("Turn Left/Turn Right", etc.)
        if (allSteps != null) {
            int allStepsSize = allSteps.size();
            for (int i = 0; i < allStepsSize; i++) {
                if (PolyUtil.isLocationOnPath(currentLoc, allSteps.get(i).stepPolyline, false, MIN_TOLERANCE)) {
                    if (i + 1 < allStepsSize) {
                        String msg = Html.fromHtml(allSteps.get(i + 1).htmlInstructions).toString();
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // Also checks if current location is within the whole polyline.
        if (latlngPolyline != null) {
            if (!PolyUtil.isLocationOnPath(currentLoc, latlngPolyline, false, DEFAUULT_TOLERANCE)) {
                if (isAutoApiCallEnabled) {
                    // Automatic API call
                    fetchNewRoute();
                    isAutoApiCallEnabled = false;
                } else {
                    // To avoid continuous API call, just advise user to click the GET DIRECTION button after the first automatic API call
                    Toast.makeText(this, "Current location is out of path. You may press GET DIRECTION to adjust path.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void fetchNewRoute() {
        new FetchURL(MainActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
    }
}
