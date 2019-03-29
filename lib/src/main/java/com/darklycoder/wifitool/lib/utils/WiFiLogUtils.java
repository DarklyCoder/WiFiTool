package com.darklycoder.wifitool.lib.utils;

import android.util.Log;

public final class WiFiLogUtils {

    private static final String TAG = "WiFiTool";

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(Throwable throwable) {
        Log.e(TAG, "", throwable);
    }

}
