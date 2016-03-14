package io.goshin.bukadarkness.database;

public class ImageReferrerMapDatabase extends KVMapDatabase {
    private static ImageReferrerMapDatabase instance = null;

    private ImageReferrerMapDatabase() {
        super("image_referrer");
    }

    public static ImageReferrerMapDatabase getInstance() {
        if (instance == null && context != null) {
            instance = new ImageReferrerMapDatabase();
        }
        return instance;
    }
}
