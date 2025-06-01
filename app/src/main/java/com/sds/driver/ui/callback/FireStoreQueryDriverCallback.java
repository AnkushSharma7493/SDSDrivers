package com.sds.driver.ui.callback;

import com.sds.driver.ui.models.Driver;
import java.util.List;

public interface FireStoreQueryDriverCallback {
    void onSuccess(List<Driver> duties);
    void onFailure(Exception e);
}
