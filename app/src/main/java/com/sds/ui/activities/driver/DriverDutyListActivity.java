package com.sds.ui.activities.driver;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.R;
import com.sds.ui.adaptors.DutyAdapter;
import com.sds.ui.listener.DutyListeners;
import com.sds.ui.models.Duty;
import com.sds.ui.services.AppUtility;

import java.util.ArrayList;
import java.util.List;

public class DriverDutyListActivity extends AppCompatActivity {

    private DutyListeners listener=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_duty_list);

        List<Duty> list = new ArrayList<>();
        Duty duty = new Duty();
        duty.setCustomerName("Driver Name");
        duty.setCustomerPhone("9999999999");
        duty.setDutyReportingDate(AppUtility.getCurrentDateTime());

        RecyclerView recyclerView = findViewById(R.id.recyclerListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DutyAdapter adapter = new DutyAdapter(listener);
        recyclerView.setAdapter(adapter);
        adapter.setDuties(list);

    }
}