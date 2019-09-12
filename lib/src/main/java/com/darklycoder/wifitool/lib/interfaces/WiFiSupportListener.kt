package com.darklycoder.wifitool.lib.interfaces

import android.net.wifi.WifiInfo
import com.darklycoder.wifitool.lib.info.action.IWiFiAction
import com.darklycoder.wifitool.lib.info.action.WiFiConnectAction
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction
import com.darklycoder.wifitool.lib.type.Types

interface WiFiSupportListener {

    /**
     * 获取当前执行的action
     */
    val currentAction: IWiFiAction?

    /**
     * 获取当前了解的WiFi信息
     */
    val connectedWifiInfo: WifiInfo?

    fun onWiFiClose()

    fun onWiFiListChange()

    fun doneScanAction(action: WiFiScanAction)

    fun doneConnectSuccess(SSID: String, @Types.ConnectSuccessType type: Int)

    fun doneConnectFail(action: WiFiConnectAction)

}
