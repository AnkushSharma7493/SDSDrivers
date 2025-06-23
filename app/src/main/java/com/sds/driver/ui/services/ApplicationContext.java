package com.sds.driver.ui.services;

import android.app.PendingIntent;

import com.sds.driver.ui.enums.ErrorCodeEnum;
import com.sds.driver.ui.models.Driver;

import java.util.ArrayList;

public class ApplicationContext {

    // configurable properties
    private static String critalOperationPassword = "123";
    private static int COMMISSION_PERCENTAGE=10;
    private static boolean skipCache=false;
    private static boolean dateCriteria=false;
    private static boolean showError=true;
    private static String adminPhone="8800130490";
    private static Driver profile;

    private static String upi_id;
    private static String upi_name;
    private static ArrayList<ErrorCodeEnum> stacktrace=new ArrayList<>();

    private static Driver driver=null;

    private static PendingIntent pendingIntent;


    //final param
    public static final String DRIVER_CACHE="drivers_cache";
    public static final String DRIVER_PHONE_CACHE="drivers_phone";
    public static final String DRIVER_PIN_CACHE="drivers_pin";
    public static final String DRIVER_ROLE_CACHE ="driver_role";
    public static final String DRIVER_ROLE="driver";
    public static final String ADMIN_ROLE="admin";

    public static String LOGIN_USER_PHONE="";


    public static String getCritalOperationPassword() {
        return critalOperationPassword;
    }

    public static void setCritalOperationPassword(String critalOperationPassword) {
        ApplicationContext.critalOperationPassword = critalOperationPassword;
    }

    public static int getCommissionPercentage() {
        return COMMISSION_PERCENTAGE;
    }

    public static void setCommissionPercentage(int commissionPercentage) {
        COMMISSION_PERCENTAGE = commissionPercentage;
    }

    public static boolean isSkipCache() {
        return skipCache;
    }

    public static void setSkipCache(boolean skipCache) {
        ApplicationContext.skipCache = skipCache;
    }

    public static boolean isDateCriteria() {
        return dateCriteria;
    }

    public static void setDateCriteria(boolean dateCriteria) {
        ApplicationContext.dateCriteria = dateCriteria;
    }

    public static boolean isShowError() {
        return showError;
    }

    public static void setShowError(boolean showError) {
        ApplicationContext.showError = showError;
    }

    public static ArrayList<ErrorCodeEnum> getStacktrace() {
        return stacktrace;
    }

    public static void setStacktrace(ArrayList<ErrorCodeEnum> stacktrace) {
        ApplicationContext.stacktrace = stacktrace;
    }

    public static void addStacktrace(ErrorCodeEnum stacktrace) {
        ApplicationContext.stacktrace.add(stacktrace);
    }

    public static String getAdminPhone() {
        return adminPhone;
    }

    public static void setAdminPhone(String adminPhone) {
        ApplicationContext.adminPhone = adminPhone;
    }

    public static Driver getProfile() {
        return profile;
    }

    public static void setProfile(Driver profile) {
        ApplicationContext.profile = profile;
    }

    public static String getUpi_id() {
        return upi_id;
    }

    public static void setUpi_id(String upiid) {
        upi_id = upiid;
    }

    public static String getUpi_name() {
        return upi_name;
    }

    public static void setUpi_name(String upiname) {
        upi_name = upiname;
    }

    public static Driver getDriver() {
        return driver;
    }

    public static void setDriver(Driver driver) {
        ApplicationContext.driver = driver;
    }

    public static PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public static void setPendingIntent(PendingIntent pendingIntent) {
        ApplicationContext.pendingIntent = pendingIntent;
    }
}

