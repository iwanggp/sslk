package com.jufan.cyss.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cyjss on 2014/12/23.
 */
public class MD5 {

    private static MessageDigest md5;
    private static final String LOG_TAG = "MD5";

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt32(String source) throws NoSuchAlgorithmException {
        String result = "";
        md5.update(source.getBytes());
        byte b[] = md5.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        result = buf.toString();

        return result;
    }

    public static String encrypt16(String source) throws NoSuchAlgorithmException {
        String result = "";
        md5.update(source.getBytes());
        byte b[] = md5.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        result = buf.toString().substring(8, 24);
        return result;
    }
}
