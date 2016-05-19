package io.goshin.bukadarkness.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    public static ByteArrayInputStream indexEncode(String clip, String resourceBaseList, int mangaID, int clipID) throws Throwable {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write("VER1".getBytes("US-ASCII"));

        ByteArrayOutputStream clipOutputStream = new ByteArrayOutputStream();
        //noinspection SpellCheckingInspection
        clipOutputStream.write("AKUB".getBytes("US-ASCII"));
        ZipOutputStream zipOutputStream = new ZipOutputStream(clipOutputStream);
        zipOutputStream.putNextEntry(new ZipEntry("index.dat"));
        zipOutputStream.write(clip.getBytes());
        zipOutputStream.closeEntry();
        zipOutputStream.close();

        byte[] clipBytes = clipOutputStream.toByteArray();
        byte[] key = generateKey(mangaID, clipID);
        xor(clipBytes, key);

        result.write(String.format("%08x", clipBytes.length).getBytes("US-ASCII"));
        result.write(clipBytes);

        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
        gzipOutputStream.write(resourceBaseList.getBytes());
        gzipOutputStream.close();

        return new ByteArrayInputStream(result.toByteArray());
    }

    private static void xor(byte[] source, byte[] key) {
        for (int i = 4; i < source.length; i++) {
            source[i] = ((byte) (source[i] ^ key[((i - 4) % 8)]));
        }
    }

    private static byte[] generateKey(int mangaID, int clipID) {
        byte[] key = new byte[8];
        key[0] = ((byte) clipID);
        key[1] = ((byte) (clipID >> 8));
        key[2] = ((byte) (clipID >> 16));
        key[3] = ((byte) (clipID >> 24));
        key[4] = ((byte) mangaID);
        key[5] = ((byte) (mangaID >> 8));
        key[6] = ((byte) (mangaID >> 16));
        key[7] = ((byte) (mangaID >> 24));
        return key;
    }

    public static String getEncodedUrl(String o) {
        if (o.matches("\\A[\\p{ASCII}&&[^\\s]]*\\z")) {
            return o;
        }

        try {
            URL url = new URL(o);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null).toASCIIString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     * From: http://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
