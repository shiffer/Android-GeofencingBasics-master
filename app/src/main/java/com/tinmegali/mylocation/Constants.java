package com.tinmegali.mylocation;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

/**
 * Created by ohadshiffer
 * on 30/04/2018.
 */

public class Constants {

    public static final String BEACON_SEARCH_STATE = "beacon_search_state";
    public static final int ALARM_MANAGER_PENDING_INTENT_REQUEST_CODE = 100;

    public static final float GEOFENCE_RADIUS = 2500.0f; // in meters
    public static final int SECOND_BOUND = 1000;
    public static final int THIRD_BOUND = 500;
    public static final int INTERVAL_FIVE_MINUTES = 60 * 1000 * 5;
    public static final int INTERVAL_MINUTE = 60 * 1000;


    @IntDef({BeaconSearchState.OUT_OF_RANGE, BeaconSearchState.FIRST_RADIUS, BeaconSearchState.SECOND_RADIUS,
            BeaconSearchState.THIRD_RADIUS})
    public @interface BeaconSearchState {
        int OUT_OF_RANGE = 0;
        int FIRST_RADIUS = 1;
        int SECOND_RADIUS = 2;
        int THIRD_RADIUS = 3;
    }

    @StringDef({GeoPointType.FIRST_RADIUS, GeoPointType.SECOND_RADIUS, GeoPointType.THIRD_RADIUS,
            GeoPointType.NEAR_BEACON, GeoPointType.OTHER})
    public @interface GeoPointType {
        String FIRST_RADIUS = "FIRST_RADIUS";
        String SECOND_RADIUS = "SECOND_RADIUS";
        String THIRD_RADIUS = "THIRD_RADIUS";
        String NEAR_BEACON = "NEAR_BEACON";
        String OTHER = "OTHER";
    }
}
