package com.sds.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.sds.databinding.ActivityCreateDriverBinding;
import com.sds.ui.models.Driver;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.services.FireStoreDriverService;
import com.sds.ui.callback.FirestoreCallback;
import java.util.Calendar;

public class CreateDriverActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT_PICK = 101;
    private static final int REQUEST_CONTACT_PERMISSION = 102;
    private Driver driver;
    private final Calendar dutyCalendar = Calendar.getInstance();
    private ActivityCreateDriverBinding binding;
    private FireStoreDriverService fireStoreDriverService;
    private boolean editMode=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editDriverName.setOnClickListener(v -> pickContact());
        binding.editDOB.setOnClickListener(v -> showDatePicker());
        binding.btnSaveDriver.setOnClickListener(v -> saveDriver());

        fireStoreDriverService = new FireStoreDriverService(this);

        // Get extras
        Intent intent = getIntent();
        if (intent != null) {
            editMode = intent.getBooleanExtra("edit_mode", false);
            driver = (Driver) intent.getParcelableExtra("driver");
        }

        if(editMode){
            populateFields();
            binding.btnSaveDriver.setText("Update Driver");
        } else {
            driver = new Driver();
            binding.btnSaveDriver.setText("Create Driver");
        }

        binding.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Driver marked Active" : "Driver marked Inactive";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        binding.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Password");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String enteredPassword = input.getText().toString();

                if (ApplicationContext.getCritalOperationPassword().equals(enteredPassword)) {
                    deleteDriver();
                } else {
                    // Password wrong
                    Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();

        });

        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.editDriverToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle( editMode?"Edit Driver Account": "Create Driver Account");
        binding.editDriverToolbar.setNavigationOnClickListener(v -> onBackPressed());
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

                    binding.editDriverName.setText(name);
                    binding.editDriverPhone.setText(phone);
                }
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dutyCalendar.set(Calendar.YEAR, year);
            dutyCalendar.set(Calendar.MONTH, month);
            dutyCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            binding.editDOB.setText(AppUtility.formatDate(dutyCalendar.getTime()));
        }, dutyCalendar.get(Calendar.YEAR), dutyCalendar.get(Calendar.MONTH), dutyCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveDriver() {
        try {
            driver.setName(binding.editDriverName.getText().toString().trim());
            driver.setPhone(binding.editDriverPhone.getText().toString().trim());
            driver.setEmergencyPhone(binding.editDriverPhone2.getText().toString().trim());
            driver.setCurrentAddress(binding.editDriverCurrentAddress.getText().toString().trim());
            driver.setNativeAddress(binding.editDriverNativeAddress.getText().toString().trim());
            driver.setDOB(AppUtility.getDutyReportingDate(binding.editDOB.getText().toString().trim()));
            driver.setAadhaarNo(binding.editAadhaar.getText().toString().trim());
            driver.setLicenceNo(binding.editLicenceNumber.getText().toString().trim());
            driver.setDriverExperience(Integer.parseInt(binding.editDrivingExperience.getText().toString().trim()));
            driver.setNote(binding.editNote.getText().toString().trim());

            driver.setRole(ApplicationContext.DRIVER_ROLE);
            driver.setJoiningDate(AppUtility.getCurrentDateTime());
            driver.setStatus(true);
            driver.setStatus(binding.switchStatus.isChecked());

            //generate 4 digit PIN
            if(driver.getPin()==null) {
                driver.setPin(AppUtility.generatePin());
            }

            //verify all required fields available
            if (driver.getName().isEmpty() || driver.getPhone().isEmpty() || driver.getEmergencyPhone().isEmpty() || driver.getCurrentAddress().isEmpty() ||
                    driver.getNativeAddress().isEmpty() || driver.getDOB() == null || driver.getAadhaarNo().isEmpty() || driver.getLicenceNo().isEmpty() ||
                    driver.getDriverExperience() == 0) {
                Toast.makeText(this, "Please provide all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create In DB
            fireStoreDriverService.saveDriver(driver, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    // Send OTP to driver for login.
                    if(driver.getPin()==null) {
                        String msg = "Dear " + driver.getName() + ", your service account created successfully.\n Please login SDS app using your phone number and PIN :" + driver.getPin();
                        sendSms(driver.getPhone(), msg);
                    } else {
                        Toast.makeText(CreateDriverActivity.this, "Account updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CreateDriverActivity.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch(Exception e){
            Toast.makeText(CreateDriverActivity.this, "Failed while saving Driver.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Driver Account created and otp sent successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateFields() {
        try{
            binding.editDriverName.setText(driver.getName());
            binding.editDriverPhone.setText(driver.getPhone());
            binding.editDriverPhone2.setText(driver.getEmergencyPhone());
            binding.editDriverCurrentAddress.setText(driver.getCurrentAddress());
            binding.editDriverNativeAddress.setText(driver.getNativeAddress());
            binding.editDOB.setText(AppUtility.formatDate(driver.getDOB()));

            binding.editAadhaar.setText(driver.getAadhaarNo());
            binding.editLicenceNumber.setText(driver.getLicenceNo());
            binding.editDrivingExperience.setText(String.valueOf(driver.getDriverExperience()));
            binding.editNote.setText(driver.getNote());

            binding.switchStatus.setChecked(driver.isStatus());

        } catch (Exception e){
            Toast.makeText(CreateDriverActivity.this, "Error occured while populate fields"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDriver(){
        try{
            // Delete duty
            fireStoreDriverService.deleteDriver(driver.getPhone(),new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CreateDriverActivity.this, "Driver Deleted Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // return to previous screen
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(CreateDriverActivity.this, "Deletion Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    finish(); // return to previous screen
                }
            });
        } catch (Exception e){
            Toast.makeText(CreateDriverActivity.this, "Error occured while delete driver account"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}