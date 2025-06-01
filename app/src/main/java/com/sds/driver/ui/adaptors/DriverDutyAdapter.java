package com.sds.driver.ui.adaptors;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.driver.R;
import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.listener.DutyListeners;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.AppUtility;

import java.util.List;

public class DriverDutyAdapter extends RecyclerView.Adapter<DriverDutyAdapter.DriverDutyViewHolder> {

    private List<Duty> dutyList;
    private DutyListeners dutyListeners;
    private DriverDutyViewHolder holder;
    private Context context;


    public DriverDutyAdapter(DutyListeners listener) {
        this.dutyListeners = listener;
    }


    @NonNull
    @Override
    public DriverDutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_duty, parent, false);
        return new DriverDutyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverDutyViewHolder holder, int position) {
        this.holder=holder;
        context = holder.itemView.getContext();

        holder.tvCustomerName.setText("Customer Name : " + dutyList.get(position).getCustomerName());
        holder.tvCustomerPhone.setText("Customer Phone : " + dutyList.get(position).getCustomerPhone());
        holder.tvCustomerAddress.setText("Address : " + dutyList.get(position).getCustomerPhone());
        holder.tvDutyCharges.setText("Duty Charges : " + dutyList.get(position).getActualCharges());

        if (dutyList.get(position).getDutyReportingTime() != null) {
            holder.tvDutyDateTime.setText("Reporting: " + AppUtility.formatReportingDateTime(dutyList.get(position).getDutyReportingDate(), dutyList.get(position).getDutyReportingTime()));
        } else {
            holder.tvDutyDateTime.setText("Reporting: N/A");
        }

        //set visibility
        adjustVisibleButtons(dutyList.get(position));

        holder.btnStartDuty.setOnClickListener(v -> {
            dutyListeners.updateDuty(dutyList.get(position), position, DutyStatus.INPROGRESS);
        });

        holder.btnCompleteDuty.setOnClickListener(v -> {
            dutyListeners.updateDuty(dutyList.get(position), position, DutyStatus.COMPLETED);
        });

        holder.btnRejectDuty.setOnClickListener(v -> {
            dutyListeners.updateDuty(dutyList.get(position), position, DutyStatus.REJECTED);
        });

        holder.btnAcceptDuty.setOnClickListener(v -> {
            dutyListeners.updateDuty(dutyList.get(position), position, DutyStatus.ACCEPTED);
        });

        // Dial Call to Customer
        holder.callCustomerIcon.setOnClickListener(v -> {
            dutyListeners.onCustomerPhoneClick(dutyList.get(position).getCustomerPhone());
        });

    }

    private void adjustVisibleButtons(Duty duty){
        if(DutyStatus.ACCEPTED.equals(duty.getDutyStatus())){
            holder.lldutyAcceptRejectStatusButtonRow.setVisibility(View.GONE);
            holder.lldutyStartStopButtonRow.setVisibility(View.VISIBLE);
            holder.btnStartDuty.setVisibility(View.VISIBLE);
            holder.btnCompleteDuty.setVisibility(View.GONE);
        } else if(DutyStatus.REJECTED.equals(duty.getDutyStatus())){
            holder.lldutyAcceptRejectStatusButtonRow.setVisibility(View.GONE);
            holder.lldutyStartStopButtonRow.setVisibility(View.GONE);
        } else if(DutyStatus.INPROGRESS.equals(duty.getDutyStatus())){
            holder.lldutyAcceptRejectStatusButtonRow.setVisibility(View.GONE);
            holder.lldutyStartStopButtonRow.setVisibility(View.VISIBLE);
            holder.btnStartDuty.setVisibility(View.GONE);
            holder.btnCompleteDuty.setVisibility(View.VISIBLE);
        } else if(DutyStatus.COMPLETED.equals(duty.getDutyStatus())){
            holder.lldutyAcceptRejectStatusButtonRow.setVisibility(View.GONE);
            holder.lldutyStartStopButtonRow.setVisibility(View.GONE);
        } else if(DutyStatus.ASSIGNED.equals(duty.getDutyStatus())){
            holder.lldutyAcceptRejectStatusButtonRow.setVisibility(View.VISIBLE);
            holder.lldutyStartStopButtonRow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dutyList.size();
    }

    public void setDutyList(List<Duty> dutyList) {
        this.dutyList = dutyList;
    }

    static class DriverDutyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName,tvCustomerPhone,tvCustomerAddress,tvDutyCharges,tvDutyDateTime,tvNote;
        Button btnStartDuty, btnCompleteDuty, btnAcceptDuty, btnRejectDuty;
        ImageView callCustomerIcon;
        LinearLayout lldutyAcceptRejectStatusButtonRow, lldutyStartStopButtonRow;

        public DriverDutyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvDutyCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvDutyCustomerPhone);
            tvDutyDateTime = itemView.findViewById(R.id.tvDutyReportingTime);
            tvDutyCharges = itemView.findViewById(R.id.tvDutyCharge);
            tvCustomerAddress = itemView.findViewById(R.id.tvDutyaddress);
            tvNote = itemView.findViewById(R.id.tvDutynote);

            callCustomerIcon = itemView.findViewById(R.id.icon_call_duty_customer);

            btnStartDuty = itemView.findViewById(R.id.btnStartDuty);
            btnCompleteDuty = itemView.findViewById(R.id.btnCompleteDuty);
            btnAcceptDuty = itemView.findViewById(R.id.btnAcceptDuty);
            btnRejectDuty = itemView.findViewById(R.id.btnRejectDuty);

            lldutyAcceptRejectStatusButtonRow = itemView.findViewById(R.id.dutyAcceptRejectStatusButtonRow);
            lldutyStartStopButtonRow = itemView.findViewById(R.id.dutyStartStopButtonRow);

        }
    }
}