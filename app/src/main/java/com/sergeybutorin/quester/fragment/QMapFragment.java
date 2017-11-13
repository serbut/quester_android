package com.sergeybutorin.quester.fragment;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.utils.QuesterDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private QuesterDbHelper dbHelper;


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
                Log.d(TAG, "Save" + questToAdd.getPositions().size());
                saveQuest(questToAdd);
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

        dbHelper = QuesterDbHelper.getInstance(getContext());
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

        getQuests();
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

    private void saveQuest(final Quest quest) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                String user = sp.getString(Constants.USER_KEYS.EMAIL.getValue(), getResources().getString(R.string.guest_name));

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues questValues = new ContentValues();
                questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE, quest.getName());
                questValues.put(QuesterDbHelper.QuestEntry.COLUMN_NAME_USER, user);
                long newRowId = db.insert(QuesterDbHelper.QuestEntry.TABLE_NAME, null, questValues);
                int order = 0;
                for (LatLng point : quest.getPositions()) {
                    ContentValues pointValues = new ContentValues();
                    pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_QUEST, newRowId);
                    pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER, order++);
                    pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_X, point.latitude);
                    pointValues.put(QuesterDbHelper.PointEntry.COLUMN_NAME_Y, point.longitude);
                    Log.d(TAG, "Point in (" + point.latitude + ", " + point.longitude + ") saved");
                    db.insert(QuesterDbHelper.PointEntry.TABLE_NAME, null, pointValues);
                }
            }
        }).start();
    }

    private void getQuests() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Reading started");

                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String[] questProjection = {
                        QuesterDbHelper.QuestEntry._ID,
                        QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE
                };
                String sortOrder =
                        QuesterDbHelper.QuestEntry._ID;
                Cursor cursor = db.query(
                        QuesterDbHelper.QuestEntry.TABLE_NAME,
                        questProjection, null,null,
                        null,null, sortOrder);

                Map <Integer, String> questIdTitle= new HashMap<>();
                while(cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry._ID));
                    String title = cursor.getString(
                            cursor.getColumnIndexOrThrow(QuesterDbHelper.QuestEntry.COLUMN_NAME_TITLE));
                    questIdTitle.put(id, title);
                    Log.d(TAG, title);
                }

                String[] pointProjection = {
                        QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER,
                        QuesterDbHelper.PointEntry.COLUMN_NAME_X,
                        QuesterDbHelper.PointEntry.COLUMN_NAME_Y
                };
                String selection = QuesterDbHelper.PointEntry._ID + " = ?";
                sortOrder = QuesterDbHelper.PointEntry.COLUMN_NAME_ORDER;
                for (Integer id : questIdTitle.keySet()) {
                    Log.d(TAG, "Quest " + id);
                    String[] selectionArgs = { String.valueOf(id) };

                    cursor = db.query(
                            QuesterDbHelper.PointEntry.TABLE_NAME,
                            pointProjection, selection,
                            selectionArgs,
                            null,null, sortOrder);
                    LinkedList<LatLng> coordinates = new LinkedList<>();
                    while(cursor.moveToNext()) {
                        double x = cursor.getDouble(cursor.getColumnIndexOrThrow(QuesterDbHelper.PointEntry.COLUMN_NAME_X));
                        double y = cursor.getDouble(cursor.getColumnIndexOrThrow(QuesterDbHelper.PointEntry.COLUMN_NAME_Y));
                        LatLng ll = new LatLng(x, y);
                        coordinates.add(ll);
                        Log.d(TAG, "Point in (" + x + ", " + y + ")");
                    }
                    Quest quest = new Quest("123", coordinates);
                    quests.add(quest);
                }
                cursor.close();
            }
        }).start();
    }
}
