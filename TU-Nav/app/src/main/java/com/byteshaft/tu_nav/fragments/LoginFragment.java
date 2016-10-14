package com.byteshaft.tu_nav.fragments;

import android.app.Fragment;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.byteshaft.tu_nav.R;
import com.byteshaft.tu_nav.utils.AppGlobals;
import com.byteshaft.tu_nav.utils.Helpers;
import com.byteshaft.tu_nav.utils.SoftKeyboard;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public static String sLoginEmail;
    ImageView ivWelcomeLogoMain;
    LinearLayout llWelcomeLogin;
    Button btnLogin;
    EditText etLoginEmail;
    EditText etLoginPassword;
    String sLoginPassword;
    SoftKeyboard softKeyboard;
    View baseViewLoginFragment;

    Animation animMainLogoFading;
    Animation animMainLogoTransitionUp;
    Animation animMainLogoTransitionDown;
    Animation animMainLogoFadeIn;
    Animation animMainLogoFadeOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseViewLoginFragment = inflater.inflate(R.layout.fragment_login, container, false);

        ivWelcomeLogoMain = (ImageView) baseViewLoginFragment.findViewById(R.id.iv_welcome_logo_main);
        llWelcomeLogin = (LinearLayout) baseViewLoginFragment.findViewById(R.id.ll_welcome_login);
        etLoginEmail = (EditText) baseViewLoginFragment.findViewById(R.id.et_login_email);
        etLoginPassword = (EditText) baseViewLoginFragment.findViewById(R.id.et_login_password);
        btnLogin = (Button) baseViewLoginFragment.findViewById(R.id.btn_login_login);
        btnLogin.setOnClickListener(this);
        llWelcomeLogin.setVisibility(View.GONE);
        animMainLogoFading = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_welcome_logo_partial_fading);
        animMainLogoTransitionUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_welcome_logo_transition_up);
        animMainLogoTransitionDown = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_welcome_logo_transition_down);
        animMainLogoFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_welcome_logo_fade_in);
        animMainLogoFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_welcome_logo_fade_out);
        RelativeLayout mainLayout = (RelativeLayout) baseViewLoginFragment.findViewById(R.id.layout_fragment_login);
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivWelcomeLogoMain.startAnimation(animMainLogoTransitionUp);
                }
            }, 350);

        softKeyboard = new SoftKeyboard(mainLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {

            @Override
            public void onSoftKeyboardHide() {
                Helpers.setIsSoftKeyboardOpen(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivWelcomeLogoMain.startAnimation(animMainLogoFadeIn);
                        }
                    });
            }

            @Override
            public void onSoftKeyboardShow() {
                Helpers.setIsSoftKeyboardOpen(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivWelcomeLogoMain.startAnimation(animMainLogoFadeOut);
                        }
                    });
                }
        });

        animMainLogoTransitionUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llWelcomeLogin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animMainLogoTransitionDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("yoyo", "ugiu");
                        ivWelcomeLogoMain.startAnimation(animMainLogoFading);
                    }
                }, 350);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animMainLogoFading.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onLoginSuccess();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return baseViewLoginFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_login:
                sLoginEmail = etLoginEmail.getText().toString();
                sLoginPassword = etLoginPassword.getText().toString();
                if (validateLoginInput()) {
                    if (Helpers.isIsSoftKeyboardOpen()) {
                        softKeyboard.closeSoftKeyboard();
                    }
                    llWelcomeLogin.setVisibility(View.GONE);
                    ivWelcomeLogoMain.startAnimation(animMainLogoTransitionDown);
                }
                break;
        }
    }

    public boolean validateLoginInput() {
        boolean valid = true;
        if (sLoginEmail.trim().isEmpty()) {
            etLoginEmail.setError("Empty");
            valid = false;
        } else if (!sLoginEmail.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(sLoginEmail).matches()) {
            etLoginEmail.setError("Invalid E-Mail");
            valid = false;
        } else {
            etLoginEmail.setError(null);
        }
        if (sLoginPassword.trim().isEmpty() || sLoginPassword.length() < 6) {
            etLoginPassword.setError("Minimum 6 Characters");
            valid = false;
        } else {
            etLoginPassword.setError(null);
        }
        return valid;
    }

    public void onLoginSuccess() {
        Log.i("Login", "Success");
        AppGlobals.putUsername(sLoginEmail);
        AppGlobals.putUserPassword(sLoginPassword);
        AppGlobals.setLoggedIn(true);
        Toast.makeText(getActivity(), "Logged In", Toast.LENGTH_LONG).show();
        getActivity().onBackPressed();
    }

    public void onLoginFailed(String message) {
        Log.i("Login", "Failed");
        Helpers.showSnackBar(getView(), message, Snackbar.LENGTH_LONG, "#f44336");
        ivWelcomeLogoMain.startAnimation(animMainLogoTransitionUp);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }
}
