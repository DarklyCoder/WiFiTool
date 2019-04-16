package com.darklycoder.wifitool.lib.utils;

import android.util.Log;

public final class WiFiLogUtils {

    private static final String TAG = "WiFiTool-log";

    private static int log_level = 0;

    /**
     * 设置日志级别
     *
     * @param level 级别
     */
    public static void setLogLevel(int level) {
        log_level = level;
    }

    public static void d(String msg) {
        if (log_level < 2) {
            Log.d(TAG, msg);
        }
    }

    public static void e(Throwable throwable) {
        if (log_level < 7) {
            Log.e(TAG, "", throwable);
        }
    }

}
