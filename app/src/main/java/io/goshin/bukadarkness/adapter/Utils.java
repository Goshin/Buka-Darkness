package io.goshin.bukadarkness.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
    private static byte[] indexKey = null;

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
        generateKey(mangaID, clipID);
        xor(clipBytes);

        result.write(String.format("%08x", clipBytes.length).getBytes("US-ASCII"));
        result.write(clipBytes);

        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
        gzipOutputStream.write(resourceBaseList.getBytes());
        gzipOutputStream.close();

        return new ByteArrayInputStream(result.toByteArray());
    }

    private static void xor(byte[] source) {
        for (int i = 4; i < source.length; i++) {
            source[i] = ((byte) (source[i] ^ indexKey[((i - 4) % 8)]));
        }
    }

    private static void generateKey(int mangaID, int clipID) {
        byte[] key = new byte[8];
        key[0] = ((byte) clipID);
        key[1] = ((byte) (clipID >> 8));
        key[2] = ((byte) (clipID >> 16));
        key[3] = ((byte) (clipID >> 24));
        key[4] = ((byte) mangaID);
        key[5] = ((byte) (mangaID >> 8));
        key[6] = ((byte) (mangaID >> 16));
        key[7] = ((byte) (mangaID >> 24));
        Utils.indexKey = key;
    }

    public static String fixedBitsHash(String o) {
        int bits = 4;
        return String.format("%0" + String.valueOf(bits) + "d", Math.abs(o.hashCode())).substring(0, bits);
    }
}
