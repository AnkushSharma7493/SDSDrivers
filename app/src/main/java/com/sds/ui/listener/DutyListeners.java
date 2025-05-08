package com.sds.ui.listener;

import com.sds.ui.models.Duty;

public interface DutyListeners {
    void onCustomerPhoneClick(String phoneNumber);
    void onDriverPhoneClick(String phoneNumber);
    void onCustomerSmsClick(Duty duty);
    void onDriverSmsClick(Duty duty);
    default void onDriverDutyListEmpty(){};
    default void onDrivrDutyListReFill(){};
    default void updateDuty(Duty duty){};

}