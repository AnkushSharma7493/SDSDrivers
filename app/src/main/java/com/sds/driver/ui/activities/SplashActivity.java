package com.sds.driver.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.sds.driver.R;
import com.sds.driver.ui.services.ApplicationContext;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash_screen);

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        ImageView logo = findViewById(R.id.logo);
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        logo.startAnimation(scaleAnim);
        logo.startAnimation(fadeInAnim);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
}