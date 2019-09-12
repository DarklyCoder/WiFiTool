package com.darklycoder.wifitool.lib.info.action

import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener
import com.darklycoder.wifitool.lib.type.WiFiCipherType

/**
 * 通过密码连接WiFi
 */
class WiFiNormalConnectAction(
        SSID: String,
        var cipherType: WiFiCipherType,
        var password: String? = null,
        listener: ConnectWiFiActionListener? = null

) : WiFiConnectAction(SSID, listener) {

    override fun toString(): String {
        return "${super.toString()} | $SSID | ${cipherType.name} | $password"
    }

}
