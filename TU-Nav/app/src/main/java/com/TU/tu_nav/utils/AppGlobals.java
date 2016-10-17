package com.TU.tu_nav.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppGlobals extends Application {

    private static final String LOGGED_IN = "logged_in";
    private static final String USER_NAME = "user_name";
    private static final String PERSON_NAME = "person_name";
    private static final String USER_PASSWORD = "user_password";
    private static final String FIRST_RUN_MAP = "first_run_map";
    private static Context sContext;
    private static SharedPreferences sPreferences;

    public static Context getContext() {
        return sContext;
    }

    public static boolean isLoggedIn() {
        return sPreferences.getBoolean(LOGGED_IN, false);
    }

    public static void setLoggedIn(boolean loggedIn) {
        sPreferences.edit().putBoolean(LOGGED_IN, loggedIn).apply();
    }

    public static void putUsername(String username) {
        sPreferences.edit().putString(USER_NAME, username).apply();
    }

    public static boolean isMapFirstRun() {
        return sPreferences.getBoolean(FIRST_RUN_MAP, true);
    }

    public static void setMapFirstRun(boolean firstRunMap) {
        sPreferences.edit().putBoolean(FIRST_RUN_MAP, firstRunMap).apply();
    }

    public static String getUsername() {
        return sPreferences.getString(USER_NAME, null);
    }


    public static String getPeronName() {
        return sPreferences.getString(PERSON_NAME, null);
    }

    public static void putPersonName(String name) {
        sPreferences.edit().putString(PERSON_NAME, name).apply();
    }

    public static void putUserPassword(String password) {
        sPreferences.edit().putString(USER_PASSWORD, password).apply();
    }

    public static String getUserPassword() {
        return sPreferences.getString(USER_PASSWORD, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
