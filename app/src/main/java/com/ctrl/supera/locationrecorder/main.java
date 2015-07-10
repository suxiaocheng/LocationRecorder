package com.ctrl.supera.locationrecorder;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class main extends ActionBarActivity {

    static final String TAG = "LocationMainActivity";
    private TextView output;

    private String lastLocationInfo;

    private Music musicPlayCtrl;

    private int locationState;

    /* LocationState value */
    static final private int locationStateOutOfServices = 0x0;
    static final private int locationStateUnavailable = 0x1;
    static final private int locationStateAvailable = 0x2;

    /* Screen wakeup lock */
    private PowerManager.WakeLock mWakeLock = null;

    /* Service's connect information */
    private GPSService mService;
    private boolean mBound = false;

    /**/
    private Intent gpsIntent;

    /**/
    UpdateGPSStatusTask updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isOPen(this) == false) {
            //openGPS(this);
            Log.d(TAG, "Gps not opening");
        }
        output = (TextView) findViewById(R.id.Location);

        gpsIntent = new Intent(this, GPSService.class);
        startService(gpsIntent);

        GetScreenOnLock();

        musicPlayCtrl = new Music();
    }

    private void dumpLocation(Location location) {
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

        bindService(gpsIntent, mConnection, Context.BIND_AUTO_CREATE);

        updateTask = new UpdateGPSStatusTask();
        updateTask.execute();

        GetScreenOnLock();
    }

    @Override
    protected void onPause() {
        musicPlayCtrl.play(this, R.raw.quit);
        super.onPause();

        updateTask.cancel(true);

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        ReleaseScreenOnLock();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(gpsIntent);
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


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        //boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean network = false;
        if (gps || network) {
            return true;
        }

        return false;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /** 定交ServiceConnection，用于绑定Service的*/
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // 已经绑定了LocalService，强转IBinder对象，调用方法得到LocalService对象
            GPSService.LocalBinder binder = (GPSService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /* AsyncTask to update the screen information */
    private class UpdateGPSStatusTask extends AsyncTask<String, Location, Void> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected Void doInBackground(String... urls) {
            while(true) {
                if (mService.needUpdate == true) {
                    mService.needUpdate = false;
                    publishProgress(mService.locationInfo);
                }
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isCancelled()){
                    break;
                }
            }

            return null;
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Void result) {
        }

        protected void onProgressUpdate(Location... progress) {
            Location value = progress[0];
            dumpLocation(value);
        }
    }
}
