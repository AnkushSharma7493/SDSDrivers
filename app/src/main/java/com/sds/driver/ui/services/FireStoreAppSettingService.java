package com.sds.driver.ui.services;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sds.driver.ui.callback.FireStoreQueryAppSettingsCallback;
import com.sds.driver.ui.callback.FirestoreCallback;
import com.sds.driver.ui.models.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class FireStoreAppSettingService {


    private final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "configuration";

    private Context context;
    public FireStoreAppSettingService(Context context) {
        this.context = context; // avoid memory leaks
    }

    public void getAppSettings(FireStoreQueryAppSettingsCallback callback) throws Exception {
        try {
            db.collection(COLLECTION_NAME)
                    .whereEqualTo("type", "APP_SETTINGS")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<AppSettings> results = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                AppSettings appSettings = doc.toObject(AppSettings.class);
                                if (appSettings != null) {
                                    results.add(appSettings);
                                }
                            }
                            callback.onSuccess(new ArrayList<>(results));
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e);
                    });
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :", Toast.LENGTH_SHORT).show();
        }
    }



    public void saveConfiguration(AppSettings appSettings, FirestoreCallback callback) {
        try {
            db.collection(COLLECTION_NAME)
                    .document(appSettings.getVersion())
                    .set(appSettings)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess();
                    })
                    .addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            Toast.makeText(this.context, "Exception occurred :", Toast.LENGTH_SHORT).show();
        }
    }

}

