package com.ctrl.supera.locationrecorder;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class main extends ActionBarActivity implements LocationListener {

    static final String TAG = "LocationMainActivity";
    private LocationManager mgr;
    private TextView output;
    private String best;
    private String lastLocationInfo;
    private String LocationStatus[] = {"Location out of services",
            "Location unavailable", "location available"};

    private Music musicPlayCtrl;

    private int locationState;

    /* LocationState value */
    static final private int locationStateOutOfServices = 0x0;
    static final private int locationStateUnavailable = 0x1;
    static final private int locationStateAvailable = 0x2;

    /* Screen wakeup lock */
    private PowerManager.WakeLock mWakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        output = (TextView) findViewById(R.id.Location);
        Log.d(TAG, "Location providers:");
        //dumpProviders();

        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        Log.d(TAG, "Best provider is: " + best);
        Log.d(TAG, "Locations (starting with last known):");

        Location location = mgr.getLastKnownLocation(best);
        dumpLocation(location);

        GetScreenOnLock();

        musicPlayCtrl = new Music();
    }

    private void dumpLocation(Location location) {
        Calendar gc = GregorianCalendar.getInstance();

        if (location != null) {
            lastLocationInfo = location.toString();
            output.setText(lastLocationInfo);
        } else {
            output.setText(R.string.UnKnowLocationInfo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start updates (doc recommends delay >= 60000 ms)
        mgr.requestLocationUpdates(best, 15000, 1, this);
        GetScreenOnLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop updates to save power while app paused
        mgr.removeUpdates(this);
        musicPlayCtrl.stop(this);
        ReleaseScreenOnLock();
    }

    public void onLocationChanged(Location location) {
        dumpLocation(location);
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
        if(status == LocationProvider.AVAILABLE){
            /* Play sound to notify the user */
            musicPlayCtrl.play(this, R.raw.main);
        }else{
            /* Doing something to info the user */
            musicPlayCtrl.stop(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GetScreenOnLock() {
        if (mWakeLock == null) {
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
            this.mWakeLock.acquire();
        }
    }

    private void ReleaseScreenOnLock() {
        if (mWakeLock != null) {
            this.mWakeLock.release();
            mWakeLock = null;
        }
    }
}
