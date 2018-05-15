//package com.tinmegali.mylocation;
//
//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.app.TaskStackBuilder;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Binder;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.location.GeofenceStatusCodes;
//import com.google.android.gms.location.GeofencingEvent;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//public class GeofenceTransitionService extends Service {
//
//    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
//
//    public static final int GEOFENCE_NOTIFICATION_ID = 0;
//
//    private AlarmManager mAlarmManager;
//    private PendingIntent mAlarmManagerPendingIntent;
//    private NotificationManager mNotificationManager;
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new LocalBinder();
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        showPersistNotification();
//    }
//
//    @Override
//    public void onDestroy() {
//        stopForeground(true);
//        mNotificationManager.cancel(100);
//        super.onDestroy();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        new Thread(() -> {
//            final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//
//            // Handling errors
//            if (geofencingEvent.hasError()) {
//                final String errorMsg = getErrorString(geofencingEvent.getErrorCode());
//                Log.e(TAG, errorMsg);
//                return;
//            }
//
//            final int geoFenceTransition = geofencingEvent.getGeofenceTransition();
//
//            // Check if the transition type is of interest
//            switch (geoFenceTransition) {
//                case Geofence.GEOFENCE_TRANSITION_ENTER:
//                    handleNotification(geofencingEvent, geoFenceTransition);
//                    initAlarmManager();
//                    break;
//
//                case Geofence.GEOFENCE_TRANSITION_EXIT:
//                    handleNotification(geofencingEvent, geoFenceTransition);
//                    cancelAlarmManager();
//                    break;
//            }
//
//        }).start();
//
//        return START_STICKY;
//    }
//
//    private void handleNotification(GeofencingEvent geofencingEvent, int geoFenceTransition) {
//        // Get the geofence that were triggered
//        final List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
//
//        final String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofences);
//
//        // Send notification details as a String
//        sendNotification(geofenceTransitionDetails);
//    }
//
//    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
//
//        // get the ID of each geofence triggered
//        final ArrayList<String> triggeringGeofencesList = new ArrayList<>();
//
//        for (Geofence geofence : triggeringGeofences) {
//            triggeringGeofencesList.add(geofence.getRequestId());
//        }
//
//        String status = null;
//
//        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            status = "Entering ";
//        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            status = "Exiting ";
//        }
//
//        return status + TextUtils.join(", ", triggeringGeofencesList);
//    }
//
//    private void sendNotification(String msg) {
//        Log.i(TAG, "sendNotification: " + msg);
//
//        // Intent to start the main Activity
//        final Intent notificationIntent = MainActivity.makeNotificationIntent(this, msg);
//
//        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(notificationIntent);
//        final PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Creating and sending Notification
//        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (notificationManager != null) {
//            notificationManager.notify(
//                    GEOFENCE_NOTIFICATION_ID,
//                    createNotification(msg, notificationPendingIntent));
//        }
//
//    }
//
//    // Create notification
//    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
//        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, TAG);
//
//        notificationBuilder
//                .setSmallIcon(R.drawable.ic_action_location)
//                .setColor(Color.RED)
//                .setContentTitle(msg)
//                .setContentText("Geofence Notification!")
//                .setContentIntent(notificationPendingIntent)
//                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
//                .setAutoCancel(true);
//
//        return notificationBuilder.build();
//    }
//
//    private static String getErrorString(int errorCode) {
//        switch (errorCode) {
//            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
//                return "GeoFence not available";
//
//            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
//                return "Too many GeoFences";
//
//            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
//                return "Too many pending intents";
//
//            default:
//                return "Unknown error.";
//        }
//    }
//
//    private void initAlarmManager() {
//        Log.d(TAG, "initAlarmManager() called");
//
//        final Calendar calendar = Calendar.getInstance();
//        final Intent intent = new Intent(this, AlarmReceiver.class);
//
//        mAlarmManagerPendingIntent = PendingIntent.getBroadcast(this,
//                Constants.ALARM_MANAGER_PENDING_INTENT_REQUEST_CODE, intent, 0);
//
//        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_FIFTEEN_MINUTES, mAlarmManagerPendingIntent);
//    }
//
//    private void cancelAlarmManager() {
//        Log.d(TAG, "cancelAlarmManager() called");
//
//        // If the alarm has been set, cancel it.
//        if (mAlarmManager!= null) {
//            mAlarmManager.cancel(mAlarmManagerPendingIntent);
//        }
//    }
//
//    private void showPersistNotification() {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Application")
//                .setSmallIcon(R.drawable.ic_action_location)
//                .setContentText("Searching for beacon")
//                .setContentTitle(getString(R.string.app_name))
//                .setPriority(NotificationCompat.PRIORITY_MIN)
//                .setSound(null);
//
//        Notification notification = mBuilder.build();
//        startForeground(100, notification);
//    }
//
//    /**
//     * Class for clients to access. Because we know this service always
//     * runs in the same process as its clients, we don't need to deal with
//     * IPC.
//     */
//    public class LocalBinder extends Binder {
//        GeofenceTransitionService getService() {
//            return GeofenceTransitionService.this;
//        }
//    }
//
//}
