package com.sds.ui.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;


public class RejectDuty implements Parcelable {

    private String dutyId;
    private String driverPhone;
    private String rejectReason;

    private Date rejectTime;

    // Constructor
    public RejectDuty(String dutyId, String driverPhone, String rejectReason,Date rejectTime) {
        this.dutyId = dutyId;
        this.driverPhone = driverPhone;
        this.rejectReason = rejectReason;
        this.rejectTime = rejectTime;
    }

    protected RejectDuty(Parcel in) {
        dutyId = in.readString();
        driverPhone = in.readString();
        rejectReason = in.readString();
        rejectTime = new Date(in.readLong());
    }

    public static final Creator<RejectDuty> CREATOR = new Creator<RejectDuty>() {
        @Override
        public RejectDuty createFromParcel(Parcel in) {
            return new RejectDuty(in);
        }

        @Override
        public RejectDuty[] newArray(int size) {
            return new RejectDuty[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dutyId);
        dest.writeString(driverPhone);
        dest.writeString(rejectReason);
        dest.writeLong(rejectTime != null ? rejectTime.getTime() : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters & setters (optional)


    public String getDutyId() {
        return dutyId;
    }

    public void setDutyId(String dutyId) {
        this.dutyId = dutyId;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}

