package com.sds.driver.ui.services;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sds.driver.ui.callback.FireStoreQueryDriverCallback;
import com.sds.driver.ui.callback.FirestoreCallback;
import com.sds.driver.ui.enums.ErrorCodeEnum;
import com.sds.driver.ui.models.Driver;

import java.util.ArrayList;
import java.util.List;


public class FireStoreDriverService {

    private final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "drivers";
    private Context context;

    public FireStoreDriverService(Context context) {
        this.context = context;
    }

    public void getDriverByPhone(String phone, FireStoreQueryDriverCallback callback) throws Exception {
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


    public void saveDriver(Driver driver, FirestoreCallback callback) {
        try {
            db.collection(COLLECTION_NAME)
                    .document(driver.getPhone())
                    .set(driver)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess();
                    })
                    .addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :"+ (ApplicationContext.isShowError()?ErrorCodeEnum.SDSE05:""), Toast.LENGTH_SHORT).show();
            if(ApplicationContext.isShowError())ApplicationContext.addStacktrace(ErrorCodeEnum.SDSE05);
            throw new RuntimeException(e);
        }
    }


}

