package com.example.mateusz.citytourapp.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.mateusz.citytourapp.MapsActivity;
import com.example.mateusz.citytourapp.Model.Feature;
import com.example.mateusz.citytourapp.Model.LocalizationOrangeDTO;
import com.example.mateusz.citytourapp.Model.MonumentsDTO;
import com.example.mateusz.citytourapp.Model.Properties;
import com.example.mateusz.citytourapp.R;
import com.example.mateusz.citytourapp.Services.OrangeApiService;
import com.example.mateusz.citytourapp.Services.PoznanApiService;
import com.example.mateusz.citytourapp.tweeter.DataStoreClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mateusz on 11.02.2018.
 */

public class TabMap extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    MapsActivity activity;
    private TextView titleBottomSheet;
    private TextView descriptionBottomSheet;
    private NetworkImageView mNetworkImageView;
    private ImageLoader mImageLoader;
    Button checkLocalizationButton;
    Button buttonBottomSheet;
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;

    MapView mapView;
    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;

    MonumentsDTO monumentsDTO = null;
    Map<Marker, Feature> markerFeatureMap = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if ( getActivity() instanceof MapsActivity){
            activity = (MapsActivity) getActivity();
        }

        View view = inflater.inflate(R.layout.tab_map, container, false);

        layoutBottomSheet = view.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        checkLocalizationButton = (Button) view.findViewById(R.id.checkLocalizationButton);
        checkLocalizationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckLocalizationBtn(v);
            }
        });

        buttonBottomSheet = (Button) view.findViewById(R.id.button_bottom_sheet);
        buttonBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomSheetBtn(v);
            }
        });

        titleBottomSheet = view.findViewById(R.id.title_bottom_sheet);
        descriptionBottomSheet = view.findViewById(R.id.description_bottom_sheet);
        mNetworkImageView = (NetworkImageView) view.findViewById(R.id.networkImageView);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        //Wyświetlanie mapy jako fragment - tak było wcześniej. W content_main w związku tym wykomentowany fragment. Teraz przez MapView.
        //SupportMapFragment mapFragment = (SupportMapFragment) activity.getFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onResume();

        try {
            MapsInitializer.initialize(activity.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        return view;
    }

    private void setupNetworkImageViewSourceInBottomSheet(String url) {
        mImageLoader = CustomVolleyRequestQueue.getInstance(activity.getApplicationContext())
                .getImageLoader();

        mImageLoader.get(url, ImageLoader.getImageListener(mNetworkImageView,
                R.mipmap.ic_launcher, android.R.drawable //Deafault image
                        .ic_dialog_alert));//Error image
        mNetworkImageView.setImageUrl(url, mImageLoader);
    }

    private void getMonumentsCloseToLocalization(Location location) {
        final PoznanApiService poznanApiService = new PoznanApiService();

        monumentsDTO = poznanApiService.getMonumentsDTO();
        markerFeatureMap = new HashMap<>();

        activity.runOnUiThread(new Runnable() {
            final MonumentsDTO monumentsDTO_UI = monumentsDTO;
            final PoznanApiService poznanApiService_UI = poznanApiService;

            @Override
            public void run() {
                for (Feature feature : monumentsDTO_UI.features) {
                    Marker marker = mGoogleMap.addMarker(getMonumentMarker(feature.properties, poznanApiService_UI.parseGeoLocationDTOLatLng(feature.geometry)));
                    markerFeatureMap.put(marker, feature);
                }
            }
        });
    }

    public void onClickCheckLocalizationBtn(View v) {

        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");*/

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                getMonumentsCloseToLocalization(null);

                OrangeApiService orangeApiService = new OrangeApiService();
                LocalizationOrangeDTO localizationOrangeDTO = orangeApiService.getGeoLocalizationOrange();
                final Location location = orangeApiService.getGeoLocation();

                activity.runOnUiThread(new Runnable() {
                    final Location locationL = location;

                    @Override
                    public void run() {
                        //onLocationChanged(locationL);
                        Toast.makeText(activity.getApplicationContext(), "Location update", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void onClickBottomSheetBtn(View v) {
        activity.switchTab(1);
    }

    private MarkerOptions getMonumentMarker(Properties properties, LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(properties.nazwa);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        return markerOptions;
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
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMarkerDragListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnMapClickListener(this);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Metoda od Google, ale wykorzystywana również do rysowania aktualnej pozycji z BTS'a.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Feature feature = markerFeatureMap.get(marker);
        activity.setSelectedFeature(feature);

        final String url = "http://www.poznan.pl/mim/upload/obiekty/" + feature.properties.grafika;
        setupNetworkImageViewSourceInBottomSheet(url);

        titleBottomSheet.setText(feature.properties.nazwa);
        descriptionBottomSheet.setText(feature.properties.opis);

        synchronized (sheetBehavior) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        };

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        synchronized (sheetBehavior) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
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

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(activity, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
