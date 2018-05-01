package com.tinmegali.mylocation;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by ohadshiffer
 * on 30/04/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    public static final int FIRST_BOARD = 5000;
    public static final int SECOND_BOARD = 1000;
    public static final int THIRD_BOARD = 500;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);

        if (checkPermission(context)) {
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d(TAG, "onSuccess() called with: location = [" + location + "]");

                    /*
                     * check what is the distance between this location and the beacon location.
                     * more then 10km - should cancel alarm manager.
                     * between 5km - 10km - alarm manager's interval should be 15 minutes.
                     * between 2km - 5km - alarm manager's interval should be 5 minutes.
                     * less then 2km - cancel alarm manager and activate beacon finder service.
                     */

                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    final float lat = sharedPref.getFloat(MainActivity.KEY_GEOFENCE_LAT, Float.MAX_VALUE);
                    final float lon = sharedPref.getFloat(MainActivity.KEY_GEOFENCE_LON, Float.MAX_VALUE);

                    // check if we saved a geo fence location
                    if (lat != Long.MAX_VALUE && lon != Long.MAX_VALUE) {

                        final Location geoFenceLocation = new Location("GeoFenceLocation");
                        geoFenceLocation.setLatitude(lat);
                        geoFenceLocation.setLongitude(lon);

                        float distance = geoFenceLocation.distanceTo(location);

                        Log.d(TAG, "distance: " + distance);

                        if (distance > FIRST_BOARD) {
                            Log.d(TAG, "distance > FIRST_BOARD (" + FIRST_BOARD + ")");
                            putStateIntoSP(sharedPref, Constants.BeaconSearchState.OUT_OF_RANGE);

                            // TODO: 30/04/2018 cancel alarm manager.
                            // UPDATE: its being canceled by the service once we exit the geofence area.
                            //cancelAlarmManager(context);
                        } else if (distance > SECOND_BOARD) {
                            Log.d(TAG, "distance > SECOND_BOARD (" + SECOND_BOARD + ")");
                            putStateIntoSP(sharedPref, Constants.BeaconSearchState.FIRST_RADIUS);

                            // TODO: 30/04/2018 alarm manager's interval should be 15 minutes.

                        } else if (distance > THIRD_BOARD) {
                            Log.d(TAG, "distance > THIRD_BOARD (" + THIRD_BOARD + ")");
                            putStateIntoSP(sharedPref, Constants.BeaconSearchState.SECOND_RADIUS);

                            // TODO: 30/04/2018 alarm manager's interval should be 5 minutes.

                        } else {
                            Log.d(TAG, "distance is less then (" + THIRD_BOARD + ")!!!!!");
                            putStateIntoSP(sharedPref, Constants.BeaconSearchState.THIRD_RADIUS);

                            // TODO: 30/04/2018 cancel alarm manager and activate beacon finder service.

                        }
                    }
                }
            });
        } else {
            Log.e(TAG, "onReceive: no permission ACCESS_FINE_LOCATION!");
        }

    }

    private void cancelAlarmManager(Context context) {
        final Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent alarmManagerPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager!= null) {
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

}
