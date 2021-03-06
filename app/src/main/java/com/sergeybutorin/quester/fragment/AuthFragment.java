package com.sergeybutorin.quester.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SignUpEvent;
import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.activity.MainActivity;
import com.sergeybutorin.quester.model.UserProfile;
import com.sergeybutorin.quester.network.AuthController;
import com.sergeybutorin.quester.utils.SPHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sergeybutorin on 03/11/2017.
 */

public class AuthFragment extends QFragment
        implements AuthController.LoginListener,
        AuthController.SignupListener {
    public static final String TAG = AuthFragment.class.getSimpleName();

    private boolean signupMode = false;

    @BindView(R.id.button_login)
    Button loginButton;
    @BindView(R.id.email_edit_text)
    EditText emailEditText;
    @BindView(R.id.password_edit_text)
    EditText passwordEditText;
    @BindView(R.id.firstname_edit_text)
    EditText firstnameEditText;
    @BindView(R.id.lastname_edit_text)
    EditText lastnameEditText;

    private AuthController controller;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, view);

        controller = AuthController.getInstance();
        controller.setLoginResultListener(this);
        controller.setSignupResultListener(this);

        getActivity().setTitle(R.string.auth_text);

        return view;
    }

    @Override
    public void setTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(R.string.auth_text);
        }
    }

    @OnClick(R.id.button_login)
    void onLoginButtonClick() {
        ((MainActivity)getActivity()).hideSoftKeyboard();
        if (checkLoginFields()) {
            controller.login(emailEditText.getText().toString(),
                    passwordEditText.getText().toString());
        }
    }

    @OnClick(R.id.button_signup)
    void onSignupButtonClick() {
        ((MainActivity)getActivity()).hideSoftKeyboard();
        if (!signupMode) {
            controller.setLoginResultListener(null);
            showSignupFields();
        } else if (checkSignupFields()) {
            controller.signup(emailEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    firstnameEditText.getText().toString(),
                    lastnameEditText.getText().toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.setLoginResultListener(null);
        controller.setSignupResultListener(null);
    }

    private void showSignupFields() {
        signupMode = true;
        loginButton.setVisibility(View.GONE);
        firstnameEditText.setVisibility(View.VISIBLE);
        lastnameEditText.setVisibility(View.VISIBLE);
    }

    private boolean checkLoginFields() {
        if (TextUtils.isEmpty(emailEditText.getText().toString()) ||
                TextUtils.isEmpty(passwordEditText.getText().toString())) {
            Toast.makeText(getContext(), R.string.error_empty_strings, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!emailEditText.getText().toString().matches(Constants.EMAIL_REGEX)){
            Toast.makeText(getContext(), R.string.error_incorrect_email, Toast.LENGTH_LONG).show();
            emailEditText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean checkSignupFields() {
        if (!checkLoginFields()) {
            return false;
        }
        if (TextUtils.isEmpty(firstnameEditText.getText().toString()) ||
                TextUtils.isEmpty(lastnameEditText.getText().toString())) {
            Toast.makeText(getContext(), R.string.error_empty_strings, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onLoginResult(boolean success, int message, UserProfile user) {
        Answers.getInstance().logLogin(new LoginEvent()
                .putSuccess(success));
        setUser(success, message, user);
    }

    @Override
    public void onSignupResult(boolean success, int message, UserProfile user) {
        Answers.getInstance().logSignUp(new SignUpEvent()
                .putSuccess(success));
        setUser(success, message, user);
    }

    private void setUser(boolean success, int message, UserProfile user) {
        if (!success) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        } else if (user != null) {
            SPHelper.getInstance(getContext()).setUserData(user);

            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, new QMapFragment()).commit();
            ((MainActivity)getActivity()).setUserInformation();
        }
    }
}
