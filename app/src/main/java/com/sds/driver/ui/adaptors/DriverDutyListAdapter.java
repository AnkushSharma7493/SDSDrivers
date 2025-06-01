package com.sds.driver.ui.adaptors;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.driver.R;
import com.sds.driver.ui.activities.DriverDutyDetailActivity;
import com.sds.driver.ui.models.Duty;
import com.sds.driver.ui.services.AppUtility;

import java.util.ArrayList;
import java.util.List;

public class DriverDutyListAdapter extends RecyclerView.Adapter<DriverDutyListAdapter.DutyViewHolder> {

    private List<Duty> dutyList=new ArrayList<>();

    public DriverDutyListAdapter() {

    }

    @NonNull
    @Override
    public DutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_duty_list, parent, false);
        return new DutyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DutyViewHolder holder, int position) {
        Duty duty = dutyList.get(position);

        holder.tvdutyDate.setText(AppUtility.formatReportingDateTime(duty.getDutyReportingDate(),duty.getDutyReportingTime()));
        holder.tvCommission.setText("\u20B9" + duty.getCommission());
        holder.tvdutyCharges.setText("\u20B9" + duty.getActualCharges());

        // Set row color based on commission status
        switch (duty.getCommissionStatus().getStatus()) {
            case "RECEIVED":
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grey));
                break;
            case "PENDING":
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.reddark));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DriverDutyDetailActivity.class);
            intent.putExtra("duty", duty);
            holder.itemView.getContext().startActivity(intent);
        });
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

    static class DutyViewHolder extends RecyclerView.ViewHolder {
        TextView tvdutyCharges, tvdutyDate, tvCommission;
        LinearLayout itemLayout;

        DutyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvdutyCharges = itemView.findViewById(R.id.tvdriverdutyCharges);
            tvdutyDate = itemView.findViewById(R.id.tvdriverdutydate);
            tvCommission = itemView.findViewById(R.id.tvCommission);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }
    }
}
