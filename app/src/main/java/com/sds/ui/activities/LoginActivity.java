package com.sds.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.sds.MainActivity;
import com.sds.databinding.ActivityLoginBinding;
import com.sds.ui.activities.driver.DriverDutyActivity;
import com.sds.ui.models.Driver;
import com.sds.ui.services.ApplicationContext;
import com.sds.ui.services.FireStoreDriverService;
import com.sds.ui.callback.FireStoreQueryDriverCallback;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FireStoreDriverService fireStoreDriverService;
    private ProgressBar loader;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.textViewRegister.setOnClickListener(v -> openRegisterScreen());

        //initialize firestore
        fireStoreDriverService = new FireStoreDriverService(this);

        loader = binding.progressBar;
    }

    private void attemptLogin() {
        String phone = binding.editTextPhoneOrEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length()>10) {
            binding.editTextPhoneOrEmail.setError("Enter valid phone number");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Enter password");
            return;
        }

        searchDriverByPhone(phone,password);
    }

    private void openRegisterScreen() {
        // TODO: Open your registration activity here
        Toast.makeText(this, "Open Registration Screen", Toast.LENGTH_SHORT).show();
    }


    private void searchDriverByPhone(String phone,String password){
        try{
            loader.setVisibility(View.VISIBLE);
            fireStoreDriverService.getDriverByPhone(phone, new FireStoreQueryDriverCallback() {
                @Override
                public void onSuccess(List<Driver> drivers) {
                    loader.setVisibility(View.GONE);
                    if(drivers.isEmpty()){
                        Toast.makeText(LoginActivity.this, "No Driver Available for Phone "+phone, Toast.LENGTH_SHORT).show();
                    } else {
                        String phoneDB=drivers.get(0).getPhone();
                        String pinDB=drivers.get(0).getPin();
                        String role=drivers.get(0).getRole();

                        if (phone.equals(phoneDB) && password.equals(pinDB)) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            prefs.edit().putString(ApplicationContext.DRIVER_PHONE_CACHE, phoneDB).apply();
                            prefs.edit().putString(ApplicationContext.DRIVER_PIN_CACHE, pinDB).apply();
                            prefs.edit().putString(ApplicationContext.DRIVER_ROLE_CACHE,role).apply();

                            //Set login user phone
                            ApplicationContext.LOGIN_USER_PHONE=phoneDB;

                            if(ApplicationContext.DRIVER_ROLE.equals(role)){
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                startActivity(new Intent(LoginActivity.this, DriverDutyActivity.class));
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }

                    }
                }

                @Override
                public void onFailure(Exception e) {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e){
            Toast.makeText(this, "Error while login", Toast.LENGTH_SHORT).show();
            loader.setVisibility(View.GONE);
        }
    }
}