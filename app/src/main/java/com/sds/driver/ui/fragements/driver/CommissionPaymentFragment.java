package com.sds.driver.ui.fragements.driver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sds.driver.databinding.FragmentCommissionPaymentBinding;
import com.sds.driver.ui.adaptors.DriverDutyListAdapter;
import com.sds.driver.ui.callback.FireStoreQueryDutyCallback;
import com.sds.driver.ui.callback.FirestoreCallback;
import com.sds.driver.ui.enums.CommissionStatus;
import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.AppUtility;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreDutyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommissionPaymentFragment extends Fragment {

    private FragmentCommissionPaymentBinding binding;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private SharedPreferences prefs;
    private FireStoreDutyService fireStoreDutyService;
    private int payableCommission = 0;
    private int temp_amountPaid = 0;

    private List<Duty> searchDuties=new ArrayList<>();

    private DriverDutyListAdapter driverDutyListAdapter;

    private ActivityResultLauncher<Intent> upiPaymentLauncher;
    private List<Duty> failedStatusUpdateDuties = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = com.sds.driver.databinding.FragmentCommissionPaymentBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loader = binding.progressBar;

        fireStoreDutyService = new FireStoreDutyService(requireContext());
        prefs = requireContext().getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        recyclerView = binding.recyclerListView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        driverDutyListAdapter = new DriverDutyListAdapter();
        recyclerView.setAdapter(driverDutyListAdapter);

        binding.iconFilterDuty.setOnClickListener(v -> showFilterPopup());


        //Trigger UPI Payment
        binding.btnPay.setOnClickListener(v->{
            String upiId = ApplicationContext.getUpi_id();
            String name = ApplicationContext.getUpi_name();
            String amount =String.valueOf(payableCommission);
            String note = "SDS Driver Commission";

            //payToAdmin(upiId, name, amount, note);
            bhimPay(upiId, name, amount, note);
        });

    }



    @Override
    public void onResume() {
        super.onResume();
        String driverPhone = prefs.getString(ApplicationContext.DRIVER_PHONE_CACHE,"");
        loadDutyByDriverPhone(driverPhone);
    }

    private static final int UPI_PAYMENT_REQUEST_CODE = 1;
    private static final String BHIM_PACKAGE_NAME = "in.org.npci.upiapp";

    private void bhimPay(String adminUpiId, String payeeName, String amount, String transactionNote) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", adminUpiId)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("mc", "")
                .appendQueryParameter("tid", UUID.randomUUID().toString())
                .appendQueryParameter("tr", UUID.randomUUID().toString())
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", "1")
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setPackage(BHIM_PACKAGE_NAME);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            upiPaymentLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "BHIM app is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            if (data != null) {
                String response = data.getStringExtra("response");
                Log.d("UPI_RESPONSE", "Response: " + response);
                Toast.makeText(getContext(), "Transaction Response:\n" + response, Toast.LENGTH_LONG).show();
            } else {
                Log.d("UPI_RESPONSE", "No response received");
                Toast.makeText(getContext(), "No response or cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void PaytmUPI(){
        String upiId = "admin@upi";
        String name = "AdminName";
        String note = "CommissionPayment";
        String amount = "100.00";

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", ApplicationContext.getUpi_id())          // Payee VPA (required)
                .appendQueryParameter("pn", name)           // Payee name (required)
                .appendQueryParameter("mc", "")             // Merchant code (optional)
                .appendQueryParameter("tid", "TXN123456")   // Transaction ID (recommended)
                .appendQueryParameter("tr", "REF123456")    // Transaction reference ID (recommended)
                .appendQueryParameter("tn", note)           // Transaction note
                .appendQueryParameter("am", amount)         // Amount (must be decimal format)
                .appendQueryParameter("cu", "INR")          // Currency
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        upiPayIntent.setPackage("net.one97.paytm");

        if (upiPayIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            upiPaymentLauncher.launch(upiPayIntent); // launch via ActivityResultLauncher
        } else {
            Toast.makeText(getContext(), "Paytm app is not installed", Toast.LENGTH_SHORT).show();
        }

//        // 🔍 Use packageManager to list valid UPI apps
//        PackageManager packageManager = requireActivity().getPackageManager();
//        List<ResolveInfo> apps = packageManager.queryIntentActivities(upiPayIntent, 0);
//
//        if (apps != null && !apps.isEmpty()) {
//            // Found UPI apps
//            Intent chooser = Intent.createChooser(upiPayIntent, "Pay with UPI");
//            upiPaymentLauncher.launch(chooser);
//        } else {
//            Toast.makeText(getContext(), "No UPI app found. Please install one.", Toast.LENGTH_SHORT).show();
//        }

    }

    private void payToAdmin(String adminUpiId, String payeeName, String amount, String transactionNote){
        PaytmUPI();
//        Uri uri = Uri.parse("upi://pay").buildUpon()
//                .appendQueryParameter("pa", adminUpiId)      // Payee UPI ID
//                .appendQueryParameter("pn", payeeName)       // Payee Name
//                .appendQueryParameter("tn", transactionNote) // Transaction note
//                .appendQueryParameter("am", amount)          // Amount
//                .appendQueryParameter("cu", "INR")           // Currency
//                .build();
//
//        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
//        upiPayIntent.setData(uri);
//
//        // Check for available UPI apps
//        PackageManager packageManager = requireActivity().getPackageManager();
//        List<ResolveInfo> apps = packageManager.queryIntentActivities(upiPayIntent, 0);
//
//        if (apps != null && !apps.isEmpty()) {
//            // Launch with chooser
//            Intent chooser = Intent.createChooser(upiPayIntent, "Pay with UPI");
//            upiPaymentLauncher.launch(chooser);
//        } else {
//            Toast.makeText(getContext(), "No UPI app found. Please install one.", Toast.LENGTH_SHORT).show();
//        }
    }

    private void payToAdmin_v1(String adminUpiId, String payeeName, String amount, String transactionNote) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", adminUpiId)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with UPI");

        if (upiPayIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            upiPaymentLauncher.launch(chooser);
        } else {
            Toast.makeText(getContext(), "No UPI app found. Please install one.", Toast.LENGTH_SHORT).show();
        }
    }

    //UPI RESPONSE HANDLING
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register launcher for UPI payment result
        upiPaymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loader.setVisibility(View.VISIBLE);
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String response = result.getData().getStringExtra("response");
                        if (response != null) {
                            Map<String, String> responseMap = AppUtility.parseUpiResponse(response.toLowerCase());

                            String status = responseMap.get("status");
                            String txnRef = responseMap.get("txnRef");
                            int amountPaid = Integer.parseInt(responseMap.get("amount"));

                            if ("success".equalsIgnoreCase(status)) {
                                if (payableCommission==amountPaid) {
                                    Toast.makeText(getContext(), "₹"+payableCommission+" Payment Successful", Toast.LENGTH_SHORT).show();

                                    // Send SMS to Admin for success payment
                                    String msg="SDS FULL COMMISSION PAYMENT MADE"+"\n"+
                                            "Driver : "+searchDuties.get(0).getDriverName()+"\n"+
                                            "Phone : "+searchDuties.get(0).getDriverPhone()+"\n"+
                                            "Amount : "+amountPaid+"\n"+
                                            "Payment ID : "+txnRef;

                                    sendSms(ApplicationContext.getAdminPhone(),msg);

                                    // Update all Duties Commission payment status to RECEIVED and commission amount to 0.
                                    searchDuties.forEach(duty -> {
                                        int dutyCommission = duty.getCommission();
                                        payableCommission-=dutyCommission;
                                        duty.setCommission(0);
                                        duty.setCommissionStatus(CommissionStatus.RECEIVED);
                                        updateDuty(duty);
                                    });

                                    // Show alert for failed duty Updates.
                                    if(failedStatusUpdateDuties.size()>0) {
                                        String failedSMS = "SDS COMMISSION PAYMENT SUCCESS, BUT DUTY STATUS FAILED" + "\n" +
                                                "Driver : " + searchDuties.get(0).getDriverName() + "\n" +
                                                "Phone : " + searchDuties.get(0).getDriverPhone() + "\n";
                                        for (Duty fDuty : failedStatusUpdateDuties) {
                                            failedSMS += "DutyId : " + fDuty.getDutyId() + "\n";
                                        }

                                        sendSms(ApplicationContext.getAdminPhone(), failedSMS);
                                        Toast.makeText(getContext(), failedSMS, Toast.LENGTH_SHORT).show();
                                    }

                                    // Show Success Alert
                                    showSuccessAlert(String.valueOf(amountPaid),txnRef);

                                } else if (payableCommission>amountPaid){
                                    Toast.makeText(getContext(), "Partial Commission amount paid: ₹" + amountPaid, Toast.LENGTH_LONG).show();
                                    temp_amountPaid=amountPaid;

                                    // Partial Amount sms To Admin
                                    String msg="SDS PARTIAL COMMISSION PAYMENT MADE"+"\n"+
                                            "Driver : "+searchDuties.get(0).getDriverName()+"\n"+
                                            "Phone : "+searchDuties.get(0).getDriverPhone()+"\n"+
                                            "COMMISSION : "+payableCommission+"\n"+
                                            "Amount PAID : "+amountPaid+"\n"+
                                            "Remaining Amount : "+(payableCommission-amountPaid)+"\n"+
                                            "Payment ID : "+txnRef;

                                    sendSms(ApplicationContext.getAdminPhone(),msg);

                                    // Update all Duties Commission payment status to RECEIVED and commission amount to 0.
                                    for(Duty duty : searchDuties) {
                                        int dutyCommission = duty.getCommission();
                                        if(temp_amountPaid==0){
                                            break; // Come out of loop
                                        } else if(temp_amountPaid>dutyCommission) {
                                            payableCommission -= dutyCommission;
                                            temp_amountPaid -= dutyCommission;
                                            duty.setCommission(0);
                                            duty.setCommissionStatus(CommissionStatus.RECEIVED);
                                            updateDuty(duty);
                                        } else if(temp_amountPaid<dutyCommission) {
                                            dutyCommission-=temp_amountPaid;
                                            payableCommission -= temp_amountPaid;
                                            temp_amountPaid = 0;

                                            duty.setCommission(dutyCommission);
                                            duty.setCommissionStatus(CommissionStatus.PENDING);
                                            updateDuty(duty);
                                        }
                                    }

                                    // Show alert for failed duty Updates.
                                    if(failedStatusUpdateDuties.size()>0) {
                                        String failedSMS = "SDS PARTIAL COMMISSION PAYMENT SUCCESS, BUT DUTY STATUS FAILED" + "\n" +
                                                "Driver : " + searchDuties.get(0).getDriverName() + "\n" +
                                                "Phone : " + searchDuties.get(0).getDriverPhone() + "\n";
                                        for (Duty fDuty : failedStatusUpdateDuties) {
                                            failedSMS += "DutyId : " + fDuty.getDutyId() + "\n";
                                        }

                                        sendSms(ApplicationContext.getAdminPhone(), failedSMS);
                                    }

                                    // Show Alert for remaining amount.
                                    showAmountMismatchAlert(String.valueOf(amountPaid),txnRef);
                                } else {
                                    // EXTRA AMOUNT PAID
                                    Toast.makeText(getContext(), "Extra Commission Paid: ₹" + amountPaid, Toast.LENGTH_LONG).show();

                                    // Send SMS to Admin
                                    String msg="SDS EXTRA COMMISSION PAYMENT MADE"+"\n"+
                                            "Driver : "+searchDuties.get(0).getDriverName()+"\n"+
                                            "Phone : "+searchDuties.get(0).getDriverPhone()+"\n"+
                                            "COMMISSION : "+payableCommission+"\n"+
                                            "Amount PAID : "+amountPaid+"\n"+
                                            "EXTRA Amount : "+(amountPaid-payableCommission)+"\n"+
                                            "Payment ID : "+txnRef;

                                    sendSms(ApplicationContext.getAdminPhone(),msg);

                                    // Alert
                                    showExtraPaidAlert(String.valueOf(amountPaid),txnRef);
                                }
                            } else {
                                showCancelAlert();
                                Toast.makeText(getContext(), "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showCancelAlert();
                            Toast.makeText(getContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();

                        }
                    }
                    loader.setVisibility(View.GONE);
                }
        );

    }

    private void showCancelAlert() {
        loader.setVisibility(View.GONE);
        new AlertDialog.Builder(requireContext())
                .setTitle("TRANSACTION CANCELLED")
                .setMessage("Transaction Cancelled. \n\nPlease try again.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Try Again", (dialog, which) -> {
                    String upiId = ApplicationContext.getUpi_id();
                    String name = ApplicationContext.getUpi_name();
                    String amount =String.valueOf(payableCommission);
                    String note = "SDS Driver Commission";

                    payToAdmin(upiId, name, amount, note);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showAmountMismatchAlert(String amountPaid,String refId) {
        loader.setVisibility(View.GONE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Partial Commission Payment")
                .setMessage("Payment : ₹" + amountPaid +
                                "\n Total Commission : ₹"+payableCommission+
                                "\n Remaining Commission : ₹"+(payableCommission-Integer.parseInt(amountPaid))+
                                "\n Transaction ID : ₹"+refId+
                                "\n\nPlease pay the remaining amount again.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Pay Again", (dialog, which) -> {
                    String upiId = ApplicationContext.getUpi_id();
                    String name = ApplicationContext.getUpi_name();
                    String amount =String.valueOf(payableCommission-Integer.parseInt(amountPaid));
                    String note = "REPAYMENT PARTIAL COMMISSION : SDS Driver Commission";

                    payToAdmin(upiId, name, amount, note);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSuccessAlert(String amountPaid,String refId) {
        loader.setVisibility(View.GONE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Commission Payment Successful")
                .setMessage("Payment : ₹" + amountPaid +
                        "\n Total Commission : ₹"+payableCommission+
                        "\n Transaction ID : ₹"+refId+
                        "\n\nPayment successful")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Ok",null)
                .show();
    }

    private void showExtraPaidAlert(String amountPaid,String refId) {
        loader.setVisibility(View.GONE);
        new AlertDialog.Builder(requireContext())
                .setTitle("Extra Commission Payment Successful")
                .setMessage("Payment : ₹" + amountPaid +
                        "\n Total Commission : ₹"+payableCommission+
                        "\n Extra Payment : ₹"+(Integer.parseInt(amountPaid)-payableCommission)+
                        "\n Transaction ID : ₹"+refId+
                        "\n\nPlease Contact to Owner for payment resolution.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Ok",null)
                .show();
    }

    private void updateDuty(Duty duty){
        // Update In DB
        fireStoreDutyService.saveDuty(duty,new FirestoreCallback() {
            @Override
            public void onSuccess() {
                //Toast.makeText(requireContext(), "Duty Created Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                failedStatusUpdateDuties.add(duty);
            }
        });
    }

    private void loadDutyByDriverPhone(String phone){
        loader.setVisibility(View.VISIBLE);
        fireStoreDutyService.getDutiesByDriverPhone(phone, new FireStoreQueryDutyCallback() {
            @Override
            public void onSuccess(List<Duty> duties) {
                loader.setVisibility(View.GONE);
                if(duties.isEmpty()) {
                    driverDutyListAdapter.setDutyList(new ArrayList<>());
                    Toast.makeText(requireContext(), "No Duty Available Now.", Toast.LENGTH_SHORT).show();
                } else {
                    // assigned for internal filter.
                    searchDuties=duties;
                    payableCommission = duties.stream()
                            .filter(d->d.getDutyStatus()==DutyStatus.COMPLETED &&
                                            d.getCommissionStatus()==CommissionStatus.PENDING)
                            .mapToInt(Duty::getCommission)
                            .sum();
                    binding.btnPay.setText("Pay  "+"\u20B9" + payableCommission);
                    computeCommission(duties);
                    duties.sort((d1, d2) -> {
                        boolean c1Pending = "PENDING".equalsIgnoreCase(d1.getCommissionStatus().getStatus());
                        boolean c2Pending = "PENDING".equalsIgnoreCase(d2.getCommissionStatus().getStatus());

                        if (c1Pending && !c2Pending) return -1;
                        if (!c1Pending && c2Pending) return 1;

                        // Secondary sort, e.g., by createdDate
                        return d2.getDutyReportingDate().compareTo(d1.getDutyReportingDate());
                    });

                    driverDutyListAdapter.setDutyList(duties);
                }
                recyclerView.setAdapter(driverDutyListAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                loader.setVisibility(View.GONE);
                driverDutyListAdapter.setDutyList(new ArrayList<>());
                recyclerView.setAdapter(driverDutyListAdapter);
                Toast.makeText(requireContext(), "Error loading duties", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void computeCommission(List<Duty> dutiesList){

        int totalCommission = dutiesList.stream().mapToInt(Duty::getCommission).sum();
        int totalCharges = dutiesList.stream().mapToInt(Duty::getCharges).sum();

        binding.tvTotalCommission.setText("\u20B9" + totalCommission);
        binding.tvTotalCharges.setText("\u20B9" + totalCharges);
    }

    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter Duties by Status");

        String[] filters = {"Assigned Duties","Accepted Duties","InProgress Duties","Completed Duties","Cancelled","All Duties"};

        builder.setItems(filters, (dialog, which) -> {
            String selected = filters[which];
            Toast.makeText(requireContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();
            // Apply filter logic here
            if(searchDuties.size()>0){
                if(selected.equals("All Duties")){
                    computeCommission(searchDuties);
                    driverDutyListAdapter.setDutyList(searchDuties);
                } else {
                    List<Duty> filteredList = searchDuties.stream()
                            .filter(d -> d.getDutyStatus() == DutyStatus.getByStatus(selected.replace(" Duties", "")))
                            .collect(Collectors.toList());
                    computeCommission(filteredList);
                    driverDutyListAdapter.setDutyList(filteredList);
                }
                recyclerView.setAdapter(driverDutyListAdapter);
            }
        });

        builder.show();
    }

    private void sendSms(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, 2);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, AppUtility.removeEmojis(message), null, null);
            //Toast.makeText(requireContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        }
    }


}