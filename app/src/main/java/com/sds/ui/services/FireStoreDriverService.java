package com.sds.ui.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sds.ui.callback.FireStoreQueryDriverCallback;
import com.sds.ui.callback.FirestoreCallback;
import com.sds.ui.enums.ErrorCodeEnum;
import com.sds.ui.models.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FireStoreDriverService {


    private final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final static List<Driver> cachedDrivers = new ArrayList<>();
    private static long LOCAL_LAST_UPDATED_TIMESTAMP=0;
    private static final String COLLECTION_NAME = "drivers";
    private static final String ETAG_FIELD = "lastUpdated";
    private static final String DRIVER_ETAG_FIELD="driver_last_updated";

    private Context context;
    private final SharedPreferences prefs;

    public FireStoreDriverService(Context context) {
        this.context = context; // avoid memory leaks
        prefs = context.getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE);
    }

    public void getAllDrivers(FireStoreQueryDriverCallback callback) throws Exception {
        try {
            if (ApplicationContext.isSkipCache()) {
                // Fetch all duties from Firestore
                getAllDriversFireStore(callback);
            } else {
                db.collection(COLLECTION_NAME)
                        .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);
                                LOCAL_LAST_UPDATED_TIMESTAMP = prefs.getLong(DRIVER_ETAG_FIELD, 0);
                                try{
                                if (cachedDrivers.isEmpty() || LOCAL_LAST_UPDATED_TIMESTAMP == 0
                                        || LOCAL_LAST_UPDATED_TIMESTAMP != latestUpdate.toDate().getTime()) {
                                    // Fetch all duties from Firestore
                                    getAllDriversFireStore(callback);
                                } else {
                                    // Load from local cache
                                    loadDutiesFromCache(callback);
                                }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            callback.onFailure(e);
                        });
            }
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE01:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE01);
            throw new RuntimeException(e);
        }
    }


    public void getDriverByPhone(String phone,FireStoreQueryDriverCallback callback) throws Exception{
        try {
            if (ApplicationContext.isSkipCache()) {
                getDriverByPhoneFireStore(phone, callback);
            } else {
                db.collection(COLLECTION_NAME)
                        .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);
                                LOCAL_LAST_UPDATED_TIMESTAMP = prefs.getLong(DRIVER_ETAG_FIELD, 0);
                                try{
                                if (cachedDrivers.isEmpty() || LOCAL_LAST_UPDATED_TIMESTAMP == 0
                                        || LOCAL_LAST_UPDATED_TIMESTAMP != latestUpdate.toDate().getTime()) {
                                    getDriverByPhoneFireStore(phone, callback);
                                } else {
                                        loadDutiesFromCacheByPhone(callback, phone);
                                }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            callback.onFailure(e);
                        });
                ;
            }
        } catch (Exception e) {
                Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE02:""), Toast.LENGTH_SHORT).show();
                if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE02);
                throw new RuntimeException(e);
            }
    }

    private void getDriverByPhoneFireStore(String phone, FireStoreQueryDriverCallback callback) throws Exception {
        try {
            db.collection(COLLECTION_NAME)
                    .whereEqualTo("phone", phone)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<Driver> results = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                Driver driver = doc.toObject(Driver.class);
                                if (driver != null) {
                                    results.add(driver);
                                    try {
                                        updateCache(driver); // update cache
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            callback.onSuccess(new ArrayList<>(results));
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE03:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE03);
            throw new RuntimeException(e);
        }
    }



    // Get all drivers
    private void getAllDriversFireStore(FireStoreQueryDriverCallback callback) throws Exception {
        try{
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cachedDrivers.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Driver driver = doc.toObject(Driver.class);
                        if (driver != null) {
                            cachedDrivers.add(driver);
                            try {
                                updateCache(driver); // update cache
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    callback.onSuccess(new ArrayList<>(cachedDrivers));
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE04:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE04);
            throw new RuntimeException(e);
        }
    }

    public void saveDriver(Driver driver, FirestoreCallback callback) {
        try {
            db.collection(COLLECTION_NAME)
                    .document(driver.getPhone())
                    .set(driver)
                    .addOnSuccessListener(aVoid -> {
                        try {
                            updateCache(driver);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        callback.onSuccess();
                    })
                    .addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE05:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE05);
            throw new RuntimeException(e);
        }
    }

    public void deleteDriver(String driverPhone, FirestoreCallback callback) throws Exception{
        try {
            db.collection(COLLECTION_NAME)
                    .document(driverPhone)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        try {
                            deleteFromCache(driverPhone);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
        Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE06:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE06);
        throw new RuntimeException(e);
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private void updateCache(Driver updatedDuty) throws Exception {
        try{
        boolean flag=true;
        for (int i = 0; i < cachedDrivers.size(); i++) {
            if (cachedDrivers.get(i).getPhone().equals(updatedDuty.getPhone())) {
                cachedDrivers.set(i, updatedDuty);
                flag=false;
            }
        }
        if(flag) {
            cachedDrivers.add(updatedDuty);
        }
        // saving into phone memory
        if (updatedDuty.getLastUpdated() != null) {
            long recordLastUpdated = updatedDuty.getLastUpdated().toDate().getTime();
            if (recordLastUpdated > LOCAL_LAST_UPDATED_TIMESTAMP) {
                prefs.edit().putLong(DRIVER_ETAG_FIELD, updatedDuty.getLastUpdated().toDate().getTime()).apply();
                LOCAL_LAST_UPDATED_TIMESTAMP = recordLastUpdated;
            }
        }
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE07:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE07);
            throw new RuntimeException(e);
        }
    }

    private void deleteFromCache(String driverPhone) throws Exception{
        try{
        for (int i = 0; i < cachedDrivers.size(); i++) {
            if (cachedDrivers.get(i).getPhone().equals(driverPhone)) {
                cachedDrivers.remove(i);
                LOCAL_LAST_UPDATED_TIMESTAMP=0;
                return;
            }
        }
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE08:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE08);
            throw new RuntimeException(e);
        }
    }

    private void loadDutiesFromCache(FireStoreQueryDriverCallback callback) throws Exception{
        try{
            callback.onSuccess(new ArrayList<>(cachedDrivers));
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE09:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE09);
            throw new RuntimeException(e);
        }
    }

    private void loadDutiesFromCacheByPhone(FireStoreQueryDriverCallback callback, String phone) throws Exception{
        try {
        List<Driver> filteredDrivers = cachedDrivers.stream()
                .filter(driver -> driver.getPhone().equals(phone))
                .collect(Collectors.toList());
        callback.onSuccess(new ArrayList<>(filteredDrivers));
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE10:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE10);
            throw new RuntimeException(e);
        }
    }

}

