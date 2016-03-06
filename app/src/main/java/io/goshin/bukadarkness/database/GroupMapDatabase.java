package io.goshin.bukadarkness.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GroupMapDatabase extends DatabaseBase {

    public static final String TABLE_NAME = "group_map";
    public static final int PREFIX_START = 987001;

    public GroupMapDatabase(Context context) {
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
                " _id integer primary key autoincrement," +
                " filename text unique," +
                " name text" +
                ");");
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", PREFIX_START);
        contentValues.put("filename", "filename");
        contentValues.put("name", "name");
        db.insert(TABLE_NAME, null, contentValues);
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

    public long getID(String filename, String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "filename=?", new String[]{filename}, null, null, null);
        if (cursor.moveToNext()) {
            long result = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            return result;
        }
        db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("name", name);
        return db.insert(TABLE_NAME, null, contentValues);
    }

    public String getFilename(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "_id=?", new String[]{String.valueOf(id)}, null, null, null);
        String result = "";
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("filename"));
        }
        cursor.close();
        return result;
    }

    public String getName(String filename) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "filename=?", new String[]{filename}, null, null, null);
        String result = "";
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("name"));
        }
        cursor.close();
        return result;
    }
}
