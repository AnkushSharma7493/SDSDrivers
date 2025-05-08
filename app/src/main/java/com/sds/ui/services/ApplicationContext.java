package com.sds.ui.services;

import com.sds.ui.enums.ErrorCodeEnum;

import java.util.ArrayList;

public class ApplicationContext {

    // configurable properties
    private static String critalOperationPassword = "123";
    private static int COMMISSION_PERCENTAGE=10;
    private static boolean skipCache=false;
    private static boolean dateCriteria=false;
    private static boolean showError=true;

    private static ArrayList<ErrorCodeEnum> stacktrace=new ArrayList<>();


    //final param
    public static final String DRIVER_CACHE="drivers_cache";
    public static final String DRIVER_PHONE_CACHE="drivers_phone";
    public static final String DRIVER_PIN_CACHE="drivers_pin";
    public static final String DRIVER_ROLE_CACHE="drivers_role";
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

}
