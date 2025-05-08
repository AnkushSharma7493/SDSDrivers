package com.sds.ui.adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sds.R;
import com.sds.ui.activities.driver.DriverDutyActivity;
import com.sds.ui.models.Duty;

import java.util.List;

public class DriverDutyListAdapter extends RecyclerView.Adapter<DriverDutyListAdapter.DutyViewHolder> {

    private List<Duty> dutyList;
    private Context context;

    public DriverDutyListAdapter(Context context, List<Duty> dutyList) {
        this.context = context;
        this.dutyList = dutyList;
    }

    @NonNull
    @Override
    public DutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_duty, parent, false);
        return new DutyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DutyViewHolder holder, int position) {
        Duty duty = dutyList.get(position);

        holder.tvDutyId.setText(duty.getDutyId());
        holder.tvCustomerName.setText(duty.getCustomerName());
        holder.tvCustomerAddress.setText(duty.getCustomerAddress());
        holder.tvCommission.setText("₹" + duty.getCommission());
        holder.tvCharges.setText("₹" + duty.getCharges());

        // Set row color based on commission status
        switch (duty.getCommissionStatus().getStatus()) {
            case "PAID":
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                break;
            case "UNPAID":
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
                break;
            case "PENDING":
                holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DriverDutyActivity.class);
            intent.putExtra("dutyId", duty.getDutyId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dutyList.size();
    }

    static class DutyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDutyId, tvCustomerName, tvCustomerAddress, tvCommission, tvCharges;
        LinearLayout itemLayout;

        DutyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDutyId = itemView.findViewById(R.id.tvDutyId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvCommission = itemView.findViewById(R.id.tvCommission);
            tvCharges = itemView.findViewById(R.id.tvCharges);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }
    }
}
