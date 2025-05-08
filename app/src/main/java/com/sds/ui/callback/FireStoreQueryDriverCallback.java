package com.sds.ui.callback;

import com.sds.ui.models.Driver;
import java.util.List;

public interface FireStoreQueryDriverCallback {
    void onSuccess(List<Driver> duties);
    void onFailure(Exception e);
}
