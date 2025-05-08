package com.sds.ui.activities.driver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.ListenerRegistration;
import com.sds.R;
import com.sds.databinding.ActivityDriverDutyBinding;
import com.sds.ui.activities.LoginActivity;
import com.sds.ui.adaptors.DriverDutyAdapter;
import com.sds.ui.callback.FireStoreQueryDutyCallback;
import com.sds.ui.listener.DutyListeners;
import com.sds.ui.models.Duty;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.services.FireStoreDutyService;

import java.util.ArrayList;
import java.util.List;

public class DriverDutyActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    private String phoneToCall;
    private RecyclerView recyclerView;
    private DriverDutyAdapter driverDutyAdapter;
    private FireStoreDutyService fireStoreDutyService;
    private ProgressBar loader;
    private DutyListeners listener = new DutyListeners() {
        @Override
        public void onCustomerPhoneClick(String phoneNumber) {
            makePhoneCall(phoneNumber);
        }

        @Override
        public void onDriverPhoneClick(String phoneNumber) {
            makePhoneCall(phoneNumber);
        }

        @Override
        public void onCustomerSmsClick(Duty duty) {
            String msg="Name : "+duty.getDriverName()+"\n"+
                    "Phone : "+duty.getDriverPhone()+"\n"+
                    "Duty Time : "+ AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
            sendSms(duty.getCustomerPhone(), "Please find the Driver details : \n"+msg);
        }

        @Override
        public void onDriverSmsClick(Duty duty) {
            String msg="Name : "+duty.getCustomerName()+"\n"+
                    "Phone : "+duty.getCustomerPhone()+"\n"+
                    "Address : "+duty.getCustomerAddress()+"\n"+
                    "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
            sendSms(duty.getDriverPhone(), "Please find Duty details \n "+msg);
        }

        @Override
        public void onDriverDutyListEmpty(){
            binding.noDutyView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDrivrDutyListReFill(){
            binding.noDutyView.setVisibility(View.GONE);
        }
    };
    private ActivityDriverDutyBinding binding;

    private ListenerRegistration dutyListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverDutyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        loader = binding.progressBar;
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer toggle (hamburger menu)
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationClick(item);
            return true;
        });

        fireStoreDutyService = new FireStoreDutyService(this);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recycler_driver_duties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driverDutyAdapter=new DriverDutyAdapter(listener,fireStoreDutyService);

    }


    @Override
    public void onResume() {
        super.onResume();
        String driverPhone = prefs.getString(ApplicationContext.DRIVER_PHONE_CACHE,"");
        if(driverPhone.isEmpty()){
            Toast.makeText(this, "App is not in-sync, Please login again", Toast.LENGTH_SHORT).show();
            logout();
        } else {
            loadDutyByDriverPhoneAndStatus(driverPhone);
        }
    }


    private void handleNavigationClick(@NonNull MenuItem item) {
        Intent intent = null;
        if(R.id.nav_duties==item.getItemId()){
            intent = new Intent(this, DriverDutyActivity.class);
        } else if(R.id.nav_pay_commission==item.getItemId()){
            intent = new Intent(this, DriverDutyListActivity.class);
        }else if(R.id.nav_profile==item.getItemId()){
            intent = new Intent(this, DriverProfileActivity.class);
        }else if(R.id.nav_logout==item.getItemId()){
            logout();
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);

        // Close drawer after clicking
        drawerLayout.closeDrawers();
    }

    public void logout(){
        prefs.edit().remove(ApplicationContext.DRIVER_PHONE_CACHE).apply();
        prefs.edit().remove(ApplicationContext.DRIVER_PIN_CACHE).apply();
        prefs.edit().remove(ApplicationContext.DRIVER_ROLE_CACHE).apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDutyByDriverPhoneAndStatus(String phone){
        loader.setVisibility(View.VISIBLE);
        this.dutyListener=fireStoreDutyService.getDutiesByDriverPhoneFireStore(phone, new FireStoreQueryDutyCallback() {
            @Override
            public void onSuccess(List<Duty> duties) {
                loader.setVisibility(View.GONE);
                if(duties.isEmpty()){
                    driverDutyAdapter.setDutyList(new ArrayList<>());
                    recyclerView.setAdapter(driverDutyAdapter);
                    binding.noDutyView.setVisibility(View.VISIBLE);
                    Toast.makeText(DriverDutyActivity.this, "No Duty Available Now.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.noDutyView.setVisibility(View.GONE);
                    driverDutyAdapter.setDutyList(duties);
                    recyclerView.setAdapter(driverDutyAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                loader.setVisibility(View.GONE);
                driverDutyAdapter.setDutyList(new ArrayList<>());
                recyclerView.setAdapter(driverDutyAdapter);
                Toast.makeText(DriverDutyActivity.this, "Error loading duties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makePhoneCall(String phoneNumber) {
        this.phoneToCall = phoneNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 101);
        } else {
            startCallIntent(phoneNumber);
        }
    }

    private void startCallIntent(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dutyListener != null) {
            dutyListener.remove();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (phoneToCall != null) {
                startCallIntent(phoneToCall);
            }
        } else {
            Toast.makeText(this, "CALL PHONE permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
