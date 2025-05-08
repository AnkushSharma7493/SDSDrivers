package com.sds.ui.adaptors;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.sds.R;
import com.sds.ui.activities.DutyDetailActivity;
import com.sds.ui.enums.ApplicationConstant;
import com.sds.ui.listener.DutyListeners;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import com.sds.ui.services.AppUtility;

import java.util.ArrayList;
import java.util.List;

public class DutyAdapter extends RecyclerView.Adapter<DutyAdapter.DutyViewHolder> {

    private List<Duty> dutyList=new ArrayList<>();
    private DutyListeners dutyListeners;


    public DutyAdapter(DutyListeners listener) {
        this.dutyListeners = listener;
    }

    public void setDuties(List<Duty> dutyList){
        this.dutyList=dutyList;
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
        holder.tvCustomerName.setText("Customer : " + duty.getCustomerName());
        holder.tvCustomerPhone.setText("Customer Phone : " + duty.getCustomerPhone());
        holder.tvDriverName.setText("Driver : " + duty.getDriverName());
        holder.tvDriverPhone.setText("Driver Phone : " + duty.getDriverPhone());
        holder.tvDutyCharges.setText("Duty Charges : " + duty.getActualCharges());

        if (duty.getDutyReportingTime() != null) {
            holder.tvDutyDateTime.setText("Reporting: " + AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime()));
        } else {
            holder.tvDutyDateTime.setText("Reporting: N/A");
        }

        holder.tvDutyStatus.setText("Status: " + duty.getDutyStatus().name());


        holder.btnMarkStatus.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMarkStatus);
            for(String status : DutyStatus.getStringValues()) {
                if(!DutyStatus.ALL.getStatus().equals(status)) {
                    popup.getMenu().add(status);
                }
            }

            popup.setOnMenuItemClickListener(item -> {
                String selectedStatus = item.getTitle().toString();
                if (dutyListeners != null) {
                    duty.setDutyStatus(DutyStatus.getByStatus(selectedStatus));
                    if(DutyStatus.UNASSIGNED.equals(duty.getDutyStatus())) {
                        duty.setDriverPhone(ApplicationConstant.DRIVER_PHONE_DEFAULT);
                        duty.setDriverName(ApplicationConstant.DRIVER_NAME_DEFAULT);
                    }
                        notifyItemChanged(position); // re render it.
                        dutyListeners.updateDuty(duty);

                }
                return true;
            });

            popup.show();
        });



        // Complete Button Action
//        holder.btnMarkStatus.setOnClickListener(v -> {
//            if (dutyListeners != null) {
//                duty.setDutyStatus(DutyStatus.COMPLETED);
//                notifyItemChanged(position); // re render it.
//                dutyListeners.updateDuty(duty);
//            }
//        });

        // Send SMS to Customer
        holder.btnSendSmsCustomer.setOnClickListener(v -> {
            if (dutyListeners != null) {
                dutyListeners.onCustomerSmsClick(duty); // Or customer
            }
        });

        // Send SMS to Driver
        holder.btnSendSmsDriver.setOnClickListener(v -> {
            if (dutyListeners != null) {
                dutyListeners.onDriverSmsClick(duty); // Or driverPhone
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
            dutyListeners.onCustomerPhoneClick(duty.getCustomerPhone());
        });

        // Dial Call to Driver
        holder.callDriverIcon.setOnClickListener(v -> {
            dutyListeners.onDriverPhoneClick(duty.getDriverPhone());
        });

        // Set background color based on status
        switch (duty.getDutyStatus().getStatus()) {
            case "Unassigned":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                break;
            case "Assigned":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.yellow));
                break;
            case "InProgress":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
                break;
            case "Completed":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue));
                break;
            case "Cancelled":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange));
                break;
            case "Accepted":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_200));
                break;
            case "Rejected":
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
                break;
            default:
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                break;
        }

    }




    @Override
    public int getItemCount() {
        return dutyList.size();
    }

    static class DutyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvDriverName, tvCustomerPhone, tvDriverPhone, tvDutyDateTime, tvDutyStatus,tvDutyCharges;
        Button btnMarkStatus, btnSendSmsCustomer, btnSendSmsDriver;
        ImageView btnEditDuty, callCustomerIcon, callDriverIcon;

        public DutyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvDriverPhone = itemView.findViewById(R.id.tvDriverPhone);
            tvDutyDateTime = itemView.findViewById(R.id.tvReportingTime);
            tvDutyStatus = itemView.findViewById(R.id.tvStatus);
            tvDutyCharges = itemView.findViewById(R.id.tvDutyCharges);

            btnMarkStatus = itemView.findViewById(R.id.btnMarkStatus);
            btnSendSmsCustomer = itemView.findViewById(R.id.btnSmsCustomer);
            btnSendSmsDriver = itemView.findViewById(R.id.btnSmsDriver);
            btnEditDuty = itemView.findViewById(R.id.ivEditDuty);
            callCustomerIcon = itemView.findViewById(R.id.icon_call_customer);
            callDriverIcon = itemView.findViewById(R.id.icon_call_driver);
        }
    }



}




