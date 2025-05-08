package com.sds.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.sds.MainActivity;
import com.sds.R;
import com.sds.ui.activities.driver.DriverDutyActivity;
import com.sds.ui.services.ApplicationContext;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        ImageView logo = findViewById(R.id.logo);
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        Animation fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        logo.startAnimation(scaleAnim);
        logo.startAnimation(fadeInAnim);

        new Handler().postDelayed(() -> {

            String role=prefs.getString(ApplicationContext.DRIVER_ROLE_CACHE,null);

            if(ApplicationContext.ADMIN_ROLE.equals(role)) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else if(ApplicationContext.DRIVER_ROLE.equals(role)){
                startActivity(new Intent(SplashActivity.this, DriverDutyActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }




            finish();
        }, 2000);


    }
}