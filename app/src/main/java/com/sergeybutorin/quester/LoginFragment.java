package com.sergeybutorin.quester;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by sergeybutorin on 03/11/2017.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {

    Button loginButton;
    Button signupButton;
    EditText firstnameEditText;
    EditText lastnameEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        loginButton = view.findViewById(R.id.button_login);
        signupButton = view.findViewById(R.id.button_signup);
        firstnameEditText = view.findViewById(R.id.firstname_edit_text);
        lastnameEditText = view.findViewById(R.id.lastname_edit_text);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                Toast.makeText(getContext(), "Login", Toast.LENGTH_LONG).show();
            case R.id.button_signup:
                showSignupFields();
        }
    }

    private void showSignupFields() {
        loginButton.setVisibility(View.GONE);
        firstnameEditText.setVisibility(View.VISIBLE);
        lastnameEditText.setVisibility(View.VISIBLE);
    }
}
