package com.tinmegali.mylocation.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import com.tinmegali.mylocation.database.beacon.GeoPoint;
import com.tinmegali.mylocation.database.beacon.GeoPointDao;

/**
 * Created by roberto on 28/01/2018.
 * Have fun!
 */

@Database(entities = {GeoPoint.class},
        version = 1,
        exportSchema = false)
public abstract class InfiDatabase extends RoomDatabase {

    public static final String DB_NAME = "infi_db";

    public abstract GeoPointDao getGeoPointDao();

    private static InfiDatabase sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context,
                    InfiDatabase.class, DB_NAME)
                    .build();
        }
    }

    public static InfiDatabase get() {
        return sInstance;
    }

    public static void executeAsync(DBTask task) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(task::execute);
    }

    public interface DBTask {
        void execute();
    }
}
