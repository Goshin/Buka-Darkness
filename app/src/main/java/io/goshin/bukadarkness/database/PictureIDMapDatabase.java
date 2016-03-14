package io.goshin.bukadarkness.database;

public class PictureIDMapDatabase extends KVMapDatabase {
    private static PictureIDMapDatabase instance = null;

    private PictureIDMapDatabase() {
        super("pic_map");
    }

    public static PictureIDMapDatabase getInstance() {
        if (instance == null && context != null) {
            instance = new PictureIDMapDatabase();
        }
        return instance;
    }
}
