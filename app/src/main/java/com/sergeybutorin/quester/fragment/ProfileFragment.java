package com.sergeybutorin.quester.fragment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.activity.MainActivity;
import com.sergeybutorin.quester.utils.SPHelper;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

/**
 * Created by sergeybutorin on 10/12/2017.
 */

public class ProfileFragment extends QFragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();

    private final int CAMERA_REQUEST_CODE = 0;
    private final int GALLERY_REQUEST_CODE = 1;

    @BindView(R.id.avatar_profile)
    ImageView avatar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, view);
        Answers.getInstance().logCustom(new CustomEvent("Profile"));

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setTitle(R.string.profile);

        return view;
    }

    @Override
    public void setTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(R.string.profile);
        }
    }

    @OnClick(R.id.button_logout)
    void onLogoutButtonClick() {
        SPHelper.getInstance(getContext()).removeUserData();
        ((MainActivity)getActivity()).setUserInformation();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, new QMapFragment()).commit();
    }

    @OnClick(R.id.button_add_avatar_camera)
    void onCameraClick() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);//zero can be replaced with any action code
    }

    @OnClick(R.id.button_add_avatar_gallery)
    void onGalleryClick() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);//one can be replaced with any action code
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case CAMERA_REQUEST_CODE:
            case GALLERY_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    Picasso.with(getContext())
                            .load(selectedImage)
                            .resize(500, 500)
                            .centerCrop()
                            .into(avatar);
                }

                break;
        }
    }
}
