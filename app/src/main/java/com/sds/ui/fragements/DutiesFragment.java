package com.sds.ui.fragements;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sds.R;
import com.sds.databinding.FragmentDutiesBinding;
import com.sds.ui.activities.CreateDutyActivity;
import com.sds.ui.callback.FirestoreCallback;
import com.sds.ui.enums.ApplicationConstant;
import com.sds.ui.listener.DutyListeners;
import com.sds.ui.adaptors.DutyAdapter;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.callback.FireStoreQueryDutyCallback;
import com.sds.ui.services.FireStoreDutyService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DutiesFragment extends Fragment {
    private String phoneToCall;
    private String selectedStatus = DutyStatus.UNASSIGNED.getStatus();
    private DutyAdapter dutyAdapter;
    private FragmentDutiesBinding binding;
    private FireStoreDutyService fireStoreDutyService;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private Calendar calendar=Calendar.getInstance();;
    private Date lastSelectedDate;
    private String[] dutyStatusArray =DutyStatus.getStringValues();

    private String operation;

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
            if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                    !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                    DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                operation = ApplicationConstant.SEND_SMS_CUSTOMER;
                verifyPasswordAndExecute(duty);
            } else {
                Toast.makeText(requireContext(), "Duty is not assigned to Driver.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDriverSmsClick(Duty duty) {
            if(!ApplicationConstant.DRIVER_PHONE_DEFAULT.equals(duty.getDriverPhone()) &&
                    !ApplicationConstant.DRIVER_NAME_DEFAULT.equals(duty.getDriverName()) &&
                    DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                operation = ApplicationConstant.SEND_SMS_DRIVER;
                verifyPasswordAndExecute(duty);
            } else {
                Toast.makeText(requireContext(), "Duty is not assigned to Driver.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void updateDuty(Duty duty) {
                operation = ApplicationConstant.UPDATE_DUTY;
                verifyPasswordAndExecute(duty);

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDutiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ApplicationContext.isDateCriteria()){
            loadDutyByDate(lastSelectedDate==null?AppUtility.getCurrentDateTime():lastSelectedDate);
        } else {
            loadDutyByStatus(DutyStatus.getByStatus(selectedStatus));
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loader = binding.progressBar;
        fireStoreDutyService = new FireStoreDutyService(requireContext());

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_duties);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dutyAdapter=new DutyAdapter(listener);

        // Date Picker for Select Date nd load duties of selected date.
        binding.btnSelectDate.setOnClickListener(v -> loadSelectedDate());

        // On click of status button load duties by status
        binding.btnStatus.setOnClickListener(v -> loadSelectedStatus());

        // FAB Button
        FloatingActionButton fab = view.findViewById(R.id.fab_add_duty);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateDutyActivity.class);
            startActivity(intent);
        });
    }


    private void loadSelectedStatus(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Duty Status");

        builder.setItems(dutyStatusArray, (dialog, which) -> {
            selectedStatus = dutyStatusArray[which];
            // Filter based on selected status
            ApplicationContext.setDateCriteria(false);
            ApplicationContext.setSkipCache(true);
            loadDutyByStatus(DutyStatus.getByStatus(selectedStatus));
            ApplicationContext.setSkipCache(false);
        });

        builder.show();
    }

    private void loadSelectedDate(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (viewDatePicker, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Filter list based on date
                    boolean flag= ApplicationContext.isSkipCache();
                    ApplicationContext.setSkipCache(true);
                    ApplicationContext.setDateCriteria(true);
                    lastSelectedDate=selectedDate.getTime();
                    loadDutyByDate(selectedDate.getTime());
                    ApplicationContext.setSkipCache(flag);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadDutyByDate(Date date){
        loader.setVisibility(View.VISIBLE);

        fireStoreDutyService.getDutiesByReportingDate(date, new FireStoreQueryDutyCallback() {
            @Override
            public void onSuccess(List<Duty> duties) {
                loader.setVisibility(View.GONE);
                if(duties.isEmpty()){
                    dutyAdapter.setDuties(new ArrayList<>());
                    recyclerView.setAdapter(dutyAdapter);
                    Toast.makeText(requireContext(), "No Duty Available for Date "+lastSelectedDate, Toast.LENGTH_SHORT).show();
                } else {
                    dutyAdapter.setDuties(duties);
                    recyclerView.setAdapter(dutyAdapter);
                }
                binding.btnSelectDate.setText(AppUtility.formatDate(lastSelectedDate==null?AppUtility.getCurrentDateTime():lastSelectedDate) +" ("+duties.size()+")");
            }

            @Override
            public void onFailure(Exception e) {
                loader.setVisibility(View.GONE);
                binding.btnSelectDate.setText(AppUtility.formatDate(lastSelectedDate==null?AppUtility.getCurrentDateTime():lastSelectedDate)+" (0)");
                Toast.makeText(requireContext(), "Error loading duties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDutyByStatus(DutyStatus status){
        loader.setVisibility(View.VISIBLE);
        fireStoreDutyService.getDutiesByStatus(status, new FireStoreQueryDutyCallback() {
            @Override
            public void onSuccess(List<Duty> duties) {
                loader.setVisibility(View.GONE);
                if(duties.isEmpty()){
                    dutyAdapter.setDuties(new ArrayList<>());
                    recyclerView.setAdapter(dutyAdapter);
                    Toast.makeText(requireContext(), "No Duty Available for Status "+status.getStatus(), Toast.LENGTH_SHORT).show();
                } else {
                    dutyAdapter.setDuties(duties);
                    recyclerView.setAdapter(dutyAdapter);
                }
                binding.btnStatus.setText(selectedStatus +" ("+duties.size()+")");
            }

            @Override
            public void onFailure(Exception e) {
                loader.setVisibility(View.GONE);
                dutyAdapter.setDuties(new ArrayList<>());
                recyclerView.setAdapter(dutyAdapter);
                binding.btnStatus.setText(selectedStatus +" (0)");
                Toast.makeText(requireContext(), "Error loading duties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDuty(Duty duty){
        try{
            // Update duty
            fireStoreDutyService.saveDuty(duty, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Duty Updated Successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(requireContext(), "Error occured while Update Duty"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
            Toast.makeText(getContext(), "CALL PHONE permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyPasswordAndExecute(Duty duty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPassword = input.getText().toString();

            if (ApplicationContext.getCritalOperationPassword().equals(enteredPassword)) {
                if(ApplicationConstant.SEND_SMS_CUSTOMER.equals(operation)) {
                    showCustomerEditDialog(duty);
                } else if(ApplicationConstant.SEND_SMS_DRIVER.equals(operation)) {
                    showDriverEditDialog(duty);
                } else if(ApplicationConstant.UPDATE_DUTY.equals(operation)) {
                    updateDuty(duty);
                }
            } else {
                // Password wrong
                Toast.makeText(requireContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showCustomerEditDialog(Duty duty) {
        String smsCustomer="Hi " +duty.getCustomerName()+", Please find the Driver Details \n Name : "+duty.getDriverName()+"\n"+
                "Phone : "+duty.getDriverPhone()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        // Create a vertical LinearLayout to hold both inputs
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Label for Customer SMS
        TextView customerLabel = new TextView(requireContext());
        customerLabel.setText("Customer SMS");
        customerLabel.setTextSize(16);
        customerLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(customerLabel);

        // EditText for customer SMS
        final EditText customerInput = new EditText(requireContext());
        customerInput.setHint("Customer SMS");
        customerInput.setText(smsCustomer);
        layout.addView(customerInput);

        // Space between inputs
        layout.addView(new Space(requireContext()), new LinearLayout.LayoutParams(0, 30));

        // Show the dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Customer SMS")
                .setView(layout)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String customerMessage = customerInput.getText().toString().trim();
                    sendSms(duty.getCustomerPhone(), customerMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDriverEditDialog(Duty duty) {
        String smsDriver="Hi " +duty.getDriverName()+", Please find the Duty Details. \n Name : "+duty.getCustomerName()+"\n"+
                "Phone : "+duty.getCustomerPhone()+"\n"+
                "Address : "+duty.getCustomerAddress()+"\n"+
                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());

        // Create a vertical LinearLayout to hold both inputs
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Label for Driver SMS
        TextView driverLabel = new TextView(requireContext());
        driverLabel.setText("Driver SMS");
        driverLabel.setTextSize(16);
        driverLabel.setTypeface(null, Typeface.BOLD);
        layout.addView(driverLabel);

        // EditText for driver SMS
        final EditText driverInput = new EditText(requireContext());
        driverInput.setHint("Driver SMS");
        driverInput.setText(smsDriver);
        layout.addView(driverInput);

        // Show the dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit SMS Messages")
                .setView(layout)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String driverMessage = driverInput.getText().toString().trim();
                    sendSms(duty.getDriverPhone(), driverMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, 2);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, AppUtility.removeEmojis(message), null, null);
            Toast.makeText(requireContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall(String phoneNumber) {
        this.phoneToCall = phoneNumber;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE)
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

}
