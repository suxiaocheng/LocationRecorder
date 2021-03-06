package com.ctrl.supera.locationrecorder;

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
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ctrl.supera.locationrecorder.Setting.Prefs;
import com.ctrl.supera.locationrecorder.debug.FileLog;

import java.util.Calendar;


public class main extends ActionBarActivity implements gpsHeaderListFragment.OnFragmentInteractionListener, gpsItemListFragment.OnFragmentInteractionListener {

    static final String TAG = "LocationMainActivity";

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
    public GPSService mService;
    public boolean mBound = false;

    /**/
    private Intent gpsIntent;

    /**/
    UpdateGPSStatusTask updateTask;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * Restart services task
     */
    private RestartServicesTask mRestartServicesTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isOPen(this) == false) {
            openGPS(this);
            FileLog.d(TAG, "Gps not opening");
        }

        startService();

        GetScreenOnLock();

        musicPlayCtrl = new Music();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.gpsMainPager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private void startService() {
        gpsIntent = new Intent(this, GPSService.class);
        startService(gpsIntent);
    }

    private void stopService() {
        if (gpsIntent != null) {
            stopService(gpsIntent);
            gpsIntent = null;
        }
    }

    private void dumpLocation(Location location) {
        gpsHeaderListFragment fragment = (gpsHeaderListFragment) ((ScreenSlidePagerAdapter) mPagerAdapter).getItem(0);
        if (fragment == null) {
            return;
        }
        if (location != null) {
            lastLocationInfo = location.toString();
            if (lastLocationInfo != null) {
                fragment.output.setText(lastLocationInfo);
            } else {
                FileLog.d(TAG, "convert location to string fail");
            }
        } else {
            fragment.output.setText(R.string.UnKnowLocationInfo);
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
        super.onPause();

        if (updateTask != null) {
            updateTask.cancel(true);
            updateTask = null;
        }
        if (mBound) {
            unbindService(mConnection);
        }

        ReleaseScreenOnLock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
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
            startActivityForResult(new Intent(this, Prefs.class), 0x0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the request went well (OK) and the request was PICK_CONTACT_REQUEST
        if (requestCode == 0x0) {
            /* Stop the services and then restart */
            mRestartServicesTask = new RestartServicesTask(true);
            mRestartServicesTask.execute();
        }
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
    public final void openGPS(Context context) {
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
    }

    /**
     * 定交ServiceConnection，用于绑定Service的
     */
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
    private class UpdateGPSStatusTask extends AsyncTask<String, Integer, Void> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(String... urls) {
            while (true) {
                if (mService != null) {
                    if (mService.needUpdate == true) {
                        mService.needUpdate = false;
                        publishProgress(0);
                    }
                    if (mService.needUpdateSatellite == true) {
                        mService.needUpdateSatellite = false;
                        publishProgress(1);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled()) {
                    break;
                }
            }

            return null;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Void result) {
        }

        protected void onProgressUpdate(Integer... type) {
            if (type[0] == 0) {
                if ((mService != null) && (mService.currentLocationInfo != null)) {
                    dumpLocation(mService.currentLocationInfo);
                } else {
                    FileLog.d(TAG, "File location info is null");
                }
            } else if (type[0] == 1) {
                gpsHeaderListFragment fragment = (gpsHeaderListFragment) ((ScreenSlidePagerAdapter) mPagerAdapter).getItem(0);
                if (fragment == null) {
                    return;
                }
                if (mService.satelliteInfo != null) {
                    fragment.satelliteInfoTextView.setText(mService.satelliteInfo);
                }
            }
        }
    }

    /* AsyncTask to update the screen information */
    private class RestartServicesTask extends AsyncTask<String, Integer, Void> {
        private static final String TAG = "RestartServicesTask";
        private boolean bNeedRestart;

        public RestartServicesTask() {
            bNeedRestart = false;
        }

        public RestartServicesTask(boolean bVal) {
            bNeedRestart = bVal;
        }

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(String... urls) {
            if (updateTask != null) {
                FileLog.d(TAG, "stop update task");
                updateTask.cancel(true);
                /* wait for the update task to exit */
                while (updateTask.getStatus() != AsyncTask.Status.FINISHED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                FileLog.d(TAG, "stop update task ok!");
                updateTask = null;
            }
            stopService();

            if (isCancelled() == true) {
                FileLog.d(TAG, "cancel by user");
                return null;
            }

            if (bNeedRestart) {
                FileLog.d(TAG, "Restart services");
                startService();
                bindService(gpsIntent, mConnection, Context.BIND_AUTO_CREATE);
                while (mBound != true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isCancelled() == true) {
                        FileLog.d(TAG, "cancel by user");
                        return null;
                    }
                }
                FileLog.d(TAG, "Bind Service ok!");
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            if (bNeedRestart) {
                updateTask = new UpdateGPSStatusTask();
                updateTask.execute();
            }
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private Fragment[] fragments = {gpsHeaderListFragment.newInstance(null, null), gpsItemListFragment.newInstance(null, null)};

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position < fragments.length) {
                return fragments[position];
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }


    /* use for test only */
    /* function use for test database */
    public static void testDatabase(Context c) {
        InsertDataBaseItem insertDataBaseItem = new InsertDataBaseItem(c);
        insertDataBaseItem.execute();
    }

    /* AsyncTask to update the screen information */
    private static class InsertDataBaseItem extends AsyncTask<String, Integer, Void> {
        private DBManager testDb;
        private long timeInMiliSecond;
        private Double mLongitude, mLatitude;
        private Context mContext;

        public InsertDataBaseItem(Context c){
            mContext = c;
        }
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(String... urls) {
            while (true) {
                Calendar calendar = Calendar.getInstance();
                timeInMiliSecond = calendar.getTimeInMillis();
                if(testDb != null){
                    testDb.add(mLongitude, mLatitude, timeInMiliSecond);
                    mLongitude+=5;
                    mLatitude++;
                    if(mLatitude > 100.){
                        break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled()) {
                    break;
                }
            }
            if(testDb != null){
                testDb.closeDB();
                testDb = null;
            }

            return null;
        }

        protected void onPreExecute() {
            testDb = new DBManager(mContext);
            mLongitude = 0.;
            mLatitude = 0.;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Void result) {
        }

        protected void onProgressUpdate(Integer... type) {
        }
    }
}
