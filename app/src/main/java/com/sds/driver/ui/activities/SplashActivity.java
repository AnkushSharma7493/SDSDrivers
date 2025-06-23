package com.sds.driver.ui.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.sds.driver.R;
import com.sds.driver.ui.callback.FireStoreQueryAppSettingsCallback;
import com.sds.driver.ui.models.AppSettings;
import com.sds.driver.ui.services.AppUtility;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreAppSettingService;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private FireStoreAppSettingService fireStoreAppSettingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash_screen);
        fireStoreAppSettingService = new FireStoreAppSettingService(this);
        loadConfiguration();
        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        ImageView logo = findViewById(R.id.logo);
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        logo.startAnimation(scaleAnim);
        logo.startAnimation(fadeInAnim);

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        new Handler().postDelayed(() -> {
            String role=prefs.getString(ApplicationContext.DRIVER_ROLE_CACHE,null);
           if(ApplicationContext.DRIVER_ROLE.equals(role)){
                startActivity(new Intent(SplashActivity.this, DriverDutyActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000);


    }

    private void loadConfiguration() {
        try {
            fireStoreAppSettingService.getAppSettings(new FireStoreQueryAppSettingsCallback() {

                @Override
                public void onSuccess(List<AppSettings> appSettings) {
                    AppUtility.updateApplicationSetting(appSettings.get(0));
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SplashActivity.this, "Error loading Configuration", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(SplashActivity.this, "Exception loading Configuration", Toast.LENGTH_SHORT).show();
        }
    }
}