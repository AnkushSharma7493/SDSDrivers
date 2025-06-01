package com.sds.driver.ui.callback;

public interface FirestoreCallback {
    void onSuccess();
    void onFailure(Exception e);
}
