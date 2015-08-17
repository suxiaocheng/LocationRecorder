package com.ctrl.supera.locationrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by suxiaocheng on 7/9/15.
 */
public class GPSService extends Service implements LocationListener {
    private static final String TAG = "GPSService";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    /* GPS data struct */
    private String best;
    private LocationManager mgr;
    public Location locationInfo;
    public static boolean needUpdate = false;

    /* Satellite info */
    public static boolean needUpdateSatellite = false;

    /* Satellite Record function */
    public static boolean needRecordLocation = false;

    /* Database use for record location */
    private static DBManager gpsDBManager = null;

    /* GPS Info string */
    private String LocationStatus[] = {"Location out of services",
            "Location unavailable", "location available"};

    /* IBinder used for service and activity communication */
    private final IBinder mBinder = new LocalBinder();

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            while (true) {
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        /* first get the location manager */
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        Log.d(TAG, "Best provider is: " + best);
        Log.d(TAG, "Locations (starting with last known):");

        Location location = mgr.getLastKnownLocation(best);

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mgr.requestLocationUpdates(best, 1000, 1, this);

        mgr.addGpsStatusListener(statusListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return mBinder;
    }

    @Override
    public void onDestroy() {
        // Stop updates to save power while app paused
        mgr.removeUpdates(this);

        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    public void onLocationChanged(Location location) {
        synchronized (this) {
            locationInfo = location;
            needUpdate = true;

            if (needRecordLocation) {
                /* First check if the database is exist or not */
                if (gpsDBManager == null) {
                    gpsDBManager = new DBManager(this);

                    /* Add Header Item to the DataBase */
                    gpsDBManager.add("");
                }
            } else {
                if (gpsDBManager != null) {
                    gpsDBManager.closeDB();
                    gpsDBManager = null;
                }
            }
        }
    }

    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }

    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }

    public void onStatusChanged(String provider, int status,
                                Bundle extras) {
        Log.d(TAG, "Provider status changed: " + provider +
                ", Status: " + LocationStatus[status] + ", extras=" + extras);
    }


    /**
     * 自定义的Binder类，这个是一个内部类，所以可以知道其外围类的对象，通过这个类，让Activity知道其Service的对象
     */
    public class LocalBinder extends Binder {
        GPSService getService() {
            // 返回Activity所关联的Service对象，这样在Activity里，就可调用Service里的一些公用方法和公用属性
            return GPSService.this;
        }
    }

    /**
     * 卫星状态监听器
     */
    private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号
    public String satelliteInfo;

    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            GpsStatus status = locationManager.getGpsStatus(null); //取当前状态
            satelliteInfo = updateGpsStatus(event, status);

            needUpdateSatellite = true;
        }
    };

    private String updateGpsStatus(int event, GpsStatus status) {
        StringBuilder sb2 = new StringBuilder("");
        if (status == null) {
            sb2.append("搜索到卫星个数：" + 0);
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
            }
            sb2.append("搜索到卫星个数：" + numSatelliteList.size());
        }

        return sb2.toString();
    }
}

