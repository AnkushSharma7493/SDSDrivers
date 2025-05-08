package com.sds.ui.callback;

public interface FirestoreCallback {
    void onSuccess();
    void onFailure(Exception e);
}
