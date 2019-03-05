package com.chekn.engltnmate;

import android.util.Log;

/**
 * Created by CHEKN on 2018-12-15.
 */

public class LogUtils {

    private static String TAG = "ansen";

    public static void e(String s) {
        if( s==null ) s= "val -> null";
        Log.e(TAG, s);
    }

    public static void i(String s) {
        if( s==null ) s= "val -> null";
        Log.i(TAG, s);
    }

    public static void e(String s, Exception e) {
        if( s==null ) s= "val -> null";
        Log.e(TAG, s);
    }
}
