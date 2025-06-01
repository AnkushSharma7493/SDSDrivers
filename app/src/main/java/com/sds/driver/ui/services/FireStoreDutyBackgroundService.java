package com.sds.driver.ui.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.sds.driver.R;
import com.sds.driver.ui.enums.DutyStatus;
import com.sds.driver.ui.models.Duty;

import java.util.ArrayList;
import java.util.List;


public class FireStoreDutyBackgroundService extends Service {

    public interface DutyUpdateListener {
        void onDutyListUpdate(List<Duty> dutyList);
        void onError(String message);
    }

    private final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "duties";
    private static final String CHANNEL_ID = "driver_duty_monitor_channel";
    private ListenerRegistration listenerRegistration;
    private final List<DutyUpdateListener> listeners = new ArrayList<>();

    private String driverPhoneNumber;

    private List<Duty> dutyList=new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Always show a placeholder foreground notification to avoid crash
        startForeground(1, createServiceNotification("SDS duty monitoring..."));

        // Load the driver ID from SharedPreferences
        driverPhoneNumber = getSharedPreferences(ApplicationContext.DRIVER_CACHE, Context.MODE_PRIVATE).getString(ApplicationContext.DRIVER_PHONE_CACHE, null);

        if (driverPhoneNumber != null) {
            Notification updatedNotification = createServiceNotification("SDS Checking Duty Updates");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(1, updatedNotification);
            }
        }
    }

    private void showDutyNotification(String title, String message) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logonotification)
                .setContentTitle(title)
                .setContentText(message) // update notification message
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), notification);
        }
    }

    private Notification createServiceNotification(String text) {
        createNotificationChannel();
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sharma Driver Services")
                .setContentText(text)
                .setSmallIcon(R.drawable.logonotification)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Sharma Driver Services ", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public FireStoreDutyBackgroundService getService() {
            return FireStoreDutyBackgroundService.this;
        }
    }


    //*****************************************************************************************************************/
    //********************************************** FIRESTORE ********************************************************/
    //*****************************************************************************************************************/

    public void startDutyListener() {
        if (listenerRegistration != null) return;  // already listening

        listenerRegistration = db.collection(COLLECTION_NAME)
                .whereEqualTo("driverPhone", driverPhoneNumber)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        notifyErrorToListeners("Firestore error: " + e.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    dutyList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        dutyList.add(doc.toObject(Duty.class));
                    }
                    notifyDutyListUpdate(dutyList);

                    dutyList.stream().forEach(duty->{
                        //Send Notification
                        if (DutyStatus.ASSIGNED.equals(duty.getDutyStatus())) {
                            // Driver Notifications - Duty Assigned
                            showDutyNotification("Duty Update", "NEW DUTY ASSIGNED TO YOU \uD83D\uDE97");
                        }
                    });
                });
    }

    private void notifyErrorToListeners(String message) {
        for (DutyUpdateListener listener : listeners) {
            listener.onError(message);
        }
    }


    private void notifyDutyListUpdate(List<Duty> dutyList) {
        for (DutyUpdateListener listener : listeners) {
            listener.onDutyListUpdate(dutyList);
        }
    }


    public void addDutyUpdateListener(DutyUpdateListener listener) {
        listeners.add(listener);
        if(dutyList.size()>0) {
            listener.onDutyListUpdate(new ArrayList<>(dutyList)); // Immediately send current data
        }
    }

    public void removeDutyUpdateListener(DutyUpdateListener listener) {
        listeners.remove(listener);
    }

    public List<Duty> getDutyList(){
        return dutyList;
    }

}

