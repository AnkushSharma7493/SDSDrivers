package com.sds.driver.ui.fragements.driver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sds.driver.R;
import com.sds.driver.databinding.FragmentDriverDutyBinding;
import com.sds.driver.ui.adaptors.DriverDutyAdapter;
import com.sds.driver.ui.callback.FirestoreCallback;
import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.listener.DutyListeners;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.AppUtility;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreDutyBackgroundService;
import com.sds.driver.ui.services.FireStoreDutyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DriverDutyFragment extends Fragment {

    private String phoneToCall;
    private FireStoreDutyBackgroundService fireStoreBackgroundDutyService;
    private FireStoreDutyService fireStoreDutyService;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private DriverDutyAdapter driverDutyAdapter;
    private Duty duty;
    private int position;
    private DutyStatus previousStatus;
    private DutyStatus nextStatus;
    private DutyListeners listener = new DutyListeners() {
        @Override
        public void onCustomerPhoneClick(String phoneNumber) {
            makePhoneCall(phoneNumber);
        }

        @Override
        public void updateDuty(Duty inDuty,int inPosition,DutyStatus status){
            previousStatus=inDuty.getDutyStatus();
            nextStatus=status;
            duty=inDuty;
            position=inPosition;

            if(status==DutyStatus.ACCEPTED){
                duty.setDutyStatus(DutyStatus.ACCEPTED);
                updateDutyDetails();
            } else if(status==DutyStatus.REJECTED){
                duty.setDutyStatus(DutyStatus.REJECTED);
                updateDutyDetails();
            } else if(status==DutyStatus.INPROGRESS){
                duty.setDutyStartTime(AppUtility.getCurrentDateTime());
                duty.setDutyStatus(DutyStatus.INPROGRESS);
                showCustomPinDialog();
            } else if(status==DutyStatus.COMPLETED) {
                duty.setDutyEndTime(AppUtility.getCurrentDateTime());
                duty.setDutyStatus(DutyStatus.COMPLETED);
                showAmountDialog();
            }
        };
    };
    private FragmentDriverDutyBinding binding;
    private AlertDialog pinDialog;

    private boolean bound = false;

    private FireStoreDutyBackgroundService.DutyUpdateListener dutyListener = new FireStoreDutyBackgroundService.DutyUpdateListener() {
        @Override
        public void onDutyListUpdate(List<Duty> dutyList) {
            if (!isAdded()) return;
            if(dutyList.isEmpty()){
                driverDutyAdapter.setDutyList(new ArrayList<>());
                recyclerView.setAdapter(driverDutyAdapter);
                binding.noDutyView.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "No Duty Available Now.", Toast.LENGTH_SHORT).show();
            } else {
                //filter duties by status
                dutyList =dutyList.stream()
                        .filter(d -> d.getDutyStatus()!= DutyStatus.UNASSIGNED
                                && d.getDutyStatus()!= DutyStatus.COMPLETED
                                && d.getDutyStatus()!= DutyStatus.REJECTED)
                        .collect(Collectors.toList());
                binding.noDutyView.setVisibility(View.GONE);
                driverDutyAdapter.setDutyList(dutyList);
                recyclerView.setAdapter(driverDutyAdapter);
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(requireContext(), "Error loading duties", Toast.LENGTH_SHORT).show();
        }

    };;


    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            FireStoreDutyBackgroundService.LocalBinder localBinder = (FireStoreDutyBackgroundService.LocalBinder) binder;
            fireStoreBackgroundDutyService = localBinder.getService();
            bound = true;
            fireStoreBackgroundDutyService.addDutyUpdateListener(dutyListener);
            fireStoreBackgroundDutyService.startDutyListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = com.sds.driver.databinding.FragmentDriverDutyBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loader = binding.progressBar;

        //Intialize firestore for duty
        fireStoreDutyService = new FireStoreDutyService(requireContext());

        //Set up RecyclerView
        recyclerView = binding.recyclerDriverDuties;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        driverDutyAdapter=new DriverDutyAdapter(listener);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission from the user before proceeding
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[]{Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC},
                        2001
                );
                return;
            }
        }

        // Start foreground service
        Intent serviceIntent = new Intent(requireContext(), FireStoreDutyBackgroundService.class);

        // Bind to the service
        requireContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
            if (dutyListener != null) {
                fireStoreBackgroundDutyService.removeDutyUpdateListener(dutyListener);
            }
            requireContext().unbindService(connection);
            bound = false;
        }
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, 2);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(requireContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall(String phoneNumber) {
        this.phoneToCall = phoneNumber;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
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
    public void onDestroy() {
        super.onDestroy();
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
            Toast.makeText(requireContext(), "CALL PHONE permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_amount_entry, null);
        builder.setView(view);

        TextView agreedAmountText = view.findViewById(R.id.agreed_amount_text);
        EditText actualAmountInput = view.findViewById(R.id.actual_amount_input);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        ImageButton btnClose = view.findViewById(R.id.btn_close);

        agreedAmountText.setText("Agreed Amount: ₹" + duty.getCharges());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String input = actualAmountInput.getText().toString().trim();

            if (input.isEmpty()) {
                duty.setActualCharges(duty.getCharges());
                showCustomPinDialog();  // Amount not entered → assume agreed
                dialog.dismiss();
                return;
            }

            int received = Integer.parseInt(input);
            duty.setActualCharges(received);
            duty.setCommission(received*ApplicationContext.getCommissionPercentage()/100);
            dialog.dismiss();
            showCustomPinDialog();
        });
    }


    private void showCustomPinDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_pin_entry, null);
        builder.setView(view);
        pinDialog = builder.create();

        EditText pin1 = view.findViewById(R.id.pin1);
        EditText pin2 = view.findViewById(R.id.pin2);
        EditText pin3 = view.findViewById(R.id.pin3);
        EditText pin4 = view.findViewById(R.id.pin4);
        ImageView close = view.findViewById(R.id.btn_close);

        close.setOnClickListener(v -> pinDialog.dismiss());

        // Move focus and handle auto-submit
        setupPinEditText(pin1, pin2, null);
        setupPinEditText(pin2, pin3, pin1);
        setupPinEditText(pin3, pin4, pin2);
        setupPinEditText(pin4, null, pin3);
        pinDialog.show();
    }

    private void setupPinEditText(EditText current, EditText next, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.setText("");
                    previous.requestFocus();
                    return true;
                }
            }
            return false;
        });

        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                } else if (s.length() == 1 && next == null) {
                    // fetch last 4 digit of cutomer phone
                    verifyPin();
                }
            }
        });
    }

    private void verifyPin() {
        String pin = ((EditText) pinDialog.findViewById(R.id.pin1)).getText().toString()
                + ((EditText) pinDialog.findViewById(R.id.pin2)).getText().toString()
                + ((EditText) pinDialog.findViewById(R.id.pin3)).getText().toString()
                + ((EditText) pinDialog.findViewById(R.id.pin4)).getText().toString();

        String phone = duty.getCustomerPhone();
        String defaultPin = phone.substring(phone.length()-4,phone.length());

        if (pin.equals(defaultPin)) {
            Toast.makeText(requireContext(), "PIN Verified!", Toast.LENGTH_SHORT).show();
            pinDialog.dismiss();
            updateDutyDetails();

        } else {
            Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDutyDetails(){
        try{
            loader.setVisibility(View.VISIBLE);
            fireStoreDutyService.saveDuty(duty, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Duty Updated Successfully", Toast.LENGTH_SHORT).show();

                    if(nextStatus==DutyStatus.ACCEPTED){
                        String msg="Customer Name : "+duty.getCustomerName()+"\n"+
                                "Address : "+duty.getCustomerAddress()+"\n"+
                                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
                        sendSms(ApplicationContext.getAdminPhone(), "Driver :"+duty.getDriverName()+" has ACCEPTED the below duty \n "+msg);
                    } else if(nextStatus==DutyStatus.REJECTED){
                        String msg="Customer Name : "+duty.getCustomerName()+"\n"+
                                "Address : "+duty.getCustomerAddress()+"\n"+
                                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
                        sendSms(ApplicationContext.getAdminPhone(), "Driver :"+duty.getDriverName()+" has REJECTED the below duty \n "+msg);
                    } else if(nextStatus==DutyStatus.INPROGRESS){
                        String msg="Customer Name : "+duty.getCustomerName()+"\n"+
                                "Address : "+duty.getCustomerAddress()+"\n"+
                                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
                        sendSms(ApplicationContext.getAdminPhone(), "Driver :"+duty.getDriverName()+" has STARTED the below duty \n "+msg);
                    } else if(nextStatus==DutyStatus.COMPLETED){
                        String msg="Customer Name : "+duty.getCustomerName()+"\n"+
                                "Address : "+duty.getCustomerAddress()+"\n"+
                                "Duty Time : "+AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime());
                        sendSms(ApplicationContext.getAdminPhone(), "Driver :"+duty.getDriverName()+" has COMPLETED the below duty \n "+msg);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    duty.setDutyStatus(previousStatus);
                    driverDutyAdapter.notifyItemChanged(position);
                }
            });
        } catch (Exception e){
            loader.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error occured while Update Duty"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }





}