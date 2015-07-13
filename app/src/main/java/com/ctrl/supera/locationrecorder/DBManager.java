package com.ctrl.supera.locationrecorder;

/**
 * Created by suxiaocheng on 7/13/15.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
    static final String TAG = "DBManager";
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DatabaseHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add table title
     *
     * @param name
     */
    public void add(String name) {
        db.beginTransaction();    //开始事务
        try {
            String titleName;
            Calendar c = Calendar.getInstance();
            titleName = name + Long.toString(c.getTimeInMillis());
            db.execSQL("INSERT INTO " + DatabaseHelper.DB_TITLE_NAME +
                            " VALUES (NULL, ?, ?)",
                    new Object[]{titleName, c.getTimeInMillis()});
            db.setTransactionSuccessful();    //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * delete old person
     *
     * @param name
     */
    public void deleteOldItemHeader(String name[]) {
        int item_delete;
        item_delete = db.delete(DatabaseHelper.DB_TITLE_NAME,
                "name == ?", name);
        Log.d(TAG, item_delete +" has been delete!");
    }

    /**
     * query all persons, return list
     *
     * @return List<Person>
     */
    public List<String> query() {
        ArrayList<String> string_list = new ArrayList<String>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            String name = new String();
            name = c.getString(c.getColumnIndex("name"));
            string_list.add(name);
        }
        c.close();
        return string_list;
    }

    /**
     * query all item header, return cursor
     *
     * @return Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM " +
                DatabaseHelper.DB_TITLE_NAME, null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}
