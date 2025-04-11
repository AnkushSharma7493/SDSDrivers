package com.sds.ui.models;

import androidx.lifecycle.ViewModel;

import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;

import java.io.Serializable;
import java.util.Date;

public class Duty extends ViewModel implements Serializable {

    private String dutyId;
    private String customerAddress;
    private String customerName;
    private String customerPhone;
    private String driverName;
    private String driverPhone;

    private Integer charges;
    private Integer commission;

    private CommissionStatus commissionStatus;
    private DutyStatus dutyStatus;

    private Date dutyReportingTime;
    private Date dutyStartTime;
    private Date dutyEndTime;

    public Duty(String dutyId, String customerAddress, String customerName, String customerPhone,
                String driverName, String driverPhone, Integer charges, Integer commission,
                CommissionStatus commissionStatus, DutyStatus dutyStatus,
                Date dutyReportingTime, Date dutyStartTime, Date dutyEndTime) {

        this.dutyId = dutyId;
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.charges = charges;
        this.commission = commission;
        this.commissionStatus = commissionStatus;
        this.dutyStatus = dutyStatus;
        this.dutyReportingTime = dutyReportingTime;
        this.dutyStartTime = dutyStartTime;
        this.dutyEndTime = dutyEndTime;
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

    public void setDutyReportingTime(Date dutyReportingTime) {
        this.dutyReportingTime = dutyReportingTime;
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

    public Integer getCharges() {
        return charges;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
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

    public String getDutyId() {
        return dutyId;
    }

    public void setDutyId(String dutyId) {
        this.dutyId = dutyId;
    }
}