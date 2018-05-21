package com.example.san.googlemapsapi.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;


import com.example.san.googlemapsapi.R;
import com.example.san.googlemapsapi.base.BaseActivity;
import com.example.san.googlemapsapi.model.place.DirectionParser;
import com.example.san.googlemapsapi.model.place.EndLocation;
import com.example.san.googlemapsapi.model.place.Leg;
import com.example.san.googlemapsapi.model.place.Polyline;
import com.example.san.googlemapsapi.model.place.StartLocation;
import com.example.san.googlemapsapi.model.place.Step;
import com.example.san.googlemapsapi.untils.BitmapUntils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.san.googlemapsapi.untils.Const.Map.MILLISECOND_PER_SECOND;
import static com.example.san.googlemapsapi.untils.Const.Map.MINUTE;


public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, IMapsView {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private Marker mCurrentMarker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mDefaultLocation = null;
    private static final float DEFAULT_ZOOM = 17;
    private Place mSelectPlace = null;
    private LatLng mCurrentLatLng = null;
    private MapsPst mMapsPst;
    private TextView mTextName, mTextAddress, mTextDuration, mTextDistance;
    private com.google.android.gms.maps.model.Polyline mCurrentPolyline = null;
    private BottomSheetBehavior sheetBehavior;
    private LinearLayout layoutBottomSheet, mButtonChiDuong, mButtonInfo;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isUpdate = false;
    private ImageButton mButtonBike, mButtonBus,mButtonCar,mButtonWalk;
    private String mode = "driving";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        innitView();
        getLocationPermission();
        showDialog();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        innitPlaceAutocompleteFragment();
        setUpLocationClientIfNeeded();
        buildLocationRequest();
    }

    private void buildLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10*MILLISECOND_PER_SECOND);
        mLocationRequest.setFastestInterval(5 * MILLISECOND_PER_SECOND);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setUpLocationClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void innitPlaceAutocompleteFragment() {
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(mPlaceSelectionListener);
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        autocompleteFragment.setText("");
                        view.setVisibility(View.GONE);
                        if (mCurrentMarker != null) mCurrentMarker.remove();
                        if (mCurrentPolyline != null) mCurrentPolyline.remove();
                        if (mMap != null) {
                            moveCameraToCurrentLocation();
                        }
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        isUpdate = false;
                    }
                });
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
    }

    private PlaceSelectionListener mPlaceSelectionListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            if (mMap != null) {
                mSelectPlace = place;
                if (mCurrentMarker != null) mCurrentMarker.remove();
                if (mCurrentPolyline != null) mCurrentPolyline.remove();
                mCurrentMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapUntils.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_location_on_black_24dp)))
                        .position(place.getLatLng())
                        .title(place.getName().toString()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                isUpdate = true;
            }
        }

        @Override
        public void onError(Status status) {
            Log.i("TAG", "An error occurred: " + status);
        }
    };


    private void innitView() {
        mDefaultLocation = new LatLng(21.007376, 105.842882);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mMapsPst = new MapsPst(this);
        layoutBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mTextName = (TextView) findViewById(R.id.text_name_location);
        mTextAddress = (TextView) findViewById(R.id.text_address);
        mTextDuration = (TextView) findViewById(R.id.text_duration);
        mTextDistance = (TextView) findViewById(R.id.text_distance);
        mButtonChiDuong = (LinearLayout) findViewById(R.id.button_chi_duong);
        mButtonChiDuong.setOnClickListener(mButtonChiDuongClick);
        mButtonInfo = (LinearLayout) findViewById(R.id.button_more_info);
        mButtonInfo.setOnClickListener(mButtonInfoClick);
        mButtonBike = (ImageButton) findViewById(R.id.button_bike);
        mButtonBus = (ImageButton) findViewById(R.id.button_bus);
        mButtonCar = (ImageButton) findViewById(R.id.button_car);
        mButtonWalk = (ImageButton) findViewById(R.id.button_walk);
        mButtonBike.setOnClickListener(mButtonSelectMode);
        mButtonWalk.setOnClickListener(mButtonSelectMode);
        mButtonCar.setOnClickListener(mButtonSelectMode);
        mButtonBus.setOnClickListener(mButtonSelectMode);
    }

    private View.OnClickListener mButtonInfoClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mButtonBike.setVisibility(View.GONE);
            mButtonBus .setVisibility(View.GONE);
            mButtonCar .setVisibility(View.GONE);
            mButtonWalk.setVisibility(View.GONE);
        }
    };
    private View.OnClickListener mButtonChiDuongClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mButtonBike.setVisibility(View.VISIBLE);
            mButtonBus .setVisibility(View.VISIBLE);
            mButtonCar .setVisibility(View.VISIBLE);
            mButtonWalk.setVisibility(View.VISIBLE);
        }
    };
    private View.OnClickListener mButtonSelectMode = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.button_bike) mode = "bicycling";
            else if(view.getId() == R.id.button_car) mode = "driving";
            else if(view.getId() == R.id.button_walk) mode = "walking";
            else mode = "transit";
            mMapsPst.getDataDirection(mCurrentLatLng,mSelectPlace.getLatLng(),mode);
            isUpdate = true;
            moveCameraToCurrentLocation();
        }
    };


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLatLng = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        int accessCoarsePermission
                = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFinePermission
                = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
            Dexter.withActivity(MapsActivity.this)
                    .withPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted())
                                mLocationPermissionGranted = true;
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else mLocationPermissionGranted = true;
    }

    private void getDeviceLocation() {
        if (mLocationPermissionGranted) {
            @SuppressLint("MissingPermission") final Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(MapsActivity.this, new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) locationResult.getResult();
                        mCurrentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 13));
                        moveCameraToCurrentLocation();
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        }
    }

    private void moveCameraToCurrentLocation() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mCurrentLatLng)             // Sets the center of the map to location user
                .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    hideDialog();
                    updateLocationUI();
                    getDeviceLocation();
                }
            });
            mMap.setPadding(0, 150, 0, 0);
        }
    }


    @Override
    public void onSuccess(List<LatLng> path, String distance, String duration) {
        mTextName.setText(mSelectPlace.getName());
        mTextAddress.setText(mSelectPlace.getAddress());
        mTextDistance.setText(distance);
        mTextDuration.setText(duration);
        if(mCurrentPolyline != null) mCurrentPolyline.remove();
        mCurrentPolyline = mMap.addPolyline(new PolylineOptions().addAll(path).color(Color.RED).width(5));
    }

    @Override
    public void onError(String err_msg) {
        Toast.makeText(getApplicationContext(),err_msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("TAG", "onLocationChanged: " + location.getProvider());
        mCurrentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        if(isUpdate)
            mMapsPst.getDataDirection(new LatLng(location.getLatitude(), location.getLongitude()), mSelectPlace.getLatLng(),mode);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        super.onDestroy();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

}
