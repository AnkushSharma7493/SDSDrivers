package com.sds.driver.ui.listener;

import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.models.Duty;

public interface DutyListeners {
    default void onCustomerPhoneClick(String phoneNumber){};
    default void onDriverPhoneClick(String phoneNumber){};
    default void onCustomerSmsClick(Duty duty){};
    default void onDriverSmsClick(Duty duty){};
    default void onDriverDutyListEmpty(){};
    default void onDrivrDutyListReFill(){};
    default void updateDuty(Duty duty){};

    default void updateDuty(Duty duty, int position, DutyStatus status){};

//    default void onDriverDutyClick(Duty duty){};
//
//    default void launchDriverPinAlert(Duty duty,int position){};

}