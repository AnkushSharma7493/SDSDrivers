package com.sds.driver.ui.callback;


import com.sds.driver.ui.models.AppSettings;

import java.util.List;

public interface FireStoreQueryAppSettingsCallback {
    void onSuccess(List<AppSettings> appSettings);
    void onFailure(Exception e);
}
