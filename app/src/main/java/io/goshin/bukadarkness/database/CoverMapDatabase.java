package io.goshin.bukadarkness.database;

public class CoverMapDatabase extends KVMapDatabase {
    private static CoverMapDatabase instance = null;

    private CoverMapDatabase() {
        super("cover");
    }

    public static CoverMapDatabase getInstance() {
        if (instance == null && context != null) {
            instance = new CoverMapDatabase();
        }
        return instance;
    }
}
