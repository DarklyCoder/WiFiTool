package com.darklycoder.wifitool.lib.info.action

import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener

/**
 * 移除
 */
class WiFiRemoveAction(
        var SSID: String,
        var listener: RemoveWiFiActionListener? = null

) : IWiFiAction() {

    override fun toString(): String {
        return "${super.toString()} | " + SSID
    }

}
