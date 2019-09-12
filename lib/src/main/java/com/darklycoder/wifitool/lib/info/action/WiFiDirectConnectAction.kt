package com.darklycoder.wifitool.lib.info.action

import android.net.wifi.WifiConfiguration

import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener

/**
 * 通过配置直接连接
 */
class WiFiDirectConnectAction(
        SSID: String,
        var configuration: WifiConfiguration,
        listener: ConnectWiFiActionListener? = null

) : WiFiConnectAction(SSID, listener) {

    override fun toString(): String {
        return "${super.toString()} | $SSID"
    }

}
