package com.sds.driver.ui.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Driver implements Parcelable {

    private String name;
    private String phone;
    private String emergencyPhone;
    private String currentAddress;
    private String nativeAddress;
    private Date DOB;
    private String aadhaarNo;
    private String licenceNo;
    private Integer driverExperience;
    private Date joiningDate;
    private boolean status;
    private String pin;
    private String role;
    private String note;

    @ServerTimestamp
    private Timestamp lastUpdated;

    // Empty constructor (required for Firestore)
    public Driver() {
    }

    // Parcelable constructor
    protected Driver(Parcel in) {
        name = in.readString();
        phone = in.readString();
        emergencyPhone = in.readString();
        currentAddress = in.readString();
        nativeAddress = in.readString();

        long dobMillis = in.readLong();
        DOB = dobMillis == -1 ? null : new Date(dobMillis);

        aadhaarNo = in.readString();
        licenceNo = in.readString();

        long joiningMillis = in.readLong();
        joiningDate = joiningMillis == -1 ? null : new Date(joiningMillis);

        status = in.readByte() != 0;
        pin = in.readString();
        role = in.readString();
        note = in.readString();

        if (in.readByte() == 0) {
            driverExperience = null;
        } else {
            driverExperience = in.readInt();
        }

        lastUpdated = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(emergencyPhone);
        dest.writeString(currentAddress);
        dest.writeString(nativeAddress);
        dest.writeLong(DOB != null ? DOB.getTime() : -1);

        dest.writeString(aadhaarNo);
        dest.writeString(licenceNo);

        dest.writeLong(joiningDate != null ? joiningDate.getTime() : -1);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeString(pin);
        dest.writeString(role);
        dest.writeString(note != null ? note : "test");

        if (driverExperience == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(driverExperience);
        }
        dest.writeParcelable(lastUpdated, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Driver> CREATOR = new Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    // Getters and Setters (optional – add as needed)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }

    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }

    public String getNativeAddress() { return nativeAddress; }
    public void setNativeAddress(String nativeAddress) { this.nativeAddress = nativeAddress; }

    public Date getDOB() { return DOB; }
    public void setDOB(Date DOB) { this.DOB = DOB; }

    public String getAadhaarNo() { return aadhaarNo; }
    public void setAadhaarNo(String aadhaarNo) { this.aadhaarNo = aadhaarNo; }

    public String getLicenceNo() { return licenceNo; }
    public void setLicenceNo(String licenceNo) { this.licenceNo = licenceNo; }

    public Integer getDriverExperience() { return driverExperience; }
    public void setDriverExperience(Integer driverExperience) { this.driverExperience = driverExperience; }

    public Date getJoiningDate() { return joiningDate; }
    public void setJoiningDate(Date joiningDate) { this.joiningDate = joiningDate; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}