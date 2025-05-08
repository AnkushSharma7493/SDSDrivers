package com.sds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.sds.databinding.ActivityMainBinding;
import com.sds.ui.activities.LoginActivity;
import com.sds.ui.services.ApplicationContext;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String APP_NAME="Sharma Driver Service";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = this.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Setting Toolbar
        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        // Optional: Remove default title
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View customView = LayoutInflater.from(this).inflate(R.layout.custom_toolbar_layout, toolbar, false);
        toolbar.addView(customView);

        // Hook listeners
        ImageView profileIcon = customView.findViewById(R.id.profile_icon);
        ImageView notificationIcon = customView.findViewById(R.id.notification_icon);
        TextView title = toolbar.findViewById(R.id.toolbar_title);

        title.setText(APP_NAME); // Set dynamically if needed

        profileIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("View Profile");
            popup.getMenu().add("Sign Out");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "View Profile":
                        // handle view profile
                        return true;
                    case "Sign Out":
                        prefs.edit().remove(ApplicationContext.DRIVER_PHONE_CACHE).apply();
                        prefs.edit().remove(ApplicationContext.DRIVER_PIN_CACHE).apply();
                        prefs.edit().remove(ApplicationContext.DRIVER_ROLE_CACHE).apply();

                        startActivity(new Intent(this, LoginActivity.class));
                        return true;
                }
                return false;
            });

            popup.show();
        });

        notificationIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Non Assigned Driver");
            popup.getMenu().add("Assigned Driver");
            popup.getMenu().add("Pending Duties");
            popup.getMenu().add("Waiting Driver Confirmation");

            popup.setOnMenuItemClickListener(item -> {
                // Handle each filter
                Toast.makeText(this, "Filter: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });

            popup.show();
        });

        // Set User Phone's Status bar to same color as toolbar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_200));
        }


    }
}