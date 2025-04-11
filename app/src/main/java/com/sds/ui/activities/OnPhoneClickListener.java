package com.sds.ui.activities;

public interface OnPhoneClickListener {
    void onCustomerPhoneClick(String phoneNumber);
    void onDriverPhoneClick(String phoneNumber);
    void onCustomerSmsClick(String phoneNumber);
    void onDriverSmsClick(String phoneNumber);
}