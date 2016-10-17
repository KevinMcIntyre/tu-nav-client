package com.TU.tu_nav;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.TU.tu_nav.fragments.LoginFragment;
import com.TU.tu_nav.fragments.RegisterFragment;

public class SecondaryActivity extends Activity {

    static FragmentManager fragmentManager;
    public static boolean isWelcomeActivityRunning;
    private static SecondaryActivity sInstance;
    String fragmentName;

    public static SecondaryActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);
        sInstance = this;
        getWindow().setBackgroundDrawableResource(R.mipmap.img_background_welcome);
        fragmentManager = getFragmentManager();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fragmentName = extras.getString("FragmentName");
        }

        if (fragmentName.equals("login")) {
            Log.i("login", "opened");
            loadLoginFragment(new LoginFragment());
        } else if (fragmentName.equals("register")) {
            loadLoginFragment(new RegisterFragment());
        }
    }

    public void loadLoginFragment(Fragment fragment) {
        android.app.FragmentTransaction tx = fragmentManager.beginTransaction();
        tx.replace(R.id.container_secondary_activity, fragment);
        tx.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
