package com.sds.ui.adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.R;
import com.sds.ui.callback.FirestoreCallback;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.listener.DutyListeners;
import com.sds.ui.models.Duty;
import com.sds.ui.services.AppUtility;
import com.sds.ui.services.FireStoreDutyService;

import java.util.List;

public class DriverDutyAdapter extends RecyclerView.Adapter<DriverDutyAdapter.DriverDutyViewHolder> {

    private List<Duty> dutyList;
    private FireStoreDutyService fireStoreDutyService;
    private DutyListeners dutyListeners;

    private DriverDutyViewHolder holder;

    public DriverDutyAdapter(DutyListeners listener, FireStoreDutyService fireStoreDutyService) {
        this.dutyListeners = listener;
        this.fireStoreDutyService=fireStoreDutyService;
    }

    public DriverDutyAdapter(List<Duty> dutyList) {
        this.dutyList = dutyList;
    }

    @NonNull
    @Override
    public DriverDutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_duty, parent, false);
        return new DriverDutyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverDutyViewHolder holder, int position) {
        Duty duty = dutyList.get(position);
        this.holder=holder;
        holder.tvCustomerName.setText("Customer Name : " + duty.getCustomerName());
        holder.tvCustomerPhone.setText("Customer Phone : " + duty.getCustomerPhone());
        holder.tvCustomerAddress.setText("Address : " + duty.getCustomerPhone());
        holder.tvDutyCharges.setText("Duty Charges : " + duty.getActualCharges());

        if (duty.getDutyReportingTime() != null) {
            holder.tvDutyDateTime.setText("Reporting: " + AppUtility.formatReportingDateTime(duty.getDutyReportingDate(), duty.getDutyReportingTime()));
        } else {
            holder.tvDutyDateTime.setText("Reporting: N/A");
        }

        //set visibility
        adjustVisibleButtons(duty);

        holder.btnStartDuty.setOnClickListener(v -> {
            duty.setDutyStatus(DutyStatus.IN_PROGRESS);
            adjustVisibleButtons(duty);
            updateDuty(duty);
        });

        holder.btnCompleteDuty.setOnClickListener(v -> {
            duty.setDutyStatus(DutyStatus.COMPLETED);
            adjustVisibleButtons(duty);
            updateDuty(duty);
            dutyList.remove(position);
            // Notify adapter
            notifyItemRemoved(position);

            if (dutyList.isEmpty() ) {
                dutyListeners.onDriverDutyListEmpty();
            }
        });


        holder.btnRejectDuty.setOnClickListener(v -> {
            duty.setDutyStatus(DutyStatus.REJECTED);
            adjustVisibleButtons(duty);
            updateDuty(duty);
            dutyList.remove(position);
            // Notify adapter
            notifyItemRemoved(position);
        });

        holder.btnAcceptDuty.setOnClickListener(v -> {
            duty.setDutyStatus(DutyStatus.ACCEPTED);
            adjustVisibleButtons(duty);
            updateDuty(duty);
        });

        // Dial Call to Customer
        holder.callCustomerIcon.setOnClickListener(v -> {
            dutyListeners.onCustomerPhoneClick(duty.getCustomerPhone());
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
        } else if(DutyStatus.IN_PROGRESS.equals(duty.getDutyStatus())){
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

    private void updateDuty(Duty duty){
        try{
            // Update duty
            fireStoreDutyService.saveDuty(duty, new FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(holder.itemView.getContext(), "Duty Updated Successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(holder.itemView.getContext(), "Update Failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(holder.itemView.getContext(), "Error occured while Update Duty"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return dutyList.size();
    }

    public List<Duty> getDutyList() {
        return dutyList;
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