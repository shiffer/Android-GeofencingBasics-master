package com.tinmegali.mylocation;

import android.support.annotation.IntDef;

/**
 * Created by ohadshiffer
 * on 30/04/2018.
 */

public class Constants {

    public static final String BEACON_SEARCH_STATE = "beacon_search_state";

    @IntDef({BeaconSearchState.OUT_OF_RANGE, BeaconSearchState.FIRST_RADIUS, BeaconSearchState.SECOND_RADIUS,
            BeaconSearchState.THIRD_RADIUS})
    public @interface BeaconSearchState {
        int OUT_OF_RANGE = 0;
        int FIRST_RADIUS = 1;
        int SECOND_RADIUS = 2;
        int THIRD_RADIUS = 3;
    }


}
