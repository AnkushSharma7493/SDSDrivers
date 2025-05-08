package com.sds.ui.adaptors;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.R;
import com.sds.ui.activities.CreateDriverActivity;
import com.sds.ui.listener.DriverClickListener;
import com.sds.ui.models.Driver;
import com.sds.ui.services.AppUtility;

import java.util.ArrayList;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private List<Driver> driverList =new ArrayList<>();
    private DriverClickListener driverClickListener;

    public DriverAdapter(DriverClickListener listener) {
        this.driverClickListener = listener;
    }

    public void setDuties(List<Driver> driverList){
        this.driverList =driverList;
    }

    @NonNull
    @Override
    public DriverAdapter.DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        holder.tvName.setText("Name : " + driver.getName());
        holder.tvPhone.setText("Phone : " + driver.getPhone());
        holder.tvDOB.setText("DOB : " + AppUtility.formatDate(driver.getDOB()));
        holder.tvEmergencyPhone.setText("Emergency Phone : " + driver.getEmergencyPhone());
        holder.tvjoiningDate.setText("Joining Date : " + AppUtility.formatDate(driver.getJoiningDate()));
        holder.tvStatus.setText("Status: " + (driver.isStatus()?"ACTIVE":"INACTIVE"));
        holder.tvPin.setText("PIN : " + driver.getPin());


        // Edit Driver
        holder.btnEditDriver.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CreateDriverActivity.class);
            intent.putExtra("driver",driver);
            intent.putExtra("edit_mode", true);
            v.getContext().startActivity(intent);
        });

        // Dial Call to Emergency
        holder.callDriverIcon.setOnClickListener(v -> {
            driverClickListener.onPhoneClick(driver.getPhone());
        });

        // Dial Call to Driver
        holder.callEmergencyIcon.setOnClickListener(v -> {
            driverClickListener.onEmergencyPhoneClick(driver.getEmergencyPhone());
        });

    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder  extends RecyclerView.ViewHolder{

        ImageView btnEditDriver, callDriverIcon, callEmergencyIcon;
        TextView tvName, tvPhone, tvEmergencyPhone, tvDOB, tvjoiningDate, tvStatus,tvPin;
        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.driverName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmergencyPhone = itemView.findViewById(R.id.tvEmergencyPhone);
            tvDOB = itemView.findViewById(R.id.tvDOB);
            tvjoiningDate = itemView.findViewById(R.id.tv_joiningDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPin = itemView.findViewById(R.id.pin);

            btnEditDriver  = itemView.findViewById(R.id.ivEditDriver);
            callDriverIcon = itemView.findViewById(R.id.icon_call_driver);
            callEmergencyIcon = itemView.findViewById(R.id.icon_call_emergency);
        }
    }

}




