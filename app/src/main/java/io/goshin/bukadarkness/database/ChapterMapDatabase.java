package io.goshin.bukadarkness.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.Pair;

public class ChapterMapDatabase extends DatabaseBase {

    public static final String TABLE_NAME = "chapter_map";
    private SQLiteDatabase writableDatabase;

    public ChapterMapDatabase(Context context) {
        super(context, TABLE_NAME);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + " (" +
                " mid integer," +
                " cid integer," +
                " filename text," +
                " url text," +
                " primary key (mid, cid)" +
                ");");
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void putPrepare() {
        writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
    }

    public void put(long mid, long cid, String filename, String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("mid", mid);
        contentValues.put("cid", cid);
        contentValues.put("filename", filename);
        contentValues.put("url", url);
        writableDatabase.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void commit() {
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
    }

    public Pair<String, String> get(String mid, String cid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "mid=? and cid=?", new String[]{mid, cid}, null, null, null);
        Pair<String, String> result = null;
        if (cursor.moveToNext()) {
            result = new Pair<>(cursor.getString(cursor.getColumnIndex("filename")), cursor.getString(cursor.getColumnIndex("url")));
        }
        cursor.close();
        return result;
    }
}
