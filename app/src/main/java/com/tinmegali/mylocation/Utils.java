package com.tinmegali.mylocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by ohadshiffer
 * on 07/05/2018.
 */
public class Utils {

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy hh:mm:ss a", cal).toString();
    }

    public static Location getGeoFenceLocation(Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final String latStr = sharedPref.getString(MainActivity.KEY_GEOFENCE_LAT, null);
        final String lonStr = sharedPref.getString(MainActivity.KEY_GEOFENCE_LON, null);

        double lat = latStr != null ? Double.valueOf(latStr) : 0;
        double lon = lonStr != null ? Double.valueOf(lonStr) : 0;

        // check if we saved a geo fence location
        if (lat != 0 && lon != 0) {
            final Location geoFenceLocation = new Location("GeoFenceLocation");
            geoFenceLocation.setLatitude(lat);
            geoFenceLocation.setLongitude(lon);

            return geoFenceLocation;
        }

        return null;
    }

    public static float getDistanceFromGeoFence(Context context, Location location) {
        final Location geoFenceLocation = getGeoFenceLocation(context);
        return geoFenceLocation != null ?
                geoFenceLocation.distanceTo(location) :
                Float.MIN_VALUE;
    }

}
