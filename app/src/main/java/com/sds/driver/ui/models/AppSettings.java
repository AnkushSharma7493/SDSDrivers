package com.sds.driver.ui.models;

public class AppSettings {
    private String criticalOperationPassword;
    private int commissionPercentage;
    private String upi_id;
    private String upi_name;
    private String adminPhone;
    private boolean skipCache;
    private boolean dateCriteria;
    private boolean displayException;
    private String version;
    private boolean disableNotification=false;


    public String getCriticalOperationPassword() {
        return criticalOperationPassword;
    }

    public void setCriticalOperationPassword(String criticalOperationPassword) {
        this.criticalOperationPassword = criticalOperationPassword;
    }

    public int getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(int commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public boolean isSkipCache() {
        return skipCache;
    }

    public void setSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
    }

    public boolean isDateCriteria() {
        return dateCriteria;
    }

    public void setDateCriteria(boolean dateCriteria) {
        this.dateCriteria = dateCriteria;
    }

    public boolean isDisplayException() {
        return displayException;
    }

    public void setDisplayException(boolean displayException) {
        this.displayException = displayException;
    }

    public String getUpi_id() {
        return upi_id;
    }

    public void setUpi_id(String upi_id) {
        this.upi_id = upi_id;
    }

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpi_name() {
        return upi_name;
    }

    public void setUpi_name(String upi_name) {
        this.upi_name = upi_name;
    }

    public boolean isDisableNotification() {
        return disableNotification;
    }

    public void setDisableNotification(boolean disableNotification) {
        this.disableNotification = disableNotification;
    }
}
