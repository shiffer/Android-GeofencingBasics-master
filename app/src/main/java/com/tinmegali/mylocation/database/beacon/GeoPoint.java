package com.tinmegali.mylocation.database.beacon;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.tinmegali.mylocation.Constants;
import com.tinmegali.mylocation.Utils;

/**
 * Created by ohadshiffer
 * on 07/05/2018.
 */
@Keep
@Entity(tableName = GeoPoint.TABLE_NAME)
public class GeoPoint {

    static final String TABLE_NAME = "geo_points";

    public GeoPoint(long time, String geoPointType, double lat, double lon) {
        this(Utils.getDate(time), geoPointType, lat, lon);
    }

    public GeoPoint(@NonNull String time, String geoPointType, double lat, double lon) {
        this.time = time;
        this.geoPointType = geoPointType;
        this.lat = lat;
        this.lon = lon;
    }

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "geo_point_type")
    @Constants.GeoPointType
    private String geoPointType;

    @ColumnInfo(name = "lat")
    private double lat;

    @ColumnInfo(name = "lon")
    private double lon;

    @ColumnInfo(name = "distance")
    private float distance;

    @ColumnInfo(name = "uniqueId")
    private String uniqueId;

    @ColumnInfo(name = "bluetoothAddress")
    private String bluetoothAddress;

    @ColumnInfo(name = "id1")
    private String id1;

    @ColumnInfo(name = "id2")
    private String id2;

    @ColumnInfo(name = "id3")
    private String id3;

    public String getTime() {
        return time;
    }

    public void setTime(@NonNull String time) {
        this.time = time;
    }

    public void setTime(long time) {
        this.time = Utils.getDate(time);
    }

    public String getGeoPointType() {
        return geoPointType;
    }

    public void setGeoPointType(String geoPointType) {
        this.geoPointType = geoPointType;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getId3() {
        return id3;
    }

    public void setId3(String id3) {
        this.id3 = id3;
    }

}
