package com.byteshaft.tu_nav.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byteshaft.tu_nav.R;
import com.byteshaft.tu_nav.utils.Helpers;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    public static boolean isRegistrationFragmentOpen;
    public static int responseCode;
    public static View baseViewRegisterFragment;
    public static boolean locationAcquired;
    static String userRegisterEmail;
    static int registerUserType = -1;
    static String driverLocationToString;

    EditText etRegisterUserFullName;
    EditText etRegisterUserEmail;
    EditText etRegisterUserEmailRepeat;
    EditText etRegisterUserPassword;
    EditText etRegisterUserConfirmPassword;
    String userRegisterFullName;
    String userRegisterEmailRepeat;
    String userRegisterPassword;
    String userRegisterConfirmPassword;
    Button btnCreateUser;
    boolean isUserRegistrationTaskRunning;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        baseViewRegisterFragment = inflater.inflate(R.layout.fragment_register, container, false);

        etRegisterUserFullName = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_full_name);
        etRegisterUserEmail = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_email);
        etRegisterUserEmailRepeat = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_email_repeat);
        etRegisterUserPassword = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_password);
        etRegisterUserConfirmPassword = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_confirm_password);
        baseViewRegisterFragment.findViewById(R.id.ll_register).requestFocus();

        btnCreateUser = (Button) baseViewRegisterFragment.findViewById(R.id.btn_register_create_account);
        btnCreateUser.setOnClickListener(this);
    return baseViewRegisterFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register_create_account:

                userRegisterFullName = etRegisterUserFullName.getText().toString();
                userRegisterEmail = etRegisterUserEmail.getText().toString();
                userRegisterEmailRepeat = etRegisterUserEmailRepeat.getText().toString();
                userRegisterPassword = etRegisterUserPassword.getText().toString();
                userRegisterConfirmPassword = etRegisterUserConfirmPassword.getText().toString();

                if (validateRegisterInfo()) {
//                        new RegisterUserTask().execute();
                }
                break;
        }
    }

    public boolean validateRegisterInfo() {
        boolean valid = true;

        if (userRegisterFullName.trim().isEmpty()) {
            etRegisterUserFullName.setError("Empty");
            valid = false;
        }

        if (userRegisterEmail.trim().isEmpty()) {
            etRegisterUserEmail.setError("Empty");
            valid = false;
        } else if (!userRegisterEmail.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(userRegisterEmail).matches()) {
            etRegisterUserEmail.setError("Invalid E-Mail");
            valid = false;
        } else if (!userRegisterEmail.equals(userRegisterEmailRepeat)) {
            etRegisterUserEmailRepeat.setError("E-Mail doesn't match");
            valid = false;
        } else {
            etRegisterUserEmail.setError(null);
        }

        if (userRegisterPassword.trim().isEmpty() || userRegisterPassword.length() < 6) {
            etRegisterUserPassword.setError("Minimum 6 Characters");
            valid = false;
        } else if (!userRegisterPassword.equals(userRegisterConfirmPassword)) {
            etRegisterUserConfirmPassword.setError("Password doesn't match");
            valid = false;
        } else {
            etRegisterUserPassword.setError(null);
        }

        return valid;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRegistrationFragmentOpen = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isRegistrationFragmentOpen = true;
    }

    public void onRegistrationSuccess() {
        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
    }

    public void onRegistrationFailed(String message) {
        Helpers.showSnackBar(baseViewRegisterFragment, message, Snackbar.LENGTH_SHORT, "#f44336");
    }

}