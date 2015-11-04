package com.ctrl.supera.locationrecorder;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ctrl.supera.locationrecorder.debug.FileLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by suxiaocheng on 7/11/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "gpsData"; //数据库名称
    private static final String DB_NAME_SUBFIX = ".db";
    public static final String DB_TITLE_NAME = "location"; //数据库名称
    private static final int version = 1; //数据库版本

    public static final String DB_TITLE_HEADER_ID = "_id";
    public static final String DB_LATITUDE_NAME = "latitude";
    public static final String DB_LONGITUDE_NAME = "longitude";
    public static final String DB_TIME_STAMP = "time_stamp";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd_HHmmss");
        String date = sDateFormat.format(new java.util.Date());

        String sql = "create table if not exists " + DB_TITLE_NAME + date + DB_NAME_SUBFIX +
                "(" + DB_TITLE_HEADER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB_LATITUDE_NAME + " REAL not null," +
                DB_LONGITUDE_NAME + " REAL not null," +
                DB_TIME_STAMP + " INTEGER not null );";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            FileLog.d(TAG, e.toString());
        }

        FileLog.d(TAG, "Create database: " + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}
