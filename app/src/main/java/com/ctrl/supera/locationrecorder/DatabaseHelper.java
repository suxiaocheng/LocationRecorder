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

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exist " + DB_TITLE_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, +" +
                "name varchar(32) not null," +
                "time REAL not null );";
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

    public void onInsertTitleItem(SQLiteDatabase db, String name) {
        String titleName;
        Calendar c = Calendar.getInstance();
        titleName = name + Long.toString(c.getTimeInMillis());
        try {
            db.execSQL("INSERT INTO " + DB_TITLE_NAME +
                            " VALUES (NULL, ?, ?)",
                    new Object[]{titleName, c.getTimeInMillis()});
        } catch (SQLException e) {
            Log.d(TAG, e.toString());
        }
    }
}
