package com.sds.driver.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.sds.driver.R;
import com.sds.driver.databinding.ActivityDriverDutyBinding;
import com.sds.driver.ui.fragements.driver.CommissionPaymentFragment;
import com.sds.driver.ui.fragements.driver.DriverDutyFragment;
import com.sds.driver.ui.fragements.driver.DriverProfileFragment;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreDutyBackgroundService;

public class DriverDutyActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private ActivityDriverDutyBinding binding;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverDutyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start foreground service
        Intent serviceIntent = new Intent(this, FireStoreDutyBackgroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        loader = binding.progressBar;
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navViewDriver;

        // set the toolbar
        setToolbar();

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);
        String driverPhone = prefs.getString(ApplicationContext.DRIVER_PHONE_CACHE,"");
        if(driverPhone.isEmpty()){
            Toast.makeText(this, "App is not in-sync, Please login again", Toast.LENGTH_SHORT).show();
            logout();
        }

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new DriverDutyFragment());
            navigationView.setCheckedItem(com.sds.driver.R.id.nav_driver_duties);
        }

        //Call logic for fragement navigations
        navigationView.setNavigationItemSelectedListener(this::handleNavigationFragement);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setToolbar(){
        setPhoneTopBarBlack();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setTitle("SDS Driver App");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void setPhoneTopBarBlack(){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK); // Set black background

        // Ensure light (white) icons:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(0); // Clears flags so icons are white
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    private boolean handleNavigationFragement(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_driver_duties) {
            selectedFragment = new DriverDutyFragment();
        } else if (id == R.id.nav_pay_commission) {
            selectedFragment = new CommissionPaymentFragment();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new DriverProfileFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(){
        prefs.edit().remove(ApplicationContext.DRIVER_PHONE_CACHE).apply();
        prefs.edit().remove(ApplicationContext.DRIVER_PIN_CACHE).apply();
        prefs.edit().remove(ApplicationContext.DRIVER_ROLE_CACHE).apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
