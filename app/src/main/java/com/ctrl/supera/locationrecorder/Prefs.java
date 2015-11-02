package com.ctrl.supera.locationrecorder;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class Prefs extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private static final String TAG = "Prefs";

    private static final int CHOOSE_FILE = 0xff;
    private static String selectMusicStr;

    private static final String music_ext[] = {"mp3", "wav"};

    private Preference MusicOnPref;
    private ListPreference MusicTypePref;
    private Preference MusicNamePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_prefs);
        addPreferencesFromResource(R.xml.activity_prefs);

		/* Update the default value from database */
        MusicOnPref = findPreference(getResources().getString(R.string.backgroundMusicEnable));
        MusicTypePref = (ListPreference) findPreference(getResources().getString(R.string.backgroundMusicLocation));
        MusicNamePref = findPreference(getResources().getString(R.string.preferenceMusicSelect));

        MusicOnPref.setDefaultValue(getMusicEnableStatus(this));
        MusicTypePref.setDefaultValue(getMusicType(this));
        selectMusicStr = getMusicName(this);
        MusicNamePref.setDefaultValue(selectMusicStr);

        if (checkMusicValid(selectMusicStr, this) == false) {
            if(selectMusicStr != null){
                selectMusicStr = null;
                /* save the file str into share preference */
                MusicNamePref.setDefaultValue(selectMusicStr);
                MusicTypePref.setValueIndex(0);
            }
        }

        OnPreferenceClickListener onPreferenceClick = new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference == MusicNamePref) {
                    Intent i = new Intent();
                    i.setAction(i.ACTION_GET_CONTENT);
                    i.setType("*/*");
                    MusicNamePref.setIntent(i);
                    startActivityForResult(i, CHOOSE_FILE);
                }
                return true;
            }
        };
        MusicNamePref.setOnPreferenceClickListener(onPreferenceClick);
        setUpActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*
         * if (id == R.id.action_settings) { return true; }
		 */
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the current value of the music option
     */
    public static boolean getMusicEnableStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.backgroundMusicEnable), false);
    }

    /**
     * Get the current value of the hints option
     */
    public static String getMusicType(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.backgroundMusicLocation), "0");
    }

    public static String getMusicName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getString(context.getString(R.string.preferenceMusicSelect), "");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpActionBar() {
        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE: {
                Uri uri;
                String ext;
                String prefix;
                int i;
                if (data != null) {
                    uri = data.getData();
                    selectMusicStr = uri.toString();
                    selectMusicStr = Uri.decode(selectMusicStr);
                    ext = selectMusicStr.substring(
                            selectMusicStr.lastIndexOf('.') + 1,
                            selectMusicStr.length());
                    prefix = selectMusicStr.substring(0, 7);
                    for (i = 0; i < music_ext.length; i++) {
                        if (ext.compareToIgnoreCase(music_ext[i]) == 0) {
                            break;
                        }
                    }
                    if (i == music_ext.length) {
                        Toast.makeText(this,
                                "invalid file select\n" + selectMusicStr,
                                Toast.LENGTH_SHORT).show();
                        selectMusicStr = null;
                    } else {
                    /* delete the prefix */
                        if (prefix.compareToIgnoreCase("file://") == 0) {
                            selectMusicStr = selectMusicStr.substring(7,
                                    selectMusicStr.length());
                        }
                    }

                    if (checkMusicValid(selectMusicStr, this) == false) {
                        selectMusicStr = null;
                        MusicTypePref.setValueIndex(0);
                    } else {
                        // MusicNamePref.setDefaultValue(selectMusicStr);
                        MusicTypePref.setValueIndex(1);
                    }

				    /* save the file str into share preference */
                    MusicNamePref.setDefaultValue(selectMusicStr);
                    Log.d(TAG, "select music:" + selectMusicStr);
                }
            }
        }

    }

    private static boolean checkMusicValid(String name, Context context) {
        boolean valid = false;
        File music_file;
        try {
            music_file = new File(name);
        } catch (NullPointerException e) {
            return valid;
        }
        if (music_file == null) {
            return valid;
        }
        if (music_file.isFile() && music_file.canRead() && (music_file.length() > 0)) {
            valid = true;
        }
        if (valid == false) {
            Toast.makeText(context, "file operation error: \n" + name,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "file valid\n" + name, Toast.LENGTH_LONG)
                    .show();
        }
        return valid;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        Prefs.dumpInformation(TAG, this);
    }

    public static boolean dumpInformation(String tag, Context context) {
        Log.d(tag, "Enable:" + getMusicEnableStatus(context));
        Log.d(tag, "Type:" + getMusicType(context));
        Log.d(tag, "Name:" + getMusicName(context));
        return true;
    }
}
