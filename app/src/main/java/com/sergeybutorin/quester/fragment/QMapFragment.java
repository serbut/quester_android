package com.sergeybutorin.quester.fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;

import java.util.LinkedList;

/**
 * Created by sergeybutorin on 29/10/2017.
 */

public class QMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = QMapFragment.class.getSimpleName();

    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng mDefaultLocation = new LatLng(55.749465, 37.631988);


    private enum QUESTS_STATE {DISPLAY, ADD}

    private QUESTS_STATE state = QUESTS_STATE.DISPLAY;
    private final LinkedList<Quest> quests = new LinkedList<>();
    private final java.util.HashMap<Marker, Quest> mapper = new java.util.HashMap<>();
    private Quest questToAdd = new Quest();
    FloatingActionButton fabAdd;
    FloatingActionButton fabDone;
    FloatingActionButton fabClear;
    FloatingActionButton fabBack;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fabAdd = view.findViewById(R.id.fab_add);
        fabDone = view.findViewById(R.id.fab_done);
        fabClear = view.findViewById(R.id.fab_clear);
        fabBack = view.findViewById(R.id.fab_back);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = QUESTS_STATE.ADD;
                switchState();
            }
        });
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = QUESTS_STATE.DISPLAY;
//                for(Marker marker : questToAdd.getMarkers()) {
//                    marker.setIcon(BitmapDescriptorFactory
//                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                }
                quests.add(questToAdd);
                questToAdd = new Quest();
                switchState();
                showQuests();
            }
        });
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = QUESTS_STATE.DISPLAY;
                for(Marker marker : questToAdd.getMarkers()) {
                    marker.remove();
                }
                questToAdd.clear();
                switchState();
            }
        });
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuests();
            }
        });


        switchState();


        // test quests
        Quest quest1 = new Quest();
        quest1.setName("test quest 1");
        quest1.setDescription("test description 1");
        quest1.addPosition(new LatLng(55.7510, 37.6320));
        quest1.addPosition(new LatLng(55.7515, 37.6325));
        quest1.addPosition(new LatLng(55.7520, 37.6330));
        quests.add(quest1);

        Quest quest2 = new Quest();
        quest2.setName("test quest 2");
        quest2.setDescription("test description 2");
        quest2.addPosition(new LatLng(55.7410, 37.6220));
        quest2.addPosition(new LatLng(55.7415, 37.6225));
        quest2.addPosition(new LatLng(55.7420, 37.6230));
        quests.add(quest2);
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Quest quest = mapper.get(marker);
                if (quest != null) {
                    fabBack.show();
                    showQuest(quest);
                    Log.d(TAG, "onMarkerClick " + quest.getName());
                } else {
                    Log.d(TAG, "onMarkerClick ?");
                }

                return false; // TODO for what?
            }
        });

        // Add a marker in Moscow and move the camera
        mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Moscow"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                mDefaultLocation, DEFAULT_ZOOM));

        showQuests();

        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (state == QUESTS_STATE.ADD) {
//                  mMap.clear();
                    Marker marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(point)
                                    .title("title")
                                    .snippet("snippet")
                                    .draggable(true)
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    );
                    questToAdd.addPosition(point);
                    questToAdd.addMarkers(marker);

                    LatLng position = marker.getPosition();
                    Log.d(TAG, position.latitude + ", " + position.longitude);
                }
            }
        });
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

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
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void switchState() {
        switch (state) {
            case DISPLAY:
                fabAdd.show();
                fabDone.hide();
                fabClear.hide();
                break;
            case ADD:
                fabAdd.hide();
                fabDone.show();
                fabClear.show();
                break;
        }
    }

    private void showQuests() {
        fabBack.hide();
        mMap.clear();

        for(Quest quest : quests) {
            LatLng position = quest.getPositions().getFirst();
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(quest.getName())
                            .snippet(quest.getDescription())
                            .icon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            mapper.put(marker, quest);
        }
    }

    private void showQuest(Quest quest) {
        mMap.clear();

        int i = 1;
        for (LatLng position: quest.getPositions()) {
            mMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(quest.getName())
                            .snippet("#" + i)
                            .icon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            i++;
        }
    }
}
