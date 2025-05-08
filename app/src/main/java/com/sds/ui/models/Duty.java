package com.sds.ui.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Duty extends ViewModel implements Parcelable {

    private String dutyId;
    private String customerAddress;
    private String customerName;
    private String customerPhone;
    private String driverName;
    private String driverPhone;
    private Integer actualCharges;
    private Integer charges;
    private Integer commission;
    private CommissionStatus commissionStatus;
    private DutyStatus dutyStatus;
    private Date dutyReportingDate;
    private Date dutyReportingTime;
    private Date dutyStartTime;
    private Date dutyEndTime;
    private String notes;
    private Date dutyRegisteredOn;
    private List<RejectDuty> rejectReason = new ArrayList<>();

    @ServerTimestamp
    private Timestamp lastUpdated;

    public Duty(){
        this.dutyId= UUID.randomUUID().toString();
    }
    public Duty(String dutyId, String customerAddress, String customerName, String customerPhone,
                String driverName, String driverPhone, Integer actualCharges,Integer charges, Integer commission,
                CommissionStatus commissionStatus, DutyStatus dutyStatus,
                Date dutyReportingTime, Date dutyStartTime, Date dutyEndTime, Date dutyReportingDate, String notes,List<RejectDuty> rejectReason,Date dutyRegisteredOn) {
        this.dutyId = dutyId;
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.actualCharges = actualCharges;
        this.charges = charges;
        this.commission = commission;
        this.commissionStatus = commissionStatus;
        this.dutyStatus = dutyStatus;
        this.dutyReportingDate=dutyReportingDate;
        this.dutyReportingTime = dutyReportingTime;
        this.dutyStartTime = dutyStartTime;
        this.dutyEndTime = dutyEndTime;
        this.notes=notes;
        this.rejectReason=rejectReason;
        this.dutyRegisteredOn=dutyRegisteredOn;
    }


    protected Duty(Parcel in) {
        this.dutyId = in.readString();
        this.customerAddress = in.readString();
        this.customerName = in.readString();
        this.customerPhone = in.readString();
        this.driverName = in.readString();
        this.driverPhone = in.readString();
        this.actualCharges = in.readInt();
        this.charges = in.readInt();
        this.commission = in.readInt();
        this.commissionStatus = CommissionStatus.valueOf(in.readString());
        this.dutyStatus = DutyStatus.valueOf(in.readString());
        this.dutyReportingDate=new Date(in.readLong());
        this.dutyReportingTime = new Date(in.readLong());
        this.dutyStartTime = new Date(in.readLong());
        this.dutyEndTime = new Date(in.readLong());
        this.lastUpdated = new Timestamp(new Date(in.readLong()));
        this.notes = in.readString();
        this.rejectReason = in.createTypedArrayList(RejectDuty.CREATOR);
        this.dutyRegisteredOn=new Date(in.readLong());

    }

    public static final Creator<Duty> CREATOR = new Creator<Duty>() {
        @Override
        public Duty createFromParcel(Parcel in) {
            return new Duty(in);
        }

        @Override
        public Duty[] newArray(int size) {
            return new Duty[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.dutyId);
        dest.writeString(this.customerAddress);
        dest.writeString(this.customerName);
        dest.writeString(this.customerPhone);
        dest.writeString(this.driverName);
        dest.writeString(this.driverPhone);
        dest.writeInt(this.actualCharges ==null?0:this.actualCharges);
        dest.writeInt(this.charges ==null?0:this.charges);
        dest.writeInt(this.commission==null?0:this.commission);
        dest.writeString(this.commissionStatus.name());
        dest.writeString(this.dutyStatus.name());
        dest.writeLong(dutyReportingDate != null ? dutyReportingDate.getTime() : 0);
        dest.writeLong(dutyReportingTime != null ? dutyReportingTime.getTime() : 0);
        dest.writeLong(dutyStartTime != null ? dutyStartTime.getTime() : 0);
        dest.writeLong(dutyEndTime != null ? dutyEndTime.getTime() : 0);
        dest.writeLong(lastUpdated != null ? lastUpdated.toDate().getTime() : 0);
        dest.writeString(this.notes);
        dest.writeTypedList(rejectReason);
        dest.writeLong(dutyRegisteredOn != null ? dutyRegisteredOn.getTime() : 0);
    }


    // Generate Getters and Setters

    public Date getDutyEndTime() {
        return dutyEndTime;
    }

    public void setDutyEndTime(Date dutyEndTime) {
        this.dutyEndTime = dutyEndTime;
    }

    public Date getDutyStartTime() {
        return dutyStartTime;
    }

    public void setDutyStartTime(Date dutyStartTime) {
        this.dutyStartTime = dutyStartTime;
    }

    public Date getDutyReportingTime() {
        return dutyReportingTime;
    }

    public CommissionStatus getCommissionStatus() {
        return commissionStatus;
    }

    public void setCommissionStatus(CommissionStatus commissionStatus) {
        this.commissionStatus = commissionStatus;
    }

    public DutyStatus getDutyStatus() {
        return dutyStatus;
    }

    public void setDutyStatus(DutyStatus dutyStatus) {
        this.dutyStatus = dutyStatus;
    }

    public Integer getCommission() {
        return commission;
    }

    public void setCommission(Integer commission) {
        this.commission = commission;
    }

    public Integer getActualCharges() {
        return actualCharges;
    }

    public void setActualCharges(Integer actualCharges) {
        this.actualCharges = actualCharges;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public Date getDutyReportingDate() {
        return dutyReportingDate;
    }

    public void setDutyReportingDate(Date reoprtinDate) {
        this.dutyReportingDate = reoprtinDate;
    }

    public void setDutyReportingTime(Date dutytime) {
        this.dutyReportingTime = dutytime;
    }

    public String getDutyId() { return dutyId; }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDutyId(String dutyId) { this.dutyId = dutyId;}

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public Integer getCharges() {
        return charges;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public List<RejectDuty> getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(List<RejectDuty> rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Date getDutyRegisteredOn() {
        return dutyRegisteredOn;
    }

    public void setDutyRegisteredOn(Date dutyRegisteredOn) {
        this.dutyRegisteredOn = dutyRegisteredOn;
    }
}