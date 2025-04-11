package com.sds.ui.adaptors;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sds.R;
import com.sds.ui.activities.DutyDetailActivity;
import com.sds.ui.activities.OnPhoneClickListener;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import com.sds.ui.services.DutyService;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DutyAdapter extends RecyclerView.Adapter<DutyAdapter.DutyViewHolder> {

    private List<Duty> dutyList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private OnPhoneClickListener phoneClickListener;

    public DutyAdapter(OnPhoneClickListener listener) {
        this.dutyList = DutyService.getInstance().getDuties();
        this.phoneClickListener = listener;
    }

    @NonNull
    @Override
    public DutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_duty, parent, false);
        return new DutyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DutyViewHolder holder, int position) {
        Duty duty = dutyList.get(position);

        holder.tvCustomerName.setText("Customer: " + duty.getCustomerName());
        holder.tvCustomerPhone.setText("Customer Phone: " + duty.getCustomerPhone());
        holder.tvDriverName.setText("Driver: " + duty.getDriverName());
        holder.tvDriverPhone.setText("Driver Phone: " + duty.getDriverPhone());

        if (duty.getDutyReportingTime() != null) {
            holder.tvDutyDateTime.setText("Reporting: " + dateFormat.format(duty.getDutyReportingTime()));
        } else {
            holder.tvDutyDateTime.setText("Reporting: N/A");
        }

        holder.tvDutyStatus.setText("Status: " + duty.getDutyStatus().name());

        // Complete Button Action
        holder.btnMarkComplete.setOnClickListener(v -> {
            duty.setDutyStatus(DutyStatus.COMPLETED);
            notifyItemChanged(position);
        });

        // Send SMS to Customer
        holder.btnSendSmsCustomer.setOnClickListener(v -> {
            if (phoneClickListener != null) {
                phoneClickListener.onCustomerSmsClick(duty.getCustomerPhone()); // Or customer
            }
        });

        // Send SMS to Driver
        holder.btnSendSmsDriver.setOnClickListener(v -> {
            if (phoneClickListener != null) {
                phoneClickListener.onDriverSmsClick(duty.getDriverPhone()); // Or driverPhone
            }
        });

        // Edit Duty
        holder.btnEditDuty.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DutyDetailActivity.class);
            intent.putExtra("duty", duty);
            intent.putExtra("edit_mode", true);
            v.getContext().startActivity(intent);
        });

        // Dial Call to Customer
        holder.callCustomerIcon.setOnClickListener(v -> {
            phoneClickListener.onCustomerPhoneClick(duty.getCustomerPhone());
        });

        // Dial Call to Driver
        holder.callDriverIcon.setOnClickListener(v -> {
            phoneClickListener.onDriverPhoneClick(duty.getDriverPhone());
        });

    }

    public void updateList(List<Duty> newList) {
        this.dutyList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dutyList.size();
    }

    static class DutyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvDriverName, tvCustomerPhone, tvDriverPhone, tvDutyDateTime, tvDutyStatus;
        Button btnMarkComplete, btnSendSmsCustomer, btnSendSmsDriver;
        ImageView btnEditDuty, callCustomerIcon, callDriverIcon;

        public DutyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvDriverPhone = itemView.findViewById(R.id.tvDriverPhone);
            tvDutyDateTime = itemView.findViewById(R.id.tvReportingTime);
            tvDutyStatus = itemView.findViewById(R.id.tvDutyStatus);

            btnMarkComplete = itemView.findViewById(R.id.btnMarkComplete);
            btnSendSmsCustomer = itemView.findViewById(R.id.btnSmsCustomer);
            btnSendSmsDriver = itemView.findViewById(R.id.btnSmsDriver);
            btnEditDuty = itemView.findViewById(R.id.ivEditDuty);
            callCustomerIcon = itemView.findViewById(R.id.icon_call_customer);
            callDriverIcon = itemView.findViewById(R.id.icon_call_driver);
        }
    }

}




