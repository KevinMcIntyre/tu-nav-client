package com.byteshaft.tu_nav;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.tu_nav.utils.AppGlobals;
import com.byteshaft.tu_nav.utils.Helpers;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private boolean cameraAnimatedToCurrentLocation;
    private static GoogleMap mMap = null;
    private boolean isSearchEditTextVisible;
    private Menu actionsMenu;
    private EditText etMapSearch;
    private Animation animLayoutBottomUp;
    private Animation animLayoutBottomDown;
    private Animation animLayoutMapSearchBarTopUp;
    private Animation animLayoutMapSearchBarTopDown;
    private LinearLayout llMapHireButtons;
    private boolean destinationMarkerAdded;
    private String inputMapSearch;
    private LatLng currentLatLngAuto;
    public static boolean isMapFragmentVisible;
    MenuItem actionItemCancelNavigation;
    CoordinatorLayout coordinatorLayout;
    LinearLayout llNavLoggedOut;
    LinearLayout llNavLoggedIn;

    TextView tvNavName;

    final Runnable openLocationServiceSettings = new Runnable() {
        public void run() {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    };
    final Runnable recheckLocationServiceStatus = new Runnable() {
        public void run() {
            if (!Helpers.isAnyLocationServiceAvailable()) {
                Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.this, "Location Service disabled",
                        "Enable device GPS to continue driver hiring", "Settings", "ReCheck",
                        openLocationServiceSettings, recheckLocationServiceStatus);
            } else {
                Helpers.showProgressDialog(MainActivity.this, "Acquiring current location");
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
            }
        }
    };

    Marker destinationPointMarker;
    private Boolean simpleMapView = true;
    private ImageButton btnMapRemoveDestinationMarker;
    private Button btnMapStartNavigation;
    public static String destinationPoint;
    private TextView tvDestinationAddress;
    private RoutingListener mRoutingListener;

    Button btnNavRightLogin;
    Button btnNavRightSignUp;
    Button btnNavLogOut;

    public static boolean isMainActivityRunning;
    NavigationView navigationView;
    NavigationView navigationViewRight;
    private static MainActivity sInstance;
    DrawerLayout drawer;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sInstance = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view_left);
        navigationViewRight = (NavigationView) findViewById(R.id.nav_view_right);

        btnNavRightLogin = (Button) navigationViewRight.findViewById(R.id.btn_nav_login);
        btnNavRightSignUp = (Button) navigationViewRight.findViewById(R.id.btn_nav_register);
        btnNavLogOut = (Button) navigationViewRight.findViewById(R.id.btn_nav_log_out);

        llNavLoggedOut = (LinearLayout) navigationViewRight.findViewById(R.id.ll_nav_logged_out);
        llNavLoggedIn = (LinearLayout) navigationViewRight.findViewById(R.id.ll_nav_logged_in);

        tvNavName = (TextView) navigationViewRight.findViewById(R.id.tv_nav_name);

        if (AppGlobals.isLoggedIn()) {
            llNavLoggedOut.setVisibility(View.GONE);
            llNavLoggedIn.setVisibility(View.VISIBLE);
            tvNavName.setText(AppGlobals.getUsername());
            btnNavLogOut.setVisibility(View.VISIBLE);
        } else {
            llNavLoggedOut.setVisibility(View.VISIBLE);
            llNavLoggedIn.setVisibility(View.GONE);
            btnNavLogOut.setVisibility(View.INVISIBLE);
        }

        btnNavRightLogin.setOnClickListener(this);
        btnNavRightSignUp.setOnClickListener(this);

        navigationView.setNavigationItemSelectedListener(this);
        disableNavigationViewScrollbars(navigationView);
        disableNavigationViewScrollbars(navigationViewRight);

        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        llMapHireButtons = (LinearLayout) findViewById(R.id.layout_map_hire);
        animLayoutBottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_bottom_up);
        animLayoutBottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_bottom_down);
        animLayoutMapSearchBarTopUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_top_up);
        animLayoutMapSearchBarTopDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_top_down);
        tvDestinationAddress = (TextView) findViewById(R.id.tv_map_hire_address);
        btnMapStartNavigation = (Button) findViewById(R.id.btn_map_start_navigation);
        btnMapRemoveDestinationMarker = (ImageButton) findViewById(R.id.btn_map_destination_cancel);
        etMapSearch = (EditText) findViewById(R.id.et_map_search);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.placeSnackBar);

        btnMapRemoveDestinationMarker.setOnClickListener(this);
        btnMapStartNavigation.setOnClickListener(this);
        btnNavLogOut.setOnClickListener(this);

        animLayoutBottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_bottom_up);
        animLayoutBottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_bottom_down);
        animLayoutMapSearchBarTopUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_top_up);
        animLayoutMapSearchBarTopDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_top_down);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.685677, 79.408410), 4.0f));
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.this , "Permission Denied",
                            "You need to grant permissions to use Location Services for Briver", "Settings",
                            "Exit App", Helpers.openPermissionsSettingsForMarshmallow, Helpers.exitApp);
                } else if (!Helpers.checkPlayServicesAvailability(MainActivity.this)) {
                    Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.this, "Location components missing",
                            "You need to install GooglePlayServices to continue using Briver", "Install",
                            "Exit App", Helpers.openPlayServicesInstallation, Helpers.exitApp);
                } else if (!Helpers.isAnyLocationServiceAvailable()) {
                    Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.this, "Location Service disabled",
                            "Enable device GPS to continue driver hiring", "Settings", "ReCheck",
                            openLocationServiceSettings, recheckLocationServiceStatus);
                } else {
                    Helpers.showProgressDialog(MainActivity.this, "Acquiring current location");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.getUiSettings().setCompassEnabled(true);
                }

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (isSearchEditTextVisible) {
                            setSearchBarVisibility(false);
                        }
                    }
                });

                mMap.setOnMyLocationChangeListener(myLocationChangeListener);
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(final LatLng latLng) {
                        if (isSearchEditTextVisible) {
                            setSearchBarVisibility(false);
                        }
                        if (!destinationMarkerAdded) {
                            Helpers.latLngForNavigation = new LatLng (latLng.latitude, latLng.longitude);
                            destinationPointMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.fromResource(
                                            R.mipmap.ic_map_marker_destination)));
                            llMapHireButtons.setVisibility(View.VISIBLE);
                            llMapHireButtons.startAnimation(animLayoutBottomUp);
                            destinationMarkerAdded = true;
                            btnMapRemoveDestinationMarker.setVisibility(View.VISIBLE);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    final String address = Helpers.getAddress(MainActivity.this, latLng);
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (address != null) {
                                                tvDestinationAddress.setText(address);
                                                tvDestinationAddress.setVisibility(View.VISIBLE);
                                            } else {
                                                tvDestinationAddress.setText("Address not found");
                                                tvDestinationAddress.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            }
        });

        etMapSearch.addTextChangedListener(new TextWatcher() {

            Timer textChangeTimer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textChangeTimer.cancel();
                inputMapSearch = etMapSearch.getText().toString();
                if (inputMapSearch.length() > 2 && isMapFragmentVisible) {
                    textChangeTimer = new Timer();
                    textChangeTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Geocoder geocoder = new Geocoder(MainActivity.this);
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocationName(inputMapSearch, 3);
                                if (addresses != null && !addresses.equals("")) {
                                    searchAnimateCamera(addresses);
                                }
                            } catch (Exception ignored) {

                            }
                        }
                    }, 2000);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        animLayoutMapSearchBarTopUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                etMapSearch.setText("");
                Helpers.closeSoftKeyboard(MainActivity.this);
                isSearchEditTextVisible = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animLayoutMapSearchBarTopDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                etMapSearch.requestFocus();
                Helpers.openSoftKeyboardOnEditText(MainActivity.this, etMapSearch);
                isSearchEditTextVisible = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mRoutingListener = new RoutingListener() {
            @Override
            public void onRoutingFailure() {
                Toast.makeText(MainActivity.this, "Routing Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRoutingStart() {
                Helpers.showProgressDialog(MainActivity.this, "Building Route");
            }

            @Override
            public void onRoutingSuccess(PolylineOptions polylineOptions, Route route) {
                mMap.addPolyline(new PolylineOptions()
                        .addAll(polylineOptions.getPoints())
                        .width(12)
                        .geodesic(true)
                        .color(Color.parseColor("#80000000")));

                mMap.addPolyline(new PolylineOptions()
                        .addAll(polylineOptions.getPoints())
                        .width(6)
                        .geodesic(true)
                        .color(Color.RED));

                Helpers.dismissProgressDialog();
                hideNavigationButtons(true);
                llMapHireButtons.setVisibility(View.GONE);
                actionItemCancelNavigation.setVisible(true);

                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(currentLatLngAuto)
                                .zoom(17.0f)
                                .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onRoutingCancelled() {

            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String clickCheck = "";

        if (id == R.id.nav_building_one) {
            clickCheck = "BuildingOne";
        } else if (id == R.id.nav_building_two) {
            clickCheck = "BuildingTwo";
        } else if (id == R.id.nav_building_three) {
            clickCheck = "BuildingThree";
        } else if (id == R.id.nav_building_four) {
            clickCheck = "BuildingFour";
        } else if (id == R.id.nav_building_five) {
            clickCheck = "BuildingFive";
        } else if (id == R.id.nav_building_six) {
            clickCheck = "BuildingSix";
        } else if (id == R.id.nav_building_seven) {
            clickCheck = "BuildingSeven";
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
//            item.setCheckable(true);
//            setTitle(item.getTitle());
            final String finalClickCheck = clickCheck;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, finalClickCheck, Toast.LENGTH_SHORT).show();
                }
            }, 350);
        return true;
    }

    public void disableNavigationViewScrollbars(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        actionsMenu = menu;
        actionItemCancelNavigation = menu.findItem(R.id.action_cancel_map);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel_map:
                mMap.clear();
                destinationMarkerAdded = false;
                actionItemCancelNavigation.setVisible(false);

                CameraPosition cameraPosition1 =
                        new CameraPosition.Builder()
                                .target(currentLatLngAuto)
                                .zoom(15.0f)
                                .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
                break;
            case R.id.action_search_map:
                setSearchBarVisibility(!isSearchEditTextVisible);
                break;
            case R.id.action_current_location:
                if (Helpers.isAnyLocationServiceAvailable()) {
                    if (mMap != null) {
                        if (currentLatLngAuto != null) {
                            CameraPosition cameraPosition2 =
                                    new CameraPosition.Builder()
                                            .target(currentLatLngAuto)
                                            .zoom(16.0f)
                                            .build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition2));
                        } else {
                            Toast.makeText(MainActivity.this, "Error: Location not available",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error: Map not ready",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: Location Service disabled",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_change_map:
                if (simpleMapView) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    setActionIcon(false);
                    simpleMapView = false;
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    setActionIcon(true);
                    simpleMapView = true;
                }
                return true;
            case R.id.action_profile_main:
                if (drawer.isDrawerOpen(GravityCompat.END))
                    drawer.closeDrawer(GravityCompat.END);
                else drawer.openDrawer(GravityCompat.END);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setActionIcon(boolean simpleMap) {
        MenuItem item = actionsMenu.findItem(R.id.action_change_map);
        if (actionsMenu != null) {
            if (simpleMap) {
                item.setIcon(R.mipmap.ic_action_map_satellite);
            } else {
                item.setIcon(R.mipmap.ic_action_map_simple);
            }
        }
    }

    private void setSearchBarVisibility(boolean visibility) {
        if (!visibility) {
            etMapSearch.startAnimation(animLayoutMapSearchBarTopUp);
            etMapSearch.setVisibility(View.GONE);
        } else {
            etMapSearch.setVisibility(View.VISIBLE);
            etMapSearch.startAnimation(animLayoutMapSearchBarTopDown);
        }
    }

    protected void searchAnimateCamera(List<Address> addresses) {
        final Address addressForSearch = addresses.get(0);
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng latLngSearch = new LatLng(addressForSearch.getLatitude(),
                        addressForSearch.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngSearch, 15.0f));
                mMap.clear();
                btnMapRemoveDestinationMarker.callOnClick();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        isMapFragmentVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isMapFragmentVisible = true;

        if (AppGlobals.isLoggedIn()) {
            llNavLoggedOut.setVisibility(View.GONE);
            llNavLoggedIn.setVisibility(View.VISIBLE);
            tvNavName.setText(AppGlobals.getUsername());
            btnNavLogOut.setVisibility(View.VISIBLE);
        } else {
            llNavLoggedOut.setVisibility(View.VISIBLE);
            llNavLoggedIn.setVisibility(View.GONE);
            btnNavLogOut.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_map_destination_cancel:
                if (destinationMarkerAdded) {
                    btnMapRemoveDestinationMarker.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideNavigationButtons(false);
                        }
                    }, 300);
                }
                break;
            case R.id.btn_map_start_navigation:
                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.WALKING)
                        .withListener(mRoutingListener)
                        .waypoints(currentLatLngAuto, Helpers.latLngForNavigation)
                        .build();
                routing.execute();
                break;
            case R.id.btn_nav_login:
                closeRightDrawer();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadActivity(SecondaryActivity.class, "login");
                    }
                }, 350);
                break;
            case R.id.btn_nav_register:
                closeRightDrawer();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadActivity(SecondaryActivity.class, "register");
                    }
                }, 350);
                break;
            case R.id.btn_nav_log_out:
                btnNavLogOut.setVisibility(View.INVISIBLE);
                llNavLoggedIn.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        llNavLoggedOut.setVisibility(View.VISIBLE);
                        AppGlobals.setLoggedIn(false);
                    }
                }, 350);
                break;
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            Helpers.dismissProgressDialog();
            currentLatLngAuto = new LatLng(location.getLatitude(), location.getLongitude());
            if (!cameraAnimatedToCurrentLocation) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLngAuto, 15.0f), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (AppGlobals.isMapFirstRun()) {
                            Helpers.AlertDialogMessage(MainActivity.this, "One Time Message",
                                    "Tap and hold anywhere on the MapView to set destination point and start navigation", "Ok");
                            AppGlobals.setMapFirstRun(false);
                        } else {
                            Helpers.showSnackBar(coordinatorLayout, "Tap and hold to set Destination point",
                                    Snackbar.LENGTH_LONG, "#ffffff");
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (AppGlobals.isMapFirstRun()) {
                            Helpers.AlertDialogMessage(MainActivity.this, "One Time Message",
                                    "Tap and hold anywhere on the MapView to set destination point and start navigation", "Ok");
                            AppGlobals.setMapFirstRun(false);
                        } else {
                            Helpers.showSnackBar(coordinatorLayout, "Tap and hold to set Destination point",
                                    Snackbar.LENGTH_LONG, "#ffffff");
                        }
                    }
                });
                cameraAnimatedToCurrentLocation = true;
            }
        }
    };

    private void hideNavigationButtons(boolean markerVisibility) {
        llMapHireButtons.setVisibility(View.GONE);
        llMapHireButtons.startAnimation(animLayoutBottomDown);
        if (!markerVisibility) {
            destinationPointMarker.remove();
            destinationMarkerAdded = false;
        }
        tvDestinationAddress.setText("");
        tvDestinationAddress.setVisibility(View.GONE);
    }

    public void loadActivity(Class activity, String extra) {
        Intent intent = new Intent(getBaseContext(), activity);
        intent.putExtra("FragmentName", extra);
        startActivity(intent);
    }

    private void closeRightDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
    }
}
