package com.tinmegali.mylocation;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tinmegali.mylocation.database.InfiDatabase;
import com.tinmegali.mylocation.database.beacon.GeoPoint;

import java.util.Calendar;

/**
 * Created by ohadshiffer
 * on 30/04/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);

        if (checkPermission(context)) {

            client.getLastLocation().addOnSuccessListener(location -> {
                Log.d(TAG, "onSuccess() called with: location = [" + location + "]");

                /*
                 * check what is the distance between this location and the beacon location.
                 * more then GEOFENCE_RADIUS - should cancel alarm manager.
                 * between GEOFENCE_RADIUS - SECOND_BOUND - alarm manager's interval should be 15 minutes.
                 * between SECOND_BOUND - THIRD_BOUND - alarm manager's interval should be 5 minutes.
                 * less then THIRD_BOUND - cancel alarm manager and activate beacon finder service.
                 */

                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

                // get geo fence location as it saved in SP.
                final Location geoFenceLocation = Utils.getGeoFenceLocation(context);

                if (geoFenceLocation != null) {

                    final float distance = geoFenceLocation.distanceTo(location);
                    Log.d(TAG, "distance: " + distance);

                    @Constants.GeoPointType String type;

                    /*
                     * cancel previous alarm manager in order to set a new one or start {@link MyBeaconService} instead.
                     */
                    cancelAlarmManager(context);

                    /*
                     * the distance is more then {@link Constants.SECOND_BOUND} (right now its 100 meters).
                     * this is the first radius (the farthest radius).
                     * alarm every 15 minutes to recheck location.
                     */
                    if (distance > Constants.SECOND_BOUND) {
                        type = Constants.GeoPointType.FIRST_RADIUS;
                        putStateIntoSP(sharedPref, Constants.BeaconSearchState.FIRST_RADIUS);
//                        setAlarm(context, Constants.INTERVAL_MINUTE);
                        setAlarm(context, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                    }

                    /*
                     * the distance is between {@link Constants.THIRD_BOUND} to {@link Constants.SECOND_BOUND} (right now its 50-100 meters).
                     * this is the second radius (the middle radius).
                     * alarm every 5 minutes to recheck location.
                     */
                    else if (distance > Constants.THIRD_BOUND) {
                        type = Constants.GeoPointType.SECOND_RADIUS;
                        putStateIntoSP(sharedPref, Constants.BeaconSearchState.SECOND_RADIUS);
//                        setAlarm(context, Constants.INTERVAL_MINUTE);
                        setAlarm(context, Constants.INTERVAL_FIVE_MINUTES);
                    }

                    /*
                     * the distance is between the beacon location to {@link Constants.THIRD_BOUND} (right now its 0-50 meters).
                     * this is the first radius (the closest radius).
                     * start MyBeaconService in order to locate beacons.
                     */
                    else {
                        type = Constants.GeoPointType.THIRD_RADIUS;
                        putStateIntoSP(sharedPref, Constants.BeaconSearchState.THIRD_RADIUS);
                        context.startService(new Intent(context, MyBeaconService.class));

//                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
//                            //final Intent mainActivityIntent = new Intent(context, MainActivity.class);
//
//                            PendingIntent mainActivityPendingIntent = PendingIntent.getService(
//                                    context,
//                                    0,
//                                    intent,
//                                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//                            final MyBeaconService service = new MyBeaconService();
//                            service.startForeground(10, createNotification(context, "MyBeaconService is working", mainActivityPendingIntent));
////                            context.startForegroundService(new Intent(context, MyBeaconService.class));
//                        } else {
//                            context.startService(new Intent(context, MyBeaconService.class));
//                        }

                    }

//                    String msg = "distance from beacon = " + distance;
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                    // insert the current location to DB.
                    InfiDatabase.executeAsync(() -> {
                        final GeoPoint point = new GeoPoint(Calendar.getInstance().getTimeInMillis(), type, location.getLatitude(), location.getLongitude());
                        point.setDistance(distance);
                        InfiDatabase.get().getGeoPointDao().insert(point);
                    });
//                }
                }
            });

        } else {
            Log.e(TAG, "onReceive: no permission ACCESS_FINE_LOCATION!");
        }

    }

    private void setAlarm(Context context, long intervalMillis) {
        final long timeInMillis = Calendar.getInstance().getTimeInMillis();
        final Intent intent = new Intent(context, AlarmReceiver.class);

        final PendingIntent alarmManagerPendingIntent = PendingIntent.getBroadcast(context,
                Constants.ALARM_MANAGER_PENDING_INTENT_REQUEST_CODE, intent, 0);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis + intervalMillis, intervalMillis, alarmManagerPendingIntent);
        }
    }

    private void cancelAlarmManager(Context context) {
        final Intent intent = new Intent(context, AlarmReceiver.class);

        final PendingIntent alarmManagerPendingIntent = PendingIntent.getBroadcast(context,
                Constants.ALARM_MANAGER_PENDING_INTENT_REQUEST_CODE, intent, 0);

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(alarmManagerPendingIntent);
        }
    }

    private void putStateIntoSP(SharedPreferences sharedPref, @Constants.BeaconSearchState int state) {
        sharedPref.edit()
                .putInt(Constants.BEACON_SEARCH_STATE, state)
                .apply();
    }

    // Check for permission to access Location
    private boolean checkPermission(Context context) {
        Log.d(TAG, "checkPermission()");

        // Ask for permission if it wasn't granted yet
        final int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    // Create notification
    private Notification createNotification(Context context, String msg, PendingIntent notificationPendingIntent) {
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, TAG);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_action_location)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        return notificationBuilder.build();
    }

}
