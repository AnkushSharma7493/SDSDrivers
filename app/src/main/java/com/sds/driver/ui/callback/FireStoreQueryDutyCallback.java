package com.sds.driver.ui.callback;

import com.sds.driver.ui.models.Duty;

import java.util.List;

public interface FireStoreQueryDutyCallback {
    void onSuccess(List<Duty> duties);
    void onFailure(Exception e);
}
