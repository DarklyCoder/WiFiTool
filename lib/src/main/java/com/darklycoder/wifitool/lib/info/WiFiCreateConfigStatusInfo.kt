package com.darklycoder.wifitool.lib.info

import android.net.wifi.WifiConfiguration

/**
 * 创建WiFi配置状态
 */
data class WiFiCreateConfigStatusInfo(
        var SSID: String? = null,
        var configuration: WifiConfiguration? = null,
        var isSuccess: Boolean = false
)