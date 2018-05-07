package com.tinmegali.mylocation.database.beacon;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by roberto on 28/01/2018.
 * Have fun!
 */

@Dao
public interface GeoPointDao {

    @Query("SELECT * FROM geo_points") // ORDER BY " + GeoPoint.Columns.JOB_TIME
    List<GeoPoint> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GeoPoint point);

    @Insert
    long[] insertAll(GeoPoint... points);
}
