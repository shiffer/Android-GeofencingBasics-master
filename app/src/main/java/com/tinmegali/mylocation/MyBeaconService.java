package com.tinmegali.mylocation;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tinmegali.mylocation.database.InfiDatabase;
import com.tinmegali.mylocation.database.beacon.GeoPoint;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;

public class MyBeaconService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier {

    private static final String TAG = "MyBeaconService";

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private BeaconManager mBeaconManager;
    private Region mRegion;


    public MyBeaconService() {
        Log.d(TAG, "MyBeaconService() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        mBeaconManager.bind(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");

        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");

        super.onCreate();

        initBeaconManager();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");

        mBeaconManager.unbind(this);

        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private void initBeaconManager() {
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.setBackgroundMode(true);

        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        mBeaconManager.setBackgroundBetweenScanPeriod(120000L);     // 2 minutes between each scan
//        mBeaconManager.setBackgroundScanPeriod(10000L);           // default is 10000L
//        mBeaconManager.setForegroundBetweenScanPeriod(0L);        // default is 0L
//        mBeaconManager.setForegroundScanPeriod(1100L);            // Default is 1100L

        //mBeaconManager.setMaxTrackingAge(10000);
        //mBeaconManager.setRegionExitPeriod(12000L);
        mBeaconManager.setEnableScheduledScanJobs(true);

        try {
            if (mBeaconManager.isAnyConsumerBound()) {
                mBeaconManager.updateScanPeriods();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "initBeaconManager: ", e);
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect() called");

        mBeaconManager.addMonitorNotifier(this);
        mBeaconManager.addRangeNotifier(this);

        try {

            // on Ohad's phone (S8+)
//            final Identifier myBeaconNamespaceId = Identifier.parse("0x7d2f8868101c29959a00");
//            final Identifier myBeaconInstanceId = Identifier.parse("0x00000000000");

            // Samsung Galaxy S6
            final Identifier myBeaconNamespaceId = Identifier.parse("0x12345678901234567890");
            final Identifier myBeaconInstanceId = Identifier.parse("0x00000000000");

            mRegion = new Region("ohad", myBeaconNamespaceId, myBeaconInstanceId, null);

            mBeaconManager.startRangingBeaconsInRegion(mRegion);
            mBeaconManager.startMonitoringBeaconsInRegion(mRegion);

        } catch (RemoteException e) {
            Log.e(TAG, "onBeaconServiceConnect: ", e);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        Log.d(TAG, "didRangeBeaconsInRegion() called with: collection = [" + collection + "], region = [" + region + "]");

        final String uniqueId = region.getUniqueId();
        final String bluetoothAddress = region.getBluetoothAddress();
        final Identifier id1 = region.getId1();
        final Identifier id2 = region.getId2();
        final Identifier id3 = region.getId3();

        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        if (checkPermission()) {

            client.getLastLocation()
                    .addOnSuccessListener(location -> {

                        final float distanceFromGeoFence = Utils.getDistanceFromGeoFence(this, location);

                        if (distanceFromGeoFence < Constants.GEOFENCE_RADIUS) {

                            final @Constants.GeoPointType String type = Constants.GeoPointType.NEAR_BEACON;
                            final double latitude = location.getLatitude();
                            final double longitude = location.getLongitude();

                            final GeoPoint point = new GeoPoint(Calendar.getInstance().getTimeInMillis(), type, latitude, longitude);
                            point.setDistance(distanceFromGeoFence);
                            point.setUniqueId(uniqueId);
                            point.setBluetoothAddress(bluetoothAddress);

                            if (id1 != null) point.setId1(id1.toString());
                            if (id2 != null) point.setId2(id2.toString());
                            if (id3 != null) point.setId3(id3.toString());

                            InfiDatabase.executeAsync(() -> InfiDatabase.get().getGeoPointDao().insert(point));
                        } else {
                            setAlarmManager();
                            stopSelf();
                        }
                    })
                    .addOnFailureListener(e -> {
                        final @Constants.GeoPointType String type = Constants.GeoPointType.OTHER;
                        final GeoPoint point = new GeoPoint(Calendar.getInstance().getTimeInMillis(), type, 0, 0);
                        point.setUniqueId(uniqueId);
                        point.setBluetoothAddress(bluetoothAddress);
                        point.setId1(id1.toString());
                        point.setId2(id2.toString());
                        point.setId3(id3.toString());

                        InfiDatabase.executeAsync(() -> InfiDatabase.get().getGeoPointDao().insert(point));
                    });
        }

    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(TAG, "I just saw an beacon for the first time!");
    }

    @Override
    public void didExitRegion(Region region) {
        Log.i(TAG, "I no longer see an beacon");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");

        // Ask for permission if it wasn't granted yet
        final int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void setAlarmManager() {
        Log.d(TAG, "initAlarmManager() called");

        final Calendar calendar = Calendar.getInstance();
        final Intent intent = new Intent(this, AlarmReceiver.class);

        final PendingIntent alarmManagerPendingIntent = PendingIntent.getBroadcast(this,
                Constants.ALARM_MANAGER_PENDING_INTENT_REQUEST_CODE, intent, 0);

        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmManagerPendingIntent);
        }
    }

    /**
     * Class for clients to access. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        MyBeaconService getService() {
            return MyBeaconService.this;
        }
    }

}
