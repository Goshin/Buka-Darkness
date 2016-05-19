package io.goshin.bukadarkness.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChapterUpdateTimeDatabase extends DatabaseBase {
    public static final String TABLE_NAME = "chapter_update";
    private static ChapterUpdateTimeDatabase instance = null;

    private ChapterUpdateTimeDatabase() {
        super(TABLE_NAME);
    }

    public static ChapterUpdateTimeDatabase getInstance() {
        if (instance == null && context != null) {
            instance = new ChapterUpdateTimeDatabase();
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + " (" +
                " mid integer," +
                " cid integer," +
                " update_time text," +
                " primary key (mid, cid)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getUpdateTime(String mid, String cid, String defaultTime) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "mid=? and cid=?", new String[]{mid, cid}, null, null, null);
        String result = defaultTime;
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("update_time"));
        } else {
            db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("mid", mid);
            contentValues.put("cid", cid);
            contentValues.put("update_time", defaultTime);
            db.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        return result;
    }
}
