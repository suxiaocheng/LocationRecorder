package com.ctrl.supera.locationrecorder;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by suxiaocheng on 7/11/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "gpsData.db"; //数据库名称
    public static final String DB_TITLE_NAME = "location"; //数据库名称
    private static final int version = 1; //数据库版本

    public static final String DB_TITLE_HEADER_ID = "_id";
    public static final String DB_TITLE_HEADER_NAME = "name";
    public static final String DB_TITLE_HEADER_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + DB_TITLE_NAME +
                "("+DB_TITLE_HEADER_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB_TITLE_HEADER_NAME + " varchar(32) not null," +
                DB_TITLE_HEADER_TIME + " REAL not null );";
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}
