package com.sds.ui.fragements;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.sds.R;
import com.sds.databinding.FragmentContactsBinding;
import com.sds.ui.activities.CreateDriverActivity;
import com.sds.ui.listener.DriverClickListener;
import com.sds.ui.adaptors.DriverAdapter;
import com.sds.ui.models.Driver;
import com.sds.ui.services.FireStoreDriverService;
import com.sds.ui.callback.FireStoreQueryDriverCallback;

import java.util.ArrayList;
import java.util.List;


public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;
    private String phoneToCall;
    private DriverAdapter driverAdapter;
    private FireStoreDriverService fireStoreDriverService;
    private ProgressBar loader;
    private RecyclerView recyclerView;
    private DriverClickListener listener = new DriverClickListener() {

        @Override
        public void onPhoneClick(String phoneNumber) {
            makePhoneCall(phoneNumber);
        }

        @Override
        public void onEmergencyPhoneClick(String phoneNumber) {
            makePhoneCall(phoneNumber);
        }
    };
    private SearchView searchView;
    private EditText searchEditText;
    private ImageView contactPickerIcon;
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private boolean contactSearch=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loader = binding.driverprogressBar;

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_drivers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        driverAdapter = new DriverAdapter(listener);

        fireStoreDriverService = new FireStoreDriverService(requireContext());

        searchView = binding.searchView;
        contactPickerIcon=binding.contactPickerIcon;
        searchEditText = binding.searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        setupSearchView();
        setupContactPicker();

        // FAB Button
        FloatingActionButton fab = view.findViewById(R.id.fab_add_driver);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateDriverActivity.class);
            startActivity(intent);
        });

    }

    private void setupSearchView() {
        searchEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        searchEditText.setHint("Driver Phone Number");

        // Listen for search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String phone) {
                // verification of input phone number.
                if (phone != null && phone.length() == 10) {
                    searchDriverByPhone(phone);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>10){
                    Toast.makeText(requireContext(), "10 digits phone number allowed.", Toast.LENGTH_SHORT).show();
                    searchEditText.setText(newText.substring(0, 10));
                    searchEditText.setSelection(10); // Move cursor to end
                }
                return false;
            }
        });

        contactPickerIcon.setOnClickListener(v -> openContactPicker());

        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
    }

    private void setupContactPicker() {
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    contactSearch=true;
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                        Cursor cursor = requireActivity().getContentResolver().query(contactUri, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String number = cursor.getString(numberIndex);

                            // Clean the number
                            number = number.replaceAll("[^\\d]", "");
                            if (number.length() > 10) {
                                number = number.substring(number.length() - 10); // take last 10 digits
                            }

                            searchEditText.setText(number);
                            searchDriverByPhone(number);
                            cursor.close();
                        }
                    }
                }
        );
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }


    private void searchDriverByPhone(String phone){
        try{
        loader.setVisibility(View.VISIBLE);
        fireStoreDriverService.getDriverByPhone(phone, new FireStoreQueryDriverCallback() {

            @Override
            public void onSuccess(List<Driver> drivers) {
                loader.setVisibility(View.GONE);
                if(drivers.isEmpty()){
                    driverAdapter.setDuties(new ArrayList<>());
                    recyclerView.setAdapter(driverAdapter);
                    Toast.makeText(requireContext(), "No Driver Available for Phone "+phone, Toast.LENGTH_SHORT).show();
                } else {
                    driverAdapter.setDuties(drivers);
                    recyclerView.setAdapter(driverAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                loader.setVisibility(View.GONE);
                driverAdapter.setDuties(new ArrayList<>());
                recyclerView.setAdapter(driverAdapter);
                Toast.makeText(requireContext(), "Error loading Driver", Toast.LENGTH_SHORT).show();
            }
        });
        } catch(Exception e){
            Toast.makeText(requireContext(), "Error while loading Driver by phone", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
        }
    }

    private void loadAllDrivers(){
        try {
            loader.setVisibility(View.VISIBLE);

            fireStoreDriverService.getAllDrivers(new FireStoreQueryDriverCallback() {
                @Override
                public void onSuccess(List<Driver> drivers) {
                    loader.setVisibility(View.GONE);
                    if (drivers.isEmpty()) {
                        driverAdapter.setDuties(new ArrayList<>());
                        recyclerView.setAdapter(driverAdapter);
                        Toast.makeText(requireContext(), "No Driver Available", Toast.LENGTH_SHORT).show();
                    } else {
                        driverAdapter.setDuties(drivers);
                        recyclerView.setAdapter(driverAdapter);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    driverAdapter.setDuties(new ArrayList<>());
                    recyclerView.setAdapter(driverAdapter);
                    Toast.makeText(requireContext(), "Error loading duties", Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e){
            Toast.makeText(requireContext(), "Error while loading Drivers", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if(contactSearch){
                contactSearch=false;
            } else {
                //Load All Drivers
                loadAllDrivers();
            }
        } catch (Exception e) {

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