package com.sds.driver.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.sds.driver.databinding.ActivityDriverDutyDetailBinding;
import com.sds.driver.ui.enums.CommissionStatus;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.AppUtility;

public class DriverDutyDetailActivity extends AppCompatActivity {

    private Duty duty;

    private ActivityDriverDutyDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverDutyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Get extras
        Intent intent = getIntent();
        if (intent != null) {
            duty = (Duty) intent.getParcelableExtra("duty");
        }

        setupToolbar();
        populateFields();

    }

    private void setupToolbar() {
        setPhoneTopBarBlack();
        setSupportActionBar(binding.dutyToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Duty Details");
        binding.dutyToolbar.getNavigationIcon().setTint(Color.WHITE);
        binding.dutyToolbar.setTitleTextColor(Color.WHITE);
        binding.dutyToolbar.setNavigationOnClickListener(v -> onBackPressed());
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

    private void populateFields() {
        try{
            binding.customerName.setText(duty.getCustomerName());
            binding.customerPhone.setText(duty.getCustomerPhone());
            binding.customerAddress.setText(duty.getCustomerAddress());

            binding.charges.setText(String.valueOf(duty.getCharges()));
            binding.actualCharges.setText(String.valueOf(duty.getActualCharges()));
            binding.commission.setText(String.valueOf(duty.getCommission()));

            binding.dutyDate.setText(AppUtility.formatDate(duty.getDutyReportingDate()));
            binding.dutyTime.setText(AppUtility.formatTime(duty.getDutyReportingTime()));

            binding.startDutyTime.setText(AppUtility.formatTime(duty.getDutyStartTime()));
            binding.completeDutyTime.setText(AppUtility.formatTime(duty.getDutyEndTime()));

            binding.dutyStatus.setText(duty.getDutyStatus().name());
            binding.commissonStatus.setText(duty.getCommissionStatus().name());

            if(duty.getCommissionStatus()== CommissionStatus.RECEIVED){
                binding.btnPayCommission.setVisibility(View.GONE);
            }

        } catch (Exception e){
            Toast.makeText(DriverDutyDetailActivity.this, "Error occured while populate fields"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}