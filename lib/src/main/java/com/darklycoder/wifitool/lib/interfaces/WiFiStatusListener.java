package com.darklycoder.wifitool.lib.interfaces;

import android.content.Intent;

/**
 * WiFi状态回调
 */
public interface WiFiStatusListener {

    /**
     * 处理扫描列表
     */
    void handleScanResultsChanged(Intent intent);

    /**
     * 处理WiFi状态
     */
    void handleWiFiStateChanged(Intent intent);

    /**
     * 处理网络状态
     */
    void handleNetStateChanged(Intent intent);

    /**
     * 处理WiFi状态(密码错误回调)
     */
    void handleSupplicantStateChanged(Intent intent);

}
