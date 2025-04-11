package com.sds.ui.activities;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sds.R;
import com.sds.databinding.ActivityDutyDetailBinding;
import com.sds.ui.models.Duty;

import java.util.Calendar;

public class DutyDetailActivity extends AppCompatActivity {

    private ActivityDutyDetailBinding binding;
    private Calendar dutyDateTime = Calendar.getInstance();

    private Duty duty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDutyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.editToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sharma Driver Services");

        binding.editToolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.editReportingDate.setOnClickListener(v -> showDatePicker());
        binding.editReportingTime.setOnClickListener(v -> showTimePicker());

        binding.buttonSmsCustomer.setOnClickListener(v ->
                Toast.makeText(this, "Message sent to customer!", Toast.LENGTH_SHORT).show()
        );

        binding.buttonSmsDriver.setOnClickListener(v ->
                Toast.makeText(this, "Message sent to driver!", Toast.LENGTH_SHORT).show()
        );

        binding.buttonUpdateDuty.setOnClickListener(v ->
                Toast.makeText(this, "Duty Updated!", Toast.LENGTH_SHORT).show()
        );


        // Get data from previous activity
        duty = (Duty) getIntent().getSerializableExtra("duty");

        if (duty != null) {
            StringBuilder detailText = new StringBuilder();
            detailText.append("Customer Name: ").append(duty.getCustomerName()).append("\n");
            detailText.append("Customer Phone: ").append(duty.getCustomerPhone()).append("\n");
            detailText.append("Customer Address: ").append(duty.getCustomerAddress()).append("\n\n");
            detailText.append("Driver Name: ").append(duty.getDriverName()).append("\n");
            detailText.append("Driver Phone: ").append(duty.getDriverPhone()).append("\n\n");
            detailText.append("Charges: ₹").append(duty.getCharges()).append("\n");
            detailText.append("Commission: ₹").append(duty.getCommission()).append("\n");
            detailText.append("Commission Status: ").append(duty.getCommissionStatus()).append("\n");
            detailText.append("Duty Status: ").append(duty.getDutyStatus()).append("\n");
            detailText.append("Reporting Time: ").append(duty.getDutyReportingTime()).append("\n");
            detailText.append("Start Time: ").append(duty.getDutyStartTime()).append("\n");
            detailText.append("End Time: ").append(duty.getDutyEndTime());


            boolean isEditMode = getIntent().getBooleanExtra("edit_mode", false);

            if (isEditMode) {
                // Show fields in editable mode (e.g., EditTexts instead of TextViews)
                // Add Save button logic
            } else {
                // Show non-editable detail view
            }

        }
    }

    public void updateDuty(){

        // Set updated duty back to Duties Fragements.
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedDuty", duty);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            dutyDateTime.set(Calendar.YEAR, year);
            dutyDateTime.set(Calendar.MONTH, month);
            dutyDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.textReportingDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, dutyDateTime.get(Calendar.YEAR), dutyDateTime.get(Calendar.MONTH), dutyDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            dutyDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dutyDateTime.set(Calendar.MINUTE, minute);
            binding.textReportingTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, dutyDateTime.get(Calendar.HOUR_OF_DAY), dutyDateTime.get(Calendar.MINUTE), true).show();
    }
}
