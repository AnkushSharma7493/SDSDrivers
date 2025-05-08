package com.sds.ui.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.sds.databinding.ActivityCreateDutyBinding;
import com.sds.ui.enums.ApplicationConstant;
import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.FireStoreDutyService;
import com.sds.ui.callback.FirestoreCallback;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateDutyActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT_PICK = 101;
    private static final int REQUEST_CONTACT_PERMISSION = 102;
    private Duty duty=new Duty();
    private final Calendar dutyCalendar = Calendar.getInstance();
    private ActivityCreateDutyBinding binding;

    private FireStoreDutyService fireStoreDutyService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateDutyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editCustomerName.setOnClickListener(v -> pickContact());
        binding.editDutyDate.setOnClickListener(v -> showDatePicker());
        binding.editDutyTime.setOnClickListener(v -> showTimePicker());
        binding.btnSaveDuty.setOnClickListener(v -> saveDuty());

        fireStoreDutyService = new FireStoreDutyService(this);
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.editDutyToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Duty");

        binding.editDutyToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, REQUEST_CONTACT_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CONTACT_PICK && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};

            try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    binding.editCustomerName.setText(name);
                    binding.editCustomerPhone.setText(phone);
                }
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dutyCalendar.set(Calendar.YEAR, year);
            dutyCalendar.set(Calendar.MONTH, month);
            dutyCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            binding.editDutyDate.setText(sdf.format(dutyCalendar.getTime()));
        }, dutyCalendar.get(Calendar.YEAR), dutyCalendar.get(Calendar.MONTH), dutyCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            dutyCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dutyCalendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            binding.editDutyTime.setText(sdf.format(dutyCalendar.getTime()));
        }, dutyCalendar.get(Calendar.HOUR_OF_DAY), dutyCalendar.get(Calendar.MINUTE), true).show();
    }

    private void saveDuty() {
        int charges =Integer.valueOf(binding.editCharges.getText().toString().trim());
        int commission = (int) (charges*(ApplicationContext.getCommissionPercentage() /100.0));
        duty.setCustomerName(binding.editCustomerName.getText().toString().trim());
        duty.setCustomerPhone(binding.editCustomerPhone.getText().toString().trim());
        duty.setCustomerAddress(binding.editCustomerAddress.getText().toString().trim());
        duty.setActualCharges(charges);
        duty.setCharges(charges);
        duty.setCommission(commission);
        duty.setDutyReportingDate(AppUtility.getDutyReportingDate(binding.editDutyDate.getText().toString()));
        duty.setDutyReportingTime(AppUtility.getDutyReportingTime(binding.editDutyTime.getText().toString()));
        duty.setNotes(binding.editNote.getText().toString().trim());

        duty.setDriverName(ApplicationConstant.DRIVER_NAME_DEFAULT);
        duty.setDriverPhone(ApplicationConstant.DRIVER_PHONE_DEFAULT);

        //Set default values
        duty.setDutyStatus(DutyStatus.UNASSIGNED);
        duty.setCommissionStatus(CommissionStatus.PENDING);

        duty.setDutyRegisteredOn(AppUtility.getCurrentDateTime());

        if (duty.getCustomerName().isEmpty() || duty.getCustomerPhone().isEmpty() || duty.getCustomerAddress().isEmpty() ||
                duty.getDutyReportingDate() == null || duty.getDutyReportingTime() == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create In DB
        fireStoreDutyService.saveDuty(duty,new FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateDutyActivity.this, "Duty Created Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateDutyActivity.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
