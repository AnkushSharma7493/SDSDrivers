package com.sds.ui.callback;

import com.sds.ui.models.Duty;

import java.util.List;

public interface FireStoreQueryDutyCallback {
    void onSuccess(List<Duty> duties);
    void onFailure(Exception e);
}
