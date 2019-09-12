package com.darklycoder.wifitool.lib.interfaces.impl

import android.net.wifi.WifiConfiguration

import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.interfaces.WiFiListener
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType
import com.darklycoder.wifitool.lib.type.WiFiGetListType

/**
 * WiFi全局相关状态监听默认实现
 */
open class WiFiListenerImpl : WiFiListener {

    override fun onStartScan() {

    }

    override fun onCloseWiFi() {

    }

    override fun onDataChange(type: WiFiGetListType, list: List<WiFiScanInfo>) {

    }

    override fun onWiFiStartConnect(SSID: String) {

    }

    override fun onWiFiCreateConfig(SSID: String, configuration: WifiConfiguration) {

    }

    override fun onWiFiConnected(SSID: String, isInit: Boolean) {

    }

    override fun onWiFiConnectFail(SSID: String, type: WiFiConnectFailType) {

    }

    override fun onWiFiRemoveResult(info: WiFiRemoveStatusInfo) {

    }

}
