package com.sergeybutorin.quester.fragment;

import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.sergeybutorin.quester.presenters.PresenterManager;
import com.sergeybutorin.quester.presenters.QMapPresenter;
import com.sergeybutorin.quester.views.QMapView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by sergeybutorin on 29/10/2017.
 */

public class QMapFragment extends QFragment
        implements QMapView, OnMapReadyCallback {

    public static final String TAG = QMapFragment.class.getSimpleName();
    public static final String QUEST_ARG = "QUEST_ARG";

    private final String CAMERA_ZOOM_KEY = "CAMERA_ZOOM_KEY";
    private final String CAMERA_POS_KEY = "CAMERA_POS_KEY";

    private QMapPresenter presenter;
    QuestAddListener questAddListener;
    QuestSelectedListener questSelectedListener;

    private GoogleMap mMap;

    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private float mDefaultZoom = 15;
    private LatLng mDefaultLocation = new LatLng(55.749465, 37.631988);

    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;
    @BindView(R.id.fab_done)
    FloatingActionButton fabDone;
    @BindView(R.id.fab_clear)
    FloatingActionButton fabClear;
    @BindView(R.id.quest_detail_content)
    LinearLayout detailLayout;

    private final long ANIMATION_DURATION = 500L;
    private int detailViewHeight = 0;

    public interface QuestAddListener {
        void onPointsAdded(Quest quest);
    }

    public interface QuestSelectedListener {
        void onQuestSelected(Quest quest);
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

        getActivity().setTitle(R.string.map);

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        questAddListener = (QuestAddListener) getActivity();
        questSelectedListener = (QuestSelectedListener) getActivity();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        setMaxDetailHeight();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PresenterManager.getInstance().savePresenter(presenter, outState);
        if (mMap != null) {
            outState.putSerializable(CAMERA_ZOOM_KEY, mMap.getCameraPosition().zoom);
            outState.putParcelable(CAMERA_POS_KEY, mMap.getCameraPosition().target);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            presenter = PresenterManager.getInstance().restorePresenter(savedInstanceState);

            if (savedInstanceState.containsKey(CAMERA_POS_KEY)) {
                mDefaultLocation = savedInstanceState.getParcelable(CAMERA_POS_KEY);
                mDefaultZoom = (float) savedInstanceState.getSerializable(CAMERA_ZOOM_KEY);
            }

//            isRestored = true;
        } else {
            presenter = new QMapPresenter();
        }

        presenter.bindView(this);
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                presenter.onMarkerClicked(marker);
                fabAdd.hide();
                return false;
            }
        });

        getLocationPermission();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng coordinates) {
                presenter.onMapClicked(coordinates);
            }
        });

        presenter.onViewReady();
    }

    @Override
    public void setTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(R.string.map);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.unbindView();
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
        presenter.onAddButtonClicked();
    }

    @OnClick(R.id.fab_done)
    void onDoneButtonClick() {
        presenter.onDoneButtonClicked();
    }

    @OnClick(R.id.fab_clear)
    void onClearButtonClick() {
        presenter.onClearButtonClicked();
    }

    @Override
    public void switchState(QuestState state) {
        switch (state) {
            case DISPLAY:
                if (presenter.isLoggedIn()) {
                    fabAdd.show();
                } else {
                    fabAdd.hide();
                }
                hideMenu();
                presenter.clearMarkers();
                fabAdd.setClickable(true);
//                showQuests();

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
                .setDuration(ANIMATION_DURATION)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabDone.setEnabled(true);
                    }
                }).start();
        fabClear.animate()
                .translationX(-1.5f * fabClear.getWidth())
                .alpha(1)
                .setDuration(ANIMATION_DURATION)
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
                .setDuration(ANIMATION_DURATION)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fabDone.setEnabled(false);
                    }
                }).start();
        fabClear.animate()
                .alpha(0)
                .translationX(0)
                .setDuration(ANIMATION_DURATION)
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

    public void showQuests(List<Quest> quests) {
        if (mMap == null) {
            return;
        }
        mMap.clear();
        for (Quest quest : quests) {
            showQuest(quest);
        }
    }

    @Override
    public void showQuest(Quest quest) {
        if (mMap == null) {
            return;
        }
        Log.d(TAG, "Show quest, id = " + quest.getUuid());
        LatLng position = quest.getPoints().getFirst().getCoordinates();
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .position(position)
                        .title(quest.getTitle())
                        .snippet(quest.getDescription())
                        .icon(BitmapDescriptorFactory.
                                defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        presenter.mapQuestWithMarker(quest, marker);
    }

    @Override
    public void showQuestDetail(Quest quest) {
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


    @Override
    public void addMarketToMap(LatLng coordinates) {
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .position(coordinates)
                        .title("title")
                        .snippet("snippet")
                        .draggable(true)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
        );

        presenter.newMarkerAdded(marker);
    }

    @Override
    public void showNoPointsInQuest() {
        Toast.makeText(getContext(), R.string.error_no_points_quests, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointsAdded(Quest quest) {
        questAddListener.onPointsAdded(quest);
    }

    @Override
    public void onQuestSelected(Quest quest) {
        questSelectedListener.onQuestSelected(quest);
        showQuestDetail(quest);
    }

    @Override
    public void setDefaultLocation(LatLng location) {
        mDefaultLocation = location;
        setCamera(mDefaultLocation, mDefaultZoom);
    }

    @Override
    public void openDetailView() {
        final ViewGroup.LayoutParams layoutParams = detailLayout.getLayoutParams();

        if (getResources().getConfiguration().orientation != ORIENTATION_PORTRAIT ||
                layoutParams.height > 0) {
            return;
        }

        ValueAnimator animateDetailUp = ValueAnimator.ofInt(0, detailViewHeight);
        animateDetailUp.setDuration(ANIMATION_DURATION);
        animateDetailUp.setInterpolator(new LinearInterpolator());
        animateDetailUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.height = (int) animation.getAnimatedValue();
                detailLayout.setLayoutParams(layoutParams);
            }

        });
        animateDetailUp.start();
    }

    @Override
    public void closeDetailView() {
        final ViewGroup.LayoutParams layoutParams = detailLayout.getLayoutParams();
        if (layoutParams.height < detailViewHeight) {
            return;
        }

        ValueAnimator animateDetailDown = ValueAnimator.ofInt(detailViewHeight, 0);
        animateDetailDown.setDuration(ANIMATION_DURATION);
        animateDetailDown.setInterpolator(new LinearInterpolator());
        animateDetailDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.height = (int) animation.getAnimatedValue();
                detailLayout.setLayoutParams(layoutParams);
            }

        });
        animateDetailDown.start();
    }

    @Override
    public void onNewQuestAdded(Quest quest) {
        presenter.addNewQuest(quest);
    }

    private void setMaxDetailHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float height = displayMetrics.heightPixels;
        detailViewHeight = (int) height/3;
    }
}
