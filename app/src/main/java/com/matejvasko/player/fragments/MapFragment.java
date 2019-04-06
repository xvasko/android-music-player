package com.matejvasko.player.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.App;
import com.matejvasko.player.LocationService;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.MainActivity;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.utils.UtilsCallback;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private boolean locationPermissionGranted = false;

    private DatabaseReference userLocationDatabase = FirebaseDatabase.getInstance().getReference().child("user_locations").child(Authentication.getCurrentUserUid());
    private ValueEventListener userLocationListener;


    private MapView mapView;
    private GoogleMap googleMap;

    private GeoFire geoFire;
    private GeoQuery geoQuery;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_locations");
        geoFire = new GeoFire(ref);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (checkMapRequirements()) {
            if (locationPermissionGranted) {
                startLocationService();
            } else {
                getLocationPermission();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    private void startListeningToLocationChanges() {
        userLocationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double latitude = (Double) dataSnapshot.child("l").child("0").getValue();
                final Double longitude =  (Double) dataSnapshot.child("l").child("1").getValue();
                System.out.println("on data change: latitude " + latitude);
                System.out.println("on data change: longitude " + longitude);

                if (latitude != null && longitude != null) {
                    LatLng position = new LatLng(latitude, longitude);
                    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
                    GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, 0.5);
                    geoQuery.removeAllListeners();
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

                        Map<String, Marker> markers = new HashMap<>();

                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if (key.equals(Authentication.getCurrentUserUid())) return;
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(key));
                            markers.put(Authentication.getCurrentUserUid(), marker);
                            System.out.println("goeQuery: onKeyEntered: " + key);
                        }

                        @Override
                        public void onKeyExited(String key) {
                            if (key.equals(Authentication.getCurrentUserUid())) return;
                            Marker marker = markers.get(key);
                            if (marker != null) {
                                marker.remove();
                                markers.remove(key);
                            }
                            System.out.println("goeQuery: onKeyExited: " + key);
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {
                            if (key.equals(Authentication.getCurrentUserUid())) return;
                            Marker marker = markers.get(key);
                            if (marker != null) {
                                marker.setPosition(new LatLng(location.latitude, location.longitude));
                            }
                            System.out.println("goeQuery: onKeyMoved: " + key);
//                            googleMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(key));
                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });
                    googleMap.clear();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
                    googleMap.addMarker(new MarkerOptions().position(position).title("You"));
                    googleMap.addCircle(new CircleOptions().center(position).radius(500).strokeColor(Color.RED).strokeWidth(3f));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userLocationDatabase.addValueEventListener(userLocationListener);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        System.out.println("ON MAP READY");
        this.googleMap = googleMap;
        startListeningToLocationChanges();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        userLocationDatabase.removeEventListener(userLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private boolean checkMapRequirements() {
        if (isServicesOK()) {
            if (isLocationEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isLocationEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getLocationPermission() {
        if (checkSelfPermission(App.getAppContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            startLocationService();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(App.getAppContext(), LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getActivity().startForegroundService(serviceIntent);
            } else {
                getActivity().startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.matejvasko.player.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }


}

