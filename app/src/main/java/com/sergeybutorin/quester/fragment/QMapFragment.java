package com.sergeybutorin.quester.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import com.sergeybutorin.quester.model.Point;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.QuestBase;
import com.sergeybutorin.quester.network.QuestController;
import com.sergeybutorin.quester.utils.GetQuestListTask;
import com.sergeybutorin.quester.utils.QuestAddTask;
import com.sergeybutorin.quester.utils.QuestUpdateTask;
import com.sergeybutorin.quester.utils.QuesterDbHelper;
import com.sergeybutorin.quester.utils.QuestsGetTask;
import com.sergeybutorin.quester.utils.SPHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sergeybutorin on 29/10/2017.
 */

public class QMapFragment extends QFragment implements OnMapReadyCallback,
        QuestController.AddQuestListener,
        QuestController.GetQuestListener {

    public static final String TAG = QMapFragment.class.getSimpleName();
    public static final String QUEST_ARG = "QUEST_ARG";

    private final String CAMERA_ZOOM_KEY = "CAMERA_ZOOM_KEY";
    private final String CAMERA_POS_KEY = "CAMERA_POS_KEY";
    private final String STATE_KEY = "STATE_KEY";
    private final String QUEST_TO_ADD_KEY = "QUEST_TO_ADD_KEY";

    QuestAddListener questAddListener;

    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private float mDefaultZoom = 15;
    private LatLng mDefaultLocation = new LatLng(55.749465, 37.631988);

    private enum QUESTS_STATE {DISPLAY, ADD}

    private QUESTS_STATE state = QUESTS_STATE.DISPLAY;
    private LinkedList<Quest> quests = new LinkedList<>();
    private final java.util.HashMap<Marker, Quest> mapper = new java.util.HashMap<>();

    private Quest questToAdd = new Quest(UUID.randomUUID());
    private LinkedList<Marker> newQuestMarkers = new LinkedList<>();
    private Quest addedQuest;
    private boolean isLoggedIn = false;
    private boolean isRestored = false;

    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;
    @BindView(R.id.fab_done)
    FloatingActionButton fabDone;
    @BindView(R.id.fab_clear)
    FloatingActionButton fabClear;

    private QuesterDbHelper dbHelper;
    private QuestsGetTask questsGetTask;
    private GetQuestListTask getQuestListTask;

    private QuestController controller;

    private final long animationDuration = 500L;

    public interface QuestAddListener {
        void onPointsAdded(Quest quest);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        isLoggedIn = SPHelper.getInstance(getContext()).isUserSet();
        getActivity().setTitle(R.string.map);

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        questAddListener = (QuestAddListener) getActivity();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        dbHelper = QuesterDbHelper.getInstance(getContext());

        getQuestListTask = new GetQuestListTask(dbHelper, this);
        getQuestListTask.execute();

        controller = QuestController.getInstance();
        controller.setAddQuestListener(this);
        controller.setGetQuestListener(this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            addedQuest = (Quest) bundle.getSerializable(QUEST_ARG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap != null) {
            if (mMap.getCameraPosition().target != null) {
                outState.putSerializable(CAMERA_ZOOM_KEY, mMap.getCameraPosition().zoom);
                outState.putParcelable(CAMERA_POS_KEY, mMap.getCameraPosition().target);
            }
            outState.putSerializable("newQuestMarkers", newQuestMarkers);
            outState.putSerializable("Quests", quests);
            outState.putSerializable(QUEST_TO_ADD_KEY, questToAdd);
            outState.putSerializable(STATE_KEY, state);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CAMERA_POS_KEY)) {
                mDefaultLocation = savedInstanceState.getParcelable(CAMERA_POS_KEY);
                mDefaultZoom = (float) savedInstanceState.getSerializable(CAMERA_ZOOM_KEY);
            }
            quests = (LinkedList<Quest>) savedInstanceState.getSerializable("Quests");
            questToAdd = (Quest) savedInstanceState.getSerializable(QUEST_TO_ADD_KEY);
            newQuestMarkers = (LinkedList<Marker>) savedInstanceState.getSerializable("newQuestMarkers");
            state = (QUESTS_STATE) savedInstanceState.getSerializable(STATE_KEY);
            isRestored = true;
        }
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
                    showQuestDetail(quest);
                    Log.d(TAG, "onMarkerClick " + quest.getTitle());
                } else {
                    Log.d(TAG, "onMarkerClick ?");
                }

                return false; // TODO for what?
            }
        });

        getLocationPermission();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng coordinates) {
                switch (state) {
                    case DISPLAY:
                        showQuests();
                        break;
                    case ADD:
                        Marker marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(coordinates)
                                        .title("title")
                                        .snippet("snippet")
                                        .draggable(true)
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        );
                        questToAdd.addPoint(new Point(UUID.randomUUID(), coordinates));
                        newQuestMarkers.add(marker);

                        LatLng position = marker.getPosition();
                        Log.d(TAG, position.latitude + ", " + position.longitude);
                        break;
                }
            }
        });

        if (addedQuest != null) {
            String token = SPHelper.getInstance(getContext()).getUserToken();
            saveQuest(addedQuest);
            controller.add(addedQuest, token);
            mDefaultLocation = addedQuest.getPoints().getFirst().getCoordinates();
            setCamera(mDefaultLocation, mDefaultZoom);
        }
        if (isRestored) {
            setCamera(mDefaultLocation, mDefaultZoom);
        } else {
            questsGetTask = new QuestsGetTask(dbHelper, this);
            questsGetTask.execute();
        }

        switchState();
    }

    @Override
    public void setTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(R.string.map);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (questsGetTask != null) {
            questsGetTask.cancel(false);
            getQuestListTask.cancel(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.setGetQuestListener(null);
        controller.setAddQuestListener(null);
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
                            if (mLastKnownLocation != null
                                    && addedQuest == null
                                    && !isRestored) {
                                setCamera(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), mDefaultZoom);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            setCamera(mDefaultLocation, mDefaultZoom);
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
                updateLocationUI();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setCamera(LatLng position, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                position, zoom));
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
            getDeviceLocation();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
        getDeviceLocation();
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @OnClick(R.id.fab_add)
    void onAddButtonClick() {
        state = QUESTS_STATE.ADD;
        switchState();
    }

    @OnClick(R.id.fab_done)
    void onDoneButtonClick() {
        if (questToAdd.getPoints().size() < 1) {
            Toast.makeText(getContext(), R.string.error_no_points_quests, Toast.LENGTH_SHORT).show();
        } else {
            state = QUESTS_STATE.DISPLAY;
            questAddListener.onPointsAdded(questToAdd);
        }
    }

    @OnClick(R.id.fab_clear)
    void onClearButtonClick() {
        state = QUESTS_STATE.DISPLAY;
        switchState();
    }

    private void switchState() {
        switch (state) {
            case DISPLAY:
                if (isLoggedIn) {
                    fabAdd.show();
                } else {
                    fabAdd.hide();
                }
                hideMenu();
                for (Marker marker : newQuestMarkers) {
                    marker.remove();
                }
                fabAdd.setClickable(true);
                questToAdd.clear();
                showQuests();
                break;
            case ADD:
                showMenu();
                mMap.clear();
                break;
        }
    }

    private void showMenu() {
        fabDone.animate()
                .translationY(-1.5f * fabDone.getHeight())
                .alpha(1)
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabDone.setEnabled(true);
                    }
                }).start();
        fabClear.animate()
                .translationX(-1.5f * fabClear.getWidth())
                .alpha(1)
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabClear.setEnabled(true);
                    }
                }).start();

        fabAdd.setEnabled(false);
        fabAdd.setBackgroundTintList(ColorStateList
                .valueOf(ContextCompat.getColor(getContext(), R.color.colorGray)));
    }

    private void hideMenu() {
        fabDone.animate()
                .alpha(0)
                .translationY(0)
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabDone.setEnabled(false);
                    }
                }).start();
        fabClear.animate()
                .alpha(0)
                .translationX(0)
                .setDuration(animationDuration)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabClear.setEnabled(false);
                    }
                }).start();

        fabAdd.setEnabled(true);
        fabAdd.setBackgroundTintList(ColorStateList
                .valueOf(ContextCompat.getColor(getContext(), R.color.colorGreen)));
    }

    private void showQuests() {
        mMap.clear();

        for (Quest quest : quests) {
            showQuest(quest);
        }
    }

    private void showQuest(Quest quest) {
        if (mMap != null) {
            LatLng position = quest.getPoints().getFirst().getCoordinates();
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(quest.getTitle())
                            .snippet(quest.getDescription())
                            .icon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            mapper.put(marker, quest);
        }
    }

    private void showQuestDetail(Quest quest) {
        mMap.clear();

        int i = 1;
        for (Point position : quest.getPoints()) {
            mMap.addMarker(
                    new MarkerOptions()
                            .position(position.getCoordinates())
                            .title(quest.getTitle())
                            .snippet("#" + i)
                            .icon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            i++;
        }
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
        showQuest(quest);
    }

    private void saveQuest(Quest quest) {
        QuestAddTask questAddTask = new QuestAddTask(dbHelper);
        questAddTask.execute(quest);
        addQuest(quest);
    }

    private void updateQuest(Quest quest) {
        QuestUpdateTask questUpdateTask = new QuestUpdateTask(dbHelper);
        questUpdateTask.execute(quest);
    }

    @Override
    public void onAddResult(boolean success, int message, Quest quest) {
        if (quest != null) {
            updateQuest(quest);
        }
    }

    @Override
    public void onGetResult(boolean success, Quest quest) {
        if (quest != null) {
            saveQuest(quest);
        }
    }

    public void getNewQuests(List<QuestBase> existingQuests) {
        controller.getMissing(existingQuests);
    }

    public void syncQuest(Quest quest) {
        Log.d(TAG, "Not synced quest found");
        controller.add(quest, SPHelper.getInstance(getContext()).getUserToken());
    }
}
