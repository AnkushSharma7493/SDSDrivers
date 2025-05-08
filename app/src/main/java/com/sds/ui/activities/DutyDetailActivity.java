package com.sds.ui.activities;
import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sds.R;
import com.sds.databinding.ActivityDutyDetailBinding;
import com.sds.ui.callback.FireStoreQueryDriverCallback;
import com.sds.ui.enums.ApplicationConstant;
import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Driver;
import com.sds.ui.models.Duty;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.FireStoreDriverService;
import com.sds.ui.services.FireStoreDutyService;
import com.sds.ui.callback.FirestoreCallback;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kotlin._Assertions;

public class DutyDetailActivity extends AppCompatActivity {

    private ActivityDutyDetailBinding binding;

    private Duty duty;
    private boolean editMode;

    private FireStoreDutyService fireStoreDutyService;

    private ProgressBar loader;

    private static final int REQUEST_CONTACT_PICK = 101;
    private static final int REQUEST_CONTACT_PERMISSION = 102;
    private final Calendar dutyCalendar = Calendar.getInstance();
    private boolean isCustomerContactSearch=false;
    private String operation=ApplicationConstant.DELETE_DUTY;
    private FireStoreDriverService fireStoreDriverService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDutyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStoreDriverService = new FireStoreDriverService(this);


        loader = binding.progressBar;
        // Get extras
        Intent intent = getIntent();
        if (intent != null) {
            editMode = intent.getBooleanExtra("edit_mode", false);
            duty = (Duty) intent.getParcelableExtra("duty");
        }

        fireStoreDutyService = new FireStoreDutyService(this);

        setupToolbar();
        populateFields();
        setupListeners();
    }

    private void setupListeners() {
        binding.editCustomerName.setOnClickListener(v -> {
            isCustomerContactSearch=true;
            pickContact();
        });
        binding.editDriverName.setOnClickListener(v -> {
            isCustomerContactSearch=false;
            pickContact();
        });
        binding.editDutyDate.setOnClickListener(v -> showDatePicker());
        binding.editDutyTime.setOnClickListener(v -> showTimePicker());
        binding.btnUpdate.setOnClickListener(v -> {
            if(ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
            ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone())){
                updateDuty();
            } else {
                verifyDriverStatus();
            }
        });
        binding.btnDelete.setOnClickListener(v -> {
            operation=ApplicationConstant.DELETE_DUTY;
            verifyPasswordAndExecute();
        });

        binding.btnMessageCustomer.setOnClickListener(v -> {
            if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                    !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                    DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                operation = ApplicationConstant.SEND_SMS_CUSTOMER;
                verifyPasswordAndExecute();
            } else {
                Toast.makeText(this, "Duty is not assigned to Driver.", Toast.LENGTH_SHORT).show();
            }
        });


        binding.btnMessageDriver.setOnClickListener(v -> {
            if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                    !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                    DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                operation = ApplicationConstant.SEND_SMS_DRIVER;
                verifyPasswordAndExecute();
            } else {
                Toast.makeText(this, "Duty is not assigned to Driver.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void verifyPasswordAndExecute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPassword = input.getText().toString();

            if (ApplicationContext.getCritalOperationPassword().equals(enteredPassword)) {
                if(ApplicationConstant.DELETE_DUTY.equals(operation)) {
                    deleteDuty();
                } else if(ApplicationConstant.SEND_SMS_CUSTOMER.equals(operation)) {
                    showCustomerEditDialog();
                } else if(ApplicationConstant.SEND_SMS_DRIVER.equals(operation)) {
                    showDriverEditDialog();
                }
            } else {
                // Password wrong
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void verifyDriverStatus(){
        try{
            loader.setVisibility(View.VISIBLE);
            fireStoreDriverService.getDriverByPhone(duty.getDriverPhone(), new FireStoreQueryDriverCallback() {

                @Override
                public void onSuccess(List<Driver> drivers) {
                    loader.setVisibility(View.GONE);
                    if(drivers.isEmpty()){
                        Toast.makeText(DutyDetailActivity.this, "No Driver Available for Phone "+duty.getDriverPhone(), Toast.LENGTH_SHORT).show();
                    } else {
                            Driver driver = drivers.get(0);
                            if(driver.isStatus()){
                                // populate driver details and update.
                                updateDuty();
                            } else {
                                //Toast.makeText(DutyDetailActivity.this, "Driver "+driver.getName()+" is not active with phone : "+duty.getDriverPhone(), Toast.LENGTH_SHORT).show();
                                showBlockDriverAlert("Driver "+driver.getName()+" is not active with phone : "+duty.getDriverPhone());
                            }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(DutyDetailActivity.this, "Error loading driver status", Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e){
            Toast.makeText(DutyDetailActivity.this, "Error while loading Driver by phone", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
        }
    }

    private void updateDuty(){
        try {
            loader.setVisibility(View.VISIBLE);
            duty.setCustomerName(binding.editCustomerName.getText().toString());
            duty.setCustomerPhone(binding.editCustomerPhone.getText().toString());
            duty.setCustomerAddress(binding.editCustomerAddress.getText().toString());
            duty.setCharges(Integer.parseInt(binding.editCharges.getText().toString()));
            duty.setCommission(Integer.parseInt(binding.editCommission.getText().toString()));
            duty.setCommissionStatus(CommissionStatus.getByStatus(binding.editCommissonStatus.getText().toString()));
            duty.setDutyReportingTime(AppUtility.getDutyReportingTime(binding.editDutyTime.getText().toString()));
            duty.setDutyReportingDate(AppUtility.getDutyReportingDate(binding.editDutyDate.getText().toString()));
            duty.setDriverName(binding.editDriverName.getText().toString());
            duty.setDriverPhone(binding.editDriverPhone.getText().toString());

            // update duty status for unassigned duty if driver detail exist.
            if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                    !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                    DutyStatus.UNASSIGNED.equals(duty.getDutyStatus())) {
                duty.setDutyStatus(DutyStatus.ASSIGNED);
            }
            // Update duty
            fireStoreDutyService.saveDuty(duty, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    loader.setVisibility(View.GONE);

                    if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                            !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                            DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                        // Show confirmation dialog
                        new AlertDialog.Builder(DutyDetailActivity.this)
                                .setTitle("Send SMS?")
                                .setMessage("Duty is assigned to driver :"+duty.getDriverName()+" Do you want to send SMS to customer and driver ?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // ask admin to send sms to driver and customer both.
                                    showDualSmsEditDialog();
                                })
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        Toast.makeText(DutyDetailActivity.this, "Duty Updated Successfully, But NOT ASSIGNED", Toast.LENGTH_SHORT).show();
                        finish(); // return to previous screen
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(DutyDetailActivity.this, "Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    finish(); // return to previous screen
                }
            });
        } catch (Exception e){
            loader.setVisibility(View.GONE);
            Toast.makeText(DutyDetailActivity.this, "Error occured while Update Duty"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void showDualSmsEditDialog() {
        String smsCustomer="Hi "+duty.getCustomerName()+", Please find the Driver Details \n Name : "+duty.getDriverName()+"\n"+
                "Phone : "+duty.getDriverPhone()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        String smsDriver="Hi "+duty.getDriverName()+", Please find the Duty Details. \n Name : "+duty.getCustomerName()+"\n"+
                "Phone : "+duty.getCustomerPhone()+"\n"+
                "Address : "+duty.getCustomerAddress()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        // Create a vertical LinearLayout to hold both inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Label for Customer SMS
        TextView customerLabel = new TextView(this);
        customerLabel.setText("Customer SMS");
        customerLabel.setTextSize(16);
        customerLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(customerLabel);

        // EditText for customer SMS
        final EditText customerInput = new EditText(this);
        customerInput.setHint("Customer SMS");
        customerInput.setText(smsCustomer);
        layout.addView(customerInput);

        // Space between inputs
        layout.addView(new Space(this), new LinearLayout.LayoutParams(0, 30));

        // Label for Driver SMS
        TextView driverLabel = new TextView(this);
        driverLabel.setText("Driver SMS");
        driverLabel.setTextSize(16);
        driverLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(driverLabel);

        // EditText for driver SMS
        final EditText driverInput = new EditText(this);
        driverInput.setHint("Driver SMS");
        driverInput.setText(smsDriver);
        layout.addView(driverInput);

        // Show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit SMS Messages")
                .setView(layout)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String customerMessage = customerInput.getText().toString().trim();
                    String driverMessage = driverInput.getText().toString().trim();

                    sendSms(duty.getCustomerPhone(), customerMessage);
                    sendSms(duty.getDriverPhone(), driverMessage);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCustomerEditDialog() {
        String smsCustomer="Hi " +duty.getCustomerName()+", Please find the Driver Details \n Name : "+duty.getDriverName()+"\n"+
                "Phone : "+duty.getDriverPhone()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        // Create a vertical LinearLayout to hold both inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Label for Customer SMS
        TextView customerLabel = new TextView(this);
        customerLabel.setText("Customer SMS");
        customerLabel.setTextSize(16);
        customerLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(customerLabel);

        // EditText for customer SMS
        final EditText customerInput = new EditText(this);
        customerInput.setHint("Customer SMS");
        customerInput.setText(smsCustomer);
        layout.addView(customerInput);

        // Space between inputs
        layout.addView(new Space(this), new LinearLayout.LayoutParams(0, 30));

        // Show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Customer SMS")
                .setView(layout)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String customerMessage = customerInput.getText().toString().trim();
                    sendSms(duty.getCustomerPhone(), customerMessage);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDriverEditDialog() {
        String smsDriver="Hi " +duty.getDriverName()+", Please find the Duty Details. \n Name : "+duty.getCustomerName()+"\n"+
                "Phone : "+duty.getCustomerPhone()+"\n"+
                "Address : "+duty.getCustomerAddress()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        // Create a vertical LinearLayout to hold both inputs
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Label for Driver SMS
        TextView driverLabel = new TextView(this);
        driverLabel.setText("Driver SMS");
        driverLabel.setTextSize(16);
        driverLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(driverLabel);

        // EditText for driver SMS
        final EditText driverInput = new EditText(this);
        driverInput.setHint("Driver SMS");
        driverInput.setText(smsDriver);
        layout.addView(driverInput);

        // Show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit SMS Messages")
                .setView(layout)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String driverMessage = driverInput.getText().toString().trim();
                    sendSms(duty.getDriverPhone(), driverMessage);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showBlockDriverAlert(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Driver Blocked");
        builder.setMessage(msg);

        // Set a block icon from drawable
        builder.setIcon(R.drawable.blockuser); // Use a red stop or block icon

        // OK button
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Create and show dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void sendSms(String phoneNumber, String message) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 2);
            } else {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, AppUtility.removeEmojis(message), null, null);
                Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            Toast.makeText(this, "Failed sending SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDuty(){
        try{
            loader.setVisibility(View.VISIBLE);
            // Delete duty
            fireStoreDutyService.deleteDuty(duty.getDutyId(),new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(DutyDetailActivity.this, "Duty Deleted Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // return to previous screen
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(DutyDetailActivity.this, "Deletion Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    finish(); // return to previous screen
                }
            });
        } catch (Exception e){
            loader.setVisibility(View.GONE);
            Toast.makeText(DutyDetailActivity.this, "Error occured while delete duty"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void pickContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
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

                    if(isCustomerContactSearch) {
                        binding.editCustomerName.setText(name);
                        binding.editCustomerPhone.setText(phone);
                    } else {
                        binding.editDriverName.setText(name);
                        binding.editDriverPhone.setText(phone);
                    }
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

    private void setupToolbar() {
        setSupportActionBar(binding.editDutyToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Duty Details");

        binding.editDutyToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void populateFields() {
        try{
        binding.editCustomerName.setText(duty.getCustomerName());
        binding.editCustomerPhone.setText(duty.getCustomerPhone());
        binding.editCustomerAddress.setText(duty.getCustomerAddress());

        binding.editDriverName.setText(duty.getDriverName());
        binding.editDriverPhone.setText(duty.getDriverPhone());

        binding.editCharges.setText(String.valueOf(duty.getActualCharges()));
        binding.editCommission.setText(String.valueOf(duty.getCommission()));

        binding.editDutyDate.setText(AppUtility.formatDate(duty.getDutyReportingDate()));
        binding.editDutyTime.setText(AppUtility.formatTime(duty.getDutyReportingTime()));

//        binding.editDutyStatus.setOnClickListener(v -> {
//            PopupMenu popup = new PopupMenu(this, v);
//            for(String dutyStatus : DutyStatus.getStringValues()){
//                popup.getMenu().add(dutyStatus);
//            }
//
//            popup.setOnMenuItemClickListener(item -> {
//                binding.editDutyStatus.setText(item.getTitle());
//                return true;
//            });
//
//            popup.show();
//        });
//        binding.editDutyStatus.setText(duty.getDutyStatus().name());

        binding.editCommissonStatus.setText(duty.getCommissionStatus().name());

        binding.editCommissonStatus.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            for(String commissonStatus : CommissionStatus.getStringValues()){
                popup.getMenu().add(commissonStatus);
            }

            popup.setOnMenuItemClickListener(item -> {
                binding.editCommissonStatus.setText(item.getTitle());
                return true;
            });

            popup.show();
        });
        } catch (Exception e){
            Toast.makeText(DutyDetailActivity.this, "Error occured while populate fields"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }




}
