package io.goshin.bukadarkness.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class KVMapDatabase extends DatabaseBase {
    private String tableName;
    private SQLiteDatabase writableDatabase;

    public KVMapDatabase(String dbName) {
        super(dbName);
        tableName = dbName;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + tableName + " (" +
                " entry_key text primary key," +
                " entry_value text" +
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

    public void put(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("entry_key", key);
        contentValues.put("entry_value", value);
        writableDatabase.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void commit() {
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
    }

    public String get(String key) {
        Cursor cursor = getReadableDatabase().query(tableName, null, "entry_key=?", new String[]{key}, null, null, null);
        String result = null;
        if (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndex("entry_value"));
        }
        cursor.close();
        return result;
    }
}
