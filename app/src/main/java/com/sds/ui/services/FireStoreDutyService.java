package com.sds.ui.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.sds.ui.callback.FireStoreQueryDutyCallback;
import com.sds.ui.callback.FirestoreCallback;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class FireStoreDutyService {


    private final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final static List<Duty> cachedDuties = new ArrayList<>();
    private static long LOCAL_LAST_UPDATED_TIMESTAMP=0;

    private static final String COLLECTION_NAME = "duties";
    private static final String ETAG_FIELD = "lastUpdated";

    private Context context;
    private final SharedPreferences prefs;

    public FireStoreDutyService(Context context) {
        this.context = context; // avoid memory leaks
        prefs = context.getSharedPreferences("duty_cache", Context.MODE_PRIVATE);
    }


    public void getAllDuties(FireStoreQueryDutyCallback callback){
        db.collection(COLLECTION_NAME)
                .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);

                        String localETag = prefs.getString("duty_last_updated", null);

                        if (localETag == null || !localETag.equals(latestUpdate.toString())) {
                            // Fetch all duties from Firestore
                            getAllDutiesFireStore(callback);
                        } else {
                            // Load from local cache
                            loadDutiesFromCache(callback);
                        }
                    }
                });
    }

//    public void getDutiesByDriverPhone(String phone, DutyStatus status, FireStoreQueryDutyCallback callback){
//
//        if(ApplicationContext.isSkipCache()){
//            getDutiesByDriverPhoneStatusFireStore(phone,status, callback);
//        } else {
//            db.collection(COLLECTION_NAME)
//                    .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
//                    .limit(1)
//                    .get()
//                    .addOnSuccessListener(snapshot -> {
//                        if (!snapshot.isEmpty()) {
//                            Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);
//                            LOCAL_LAST_UPDATED_TIMESTAMP = prefs.getLong("duty_last_updated", 0);
//
//                            if (cachedDuties.isEmpty() || LOCAL_LAST_UPDATED_TIMESTAMP == 0
//                                    || LOCAL_LAST_UPDATED_TIMESTAMP!=latestUpdate.toDate().getTime()) {
//                                // Fetch all duties from Firestore
//                                getDutiesByDriverPhoneStatusFireStore(phone,status, callback);
//                            } else {
//                                // Load from local cache
//                                loadDutiesFromCacheByDriverPhone(callback,phone);
//                            }
//                        }
//                    });
//        }
//    }

    public void getDutiesByStatus(DutyStatus status, FireStoreQueryDutyCallback callback){

        if(ApplicationContext.isSkipCache()){
            // Fetch all duties from Firestore
            getDutiesByStatusFireStore(status, callback);
        } else {
            db.collection(COLLECTION_NAME)
                    .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);
                            LOCAL_LAST_UPDATED_TIMESTAMP = prefs.getLong("duty_last_updated", 0);

                            if (cachedDuties.isEmpty() || LOCAL_LAST_UPDATED_TIMESTAMP == 0
                                    || LOCAL_LAST_UPDATED_TIMESTAMP!=latestUpdate.toDate().getTime()) {
                                // Fetch all duties from Firestore
                                getDutiesByStatusFireStore(status, callback);
                            } else {
                                // Load from local cache
                                loadDutiesFromCacheByStatus(callback,status.getStatus());
                            }
                        }
                    });
        }
    }

    public void getDutiesByReportingDate(Date date, FireStoreQueryDutyCallback callback){
        if(ApplicationContext.isSkipCache()){
            // Fetch all duties from Firestore
            getDutiesByReportingDateFireStore(date, callback);
        } else {
            db.collection(COLLECTION_NAME)
                    .orderBy(ETAG_FIELD, Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            Timestamp latestUpdate = snapshot.getDocuments().get(0).getTimestamp(ETAG_FIELD);
                            LOCAL_LAST_UPDATED_TIMESTAMP = prefs.getLong("duty_last_updated", 0);

                            if (cachedDuties.isEmpty() || LOCAL_LAST_UPDATED_TIMESTAMP == 0
                                    || LOCAL_LAST_UPDATED_TIMESTAMP != latestUpdate.toDate().getTime()) {
                                getDutiesByReportingDateFireStore(date, callback);
                            } else {
                                loadDutiesFromCacheByDate(callback,date);
                            }
                        }
                    });
        }
    }

    // Get all duties
    private void getAllDutiesFireStore(FireStoreQueryDutyCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cachedDuties.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Duty duty = doc.toObject(Duty.class);
                        if (duty != null) {
                            cachedDuties.add(duty);
                            updateCache(duty); // update cache
                        }
                    }
                    callback.onSuccess(new ArrayList<>(cachedDuties));
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });;
    }

    // Query by status
    private void getDutiesByStatusFireStore(DutyStatus status, FireStoreQueryDutyCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("dutyStatus", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Duty> results = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Duty duty = doc.toObject(Duty.class);
                        if (duty != null) {
                            results.add(duty);
                            updateCache(duty); // update cache
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    public ListenerRegistration getDutiesByDriverPhoneStatusFireStore(String phone, DutyStatus status, FireStoreQueryDutyCallback callback) {
       return db.collection(COLLECTION_NAME)
                .whereEqualTo("driverPhone", phone)
                .whereEqualTo("dutyStatus", status)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Duty> updatedList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Duty duty = doc.toObject(Duty.class);
                            if (duty != null) {
                                updatedList.add(duty);
                                updateCache(duty); // update cache
                            }
                        }
                        callback.onSuccess(updatedList);
                    }
                });
    }

    public ListenerRegistration getDutiesByDriverPhoneFireStore(String phone, FireStoreQueryDutyCallback callback) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("driverPhone", phone)
                .whereNotEqualTo("dutyStatus", DutyStatus.UNASSIGNED)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Duty> updatedList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Duty duty = doc.toObject(Duty.class);
                            if (duty != null) {
                                updatedList.add(duty);
                                updateCache(duty); // update cache
                            }
                        }
                        callback.onSuccess(updatedList);
                    }
                });
    }

    private void getDutiesByReportingDateFireStore(Date date, FireStoreQueryDutyCallback callback) {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(date);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendarStart.getTime();

        // Start of next day (to use < next day for range)
        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextDay = calendarStart.getTime();

        db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("dutyReportingDate", startOfDay)
                .whereLessThan("dutyReportingDate", startOfNextDay)
                //.orderBy("dutyReportingTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Duty> results = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Duty duty = doc.toObject(Duty.class);
                        if (duty != null) {
                            results.add(duty);
                            updateCache(duty); // update cache
                        }
                    }
                    callback.onSuccess(results);
                });
    }

    private void getDutiesByReportingDateAndStatusFireStore(Date date,String status, FireStoreQueryDutyCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("dutyReportingDate", date)
                .whereEqualTo("dutyStatus", status)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Duty> results = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Duty duty = doc.toObject(Duty.class);
                        if (duty != null) {
                            results.add(duty);
                            updateCache(duty); // update cache
                        }
                    }
                    callback.onSuccess(results);
                });
    }

    // Create or update duty
    public void saveDuty(Duty duty, FirestoreCallback callback) {
        //duty.setLastUpdated(FieldValue.serverTimestamp());
        db.collection(COLLECTION_NAME)
                .document(duty.getDutyId())
                .set(duty)
                .addOnSuccessListener(aVoid -> {
                    updateCache(duty);
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }


    @SuppressLint("SuspiciousIndentation")
    private void updateCache(Duty updatedDuty) {
        boolean flag=true;
        for (int i = 0; i < cachedDuties.size(); i++) {
            if (cachedDuties.get(i).getDutyId().equals(updatedDuty.getDutyId())) {
                cachedDuties.set(i, updatedDuty);
                flag=false;
            }
        }
        if(flag) {
            cachedDuties.add(updatedDuty);
        }
        // saving into phone memory
        if (updatedDuty.getLastUpdated() != null) {
            long recordLastUpdated = updatedDuty.getLastUpdated().toDate().getTime();
            if (recordLastUpdated > LOCAL_LAST_UPDATED_TIMESTAMP) {
                prefs.edit().putLong("duty_last_updated", updatedDuty.getLastUpdated().toDate().getTime()).apply();
                LOCAL_LAST_UPDATED_TIMESTAMP = recordLastUpdated;
            }
        }
    }

    private void deleteFromCache(String dutyId) {
        for (int i = 0; i < cachedDuties.size(); i++) {
            if (cachedDuties.get(i).getDutyId().equals(dutyId)) {
                cachedDuties.remove(i);
                LOCAL_LAST_UPDATED_TIMESTAMP=0;
                return;
            }
        }
    }

    private void loadDutiesFromCache(FireStoreQueryDutyCallback callback){
            callback.onSuccess(new ArrayList<>(cachedDuties));
    }

    private void loadDutiesFromCacheByStatus(FireStoreQueryDutyCallback callback, String status){
        List<Duty> filteredDuties = cachedDuties.stream()
                .filter(duty -> duty.getDutyStatus().getStatus().equals(status))
                .collect(Collectors.toList());
        callback.onSuccess(new ArrayList<>(filteredDuties));
    }

    private void loadDutiesFromCacheByDriverPhone(FireStoreQueryDutyCallback callback, String phone){
        List<Duty> filteredDuties = cachedDuties.stream()
                .filter(duty -> duty.getDriverPhone().equals(phone))
                .collect(Collectors.toList());
        callback.onSuccess(new ArrayList<>(filteredDuties));
    }

    private void loadDutiesFromCacheByDate(FireStoreQueryDutyCallback callback, Date selectedDate){
        List<Duty> filteredDuties = cachedDuties.stream()
                .filter(duty -> AppUtility.isSameDay(duty.getDutyReportingDate(), selectedDate))
                .collect(Collectors.toList());

        callback.onSuccess(new ArrayList<>(filteredDuties));
    }


    public void deleteDuty(String dutyId, FirestoreCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(dutyId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteFromCache(dutyId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

}

