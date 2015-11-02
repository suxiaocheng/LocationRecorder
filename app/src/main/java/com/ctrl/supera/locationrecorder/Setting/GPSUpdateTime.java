package com.ctrl.supera.locationrecorder.Setting;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.ctrl.supera.locationrecorder.R;

/**
 * Created by Administrator on 2015/11/2.
 */
public class GPSUpdateTime extends DialogPreference {
    private int mNewValue;
    public GPSUpdateTime(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_gps_update_time);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            persistInt(mNewValue);
        }
    }

}
