package com.darklycoder.wifitool.lib.interfaces

import android.content.Intent

/**
 * WiFi状态回调
 */
interface WiFiStatusListener {

    /**
     * 处理扫描列表
     */
    fun handleScanResultsChanged(intent: Intent)

    /**
     * 处理WiFi状态
     */
    fun handleWiFiStateChanged(intent: Intent)

    /**
     * 处理网络状态
     */
    fun handleNetStateChanged(intent: Intent)

    /**
     * 处理WiFi状态(密码错误回调)
     */
    fun handleSupplicantStateChanged(intent: Intent)

}
