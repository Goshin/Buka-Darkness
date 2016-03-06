package io.goshin.bukadarkness.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "buka_darkness";
    private static final int DB_VERSION = 1;

    public DatabaseBase(Context context, String dbName) {
        super(context, DB_NAME + (dbName.equals("") ? "" : "_" + dbName) + ".db", null, DB_VERSION);
    }
}
