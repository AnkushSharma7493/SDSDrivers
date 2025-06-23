package com.sds.driver.ui.fragements.driver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.sds.driver.databinding.FragmentDriverProfileBinding;
import com.sds.driver.ui.callback.FireStoreQueryDriverCallback;
import com.sds.driver.ui.models.Driver;
import com.sds.driver.ui.services.AppUtility;
import com.sds.driver.ui.services.ApplicationContext;
import com.sds.driver.ui.services.FireStoreDriverService;
import java.util.List;

public class DriverProfileFragment extends Fragment {

    private FragmentDriverProfileBinding binding;
    private FireStoreDriverService fireStoreDriverService;
    private ProgressBar loader;

    private SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = com.sds.driver.databinding.FragmentDriverProfileBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loader = binding.progressBar;
        fireStoreDriverService = new FireStoreDriverService(requireContext());
        prefs = requireContext().getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        String driverPhone = prefs.getString(ApplicationContext.DRIVER_PHONE_CACHE,"");
        if(ApplicationContext.getProfile()!=null){
            populateFields(ApplicationContext.getProfile());
        } else {
            loadDriver(driverPhone);
        }
    }


    private void loadDriver(String phone){
        try{
            loader.setVisibility(View.VISIBLE);
            fireStoreDriverService.getDriverByPhone(phone, new FireStoreQueryDriverCallback() {
                @Override
                public void onSuccess(List<Driver> drivers) {
                    loader.setVisibility(View.GONE);
                    if(drivers.isEmpty()){
                        Toast.makeText(requireContext(), "No Driver Available for Phone "+phone, Toast.LENGTH_SHORT).show();
                    } else {
                            Driver driver = drivers.get(0);
                            ApplicationContext.setProfile(driver);
                            populateFields(driver);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e){
            Toast.makeText(requireContext(), "Error while fetching Driver Details", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
        }
    }

    private void populateFields(Driver driver){
        // populate Driver profile fields
        try {
            binding.editDriverName.setText(driver.getName());
            binding.editDriverPhone.setText(driver.getPhone());
            binding.editDriverEmergencyPhone.setText(driver.getEmergencyPhone());
            binding.editAadhaarNo.setText(driver.getAadhaarNo());
            binding.editBirthDate.setText(AppUtility.formatDate(driver.getDOB()));
            binding.editCurrentAddress.setText(driver.getCurrentAddress());
            binding.editCurrentNativeAddress.setText(driver.getNativeAddress());
            binding.editLicenceNo.setText(driver.getLicenceNo());
            binding.editJoiningDate.setText(AppUtility.formatDate(driver.getJoiningDate()));
            binding.editStatus.setText(driver.isStatus() ? "Active" : "InActive");
            binding.editNote.setText(driver.getNote());
            binding.editDriverExperience.setText(driver.getDriverExperience().toString());
        } catch (Exception e){

        }
    }
}