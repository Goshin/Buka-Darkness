package io.goshin.bukadarkness.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "buka_darkness.db";
    private static final int DB_VERSION = 1;

    public DatabaseBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
