package com.darklycoder.wifitool.lib.interfaces

import android.net.wifi.WifiConfiguration

import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType
import com.darklycoder.wifitool.lib.type.WiFiGetListType

/**
 * WiFi全局相关状态监听
 */
interface WiFiListener {

    /**
     * 开始扫描WiFi
     */
    fun onStartScan()

    /**
     * 通知WiFi关闭
     */
    fun onCloseWiFi()

    /**
     * WiFi数据更新
     */
    fun onDataChange(type: WiFiGetListType, list: List<WiFiScanInfo>)

    /**
     * 开始连接WiFi
     */
    fun onWiFiStartConnect(SSID: String)

    /**
     * 创建WiFi配置
     */
    fun onWiFiCreateConfig(SSID: String, configuration: WifiConfiguration)

    /**
     * WiFi连接成功
     *
     * @param isInit 标识是否是初始连接成功
     */
    fun onWiFiConnected(SSID: String, isInit: Boolean)

    /**
     * WiFi连接失败
     *
     * @param type 失败类型
     */
    fun onWiFiConnectFail(SSID: String, type: WiFiConnectFailType)

    /**
     * 删除WiFi状态
     */
    fun onWiFiRemoveResult(info: WiFiRemoveStatusInfo)

}
