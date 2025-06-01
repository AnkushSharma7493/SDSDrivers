package com.sds.driver.ui.fragements.driver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sds.driver.databinding.FragmentCommissionPaymentBinding;
import com.sds.driver.ui.adaptors.DriverDutyListAdapter;
import com.sds.driver.ui.callback.FireStoreQueryDutyCallback;
import com.sds.driver.ui.enums.CommissionStatus;
import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreDutyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommissionPaymentFragment extends Fragment {

    private FragmentCommissionPaymentBinding binding;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private SharedPreferences prefs;
    private FireStoreDutyService fireStoreDutyService;
    private int payableCommission = 0;

    private List<Duty> searchDuties=new ArrayList<>();

    private DriverDutyListAdapter driverDutyListAdapter;


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

    }



    @Override
    public void onResume() {
        super.onResume();
        String driverPhone = prefs.getString(ApplicationContext.DRIVER_PHONE_CACHE,"");
        loadDutyByDriverPhone(driverPhone);
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



}