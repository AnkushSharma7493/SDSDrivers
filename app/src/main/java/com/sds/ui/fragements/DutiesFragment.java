package com.sds.ui.fragements;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sds.R;
import com.sds.databinding.FragmentDutiesBinding;
import com.sds.ui.activities.OnPhoneClickListener;
import com.sds.ui.adaptors.DutyAdapter;
import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import com.sds.ui.services.DutyService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DutiesFragment extends Fragment {
    private String phoneToCall;
    private Date selectedDate = null;
    private String selectedStatus = null;
    private DutyAdapter dutyAdapter;
    private FragmentDutiesBinding binding;

    private int selectedDutyIndex;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDutiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the phone click listener
        OnPhoneClickListener listener = new OnPhoneClickListener() {
            @Override
            public void onCustomerPhoneClick(String phoneNumber) {
                makePhoneCall(phoneNumber);
            }

            @Override
            public void onDriverPhoneClick(String phoneNumber) {
                makePhoneCall(phoneNumber);
            }

            @Override
            public void onCustomerSmsClick(String phoneNumber) {
                sendSms(phoneNumber, "Your driver is on the way.");
            }

            @Override
            public void onDriverSmsClick(String phoneNumber) {
                sendSms(phoneNumber, "You have been assigned a duty.");
            }
        };

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_duties);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dutyAdapter=new DutyAdapter(listener);
        recyclerView.setAdapter(dutyAdapter);

        // Date Picker for Select Date
        binding.btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (viewDatePicker, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        // Format and show date on button
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(selectedDate.getTime());
                        binding.btnSelectDate.setText(formattedDate);

                        // Filter list based on date
                        DutyService.getInstance().filterDuties(selectedDate.getTime(), selectedStatus);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        String[] statuses =DutyStatus.getStringValues();

        binding.btnStatus.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Duty Status");

            builder.setItems(statuses, (dialog, which) -> {
                selectedStatus = statuses[which];
                binding.btnStatus.setText(selectedStatus);

                // Filter based on selected status
                DutyService.getInstance().filterDuties(selectedDate, selectedStatus);
            });

            builder.show();
        });

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
