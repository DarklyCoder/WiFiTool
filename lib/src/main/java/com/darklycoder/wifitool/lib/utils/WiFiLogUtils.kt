package com.darklycoder.wifitool.lib.utils

import android.util.Log

object WiFiLogUtils {

    private const val TAG = "WiFiTool-log"

    private var log_level = 0

    /**
     * 设置日志级别
     *
     * @param level 级别
     */
    fun setLogLevel(level: Int) {
        log_level = level
    }

    fun d(msg: String) {
        if (log_level < 2) {
            Log.d(TAG, msg)
        }
    }

    fun e(throwable: Throwable) {
        if (log_level < 7) {
            Log.e(TAG, "", throwable)
        }
    }

}
