package io.goshin.bukadarkness.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SourceSettingsDatabase extends DatabaseBase {

    public static final String TABLE_NAME = "source";

    public SourceSettingsDatabase(Context context) {
        super(context, "");
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
                " filename text primary key," +
                " enabled integer," +
                " search_enabled integer" +
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

    public SourceSettingsDatabase add(String filename) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("enabled", 1);
        contentValues.put("search_enabled", 1);
        db.insert(TABLE_NAME, null, contentValues);
        return this;
    }

    public boolean isEnabled(String filename) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "filename=?", new String[]{filename}, null, null, null);
        boolean result = false;
        if (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndex("enabled")) == 1) {
                result = true;
            }
        }
        cursor.close();
        return result;
    }

    public void delete(String filename) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "filename=?", new String[]{filename});
    }
}
