package com.wisape.android.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public class SecurityUtils{

    /**
     * Encoding with MD5 for String
     * @param source input string
     * @return md5 String. maybe return null if the source is null.
     */
    public static String md5(String source){
        if(null == source || 0 == source.length()){
            return null;
        }

        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(source.getBytes());
            return toHexString(digester.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHexString(byte [] hashValues){
        StringBuilder builder = new StringBuilder(hashValues.length);
        for(byte hash : hashValues){
            builder.append(Integer.toString(( hash & 0xFF ) + 0x100, 16).substring( 1 ));
        }
        return builder.toString();
    }
}
