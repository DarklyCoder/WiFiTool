package com.darklycoder.wifitool.lib

import android.content.Context
import android.net.wifi.WifiConfiguration

import com.darklycoder.wifitool.lib.info.action.WiFiDirectConnectAction
import com.darklycoder.wifitool.lib.info.action.WiFiNormalConnectAction
import com.darklycoder.wifitool.lib.info.action.WiFiRemoveAction
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.WiFiListener
import com.darklycoder.wifitool.lib.type.WiFiCipherType
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils
import com.darklycoder.wifitool.lib.utils.WiFiModuleService

/**
 * WiFi处理模块
 */
object WiFiModule {

    private var mWiFiSupportService: WiFiModuleService? = null
    private var mWiFiConfig: WiFiConfig? = WiFiConfig.Builder().build()
    private var isInit = false // 是否初始化

    /**
     * 设置配置
     */
    fun setConfig(config: WiFiConfig?): WiFiModule {
        this.mWiFiConfig = config
        return this
    }

    /**
     * 初始化
     */
    fun init(context: Context) {
        if (isInit) {
            return
        }

        this.mWiFiSupportService = WiFiModuleService(context)

        WiFiLogUtils.d("初始化")

        isInit = true
    }

    /**
     * 添加WiFi状态监听
     *
     * @param key      唯一标识
     * @param listener 监听回调
     */
    fun addWiFiListener(key: String, listener: WiFiListener) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！")
            return
        }

        mWiFiSupportService?.addWiFiListener(key, listener)
    }

    /**
     * 移除WiFi状态监听
     *
     * @param key 唯一标识
     */
    fun removeWiFiListener(key: String) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！")
            return
        }

        mWiFiSupportService?.removeWiFiListener(key)
    }

    /**
     * 扫描WiFi
     */
    fun startScan(listener: ScanWiFiActionListener? = null) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！")
            return
        }

        val action = WiFiScanAction(listener)
        mWiFiSupportService?.addAction(action)
    }

    /**
     * 通过密码连接WiFi
     */
    fun connectWiFi(SSID: String, type: WiFiCipherType = WiFiCipherType.WIFI_CIPHER_NO_PASS, password: String? = null, listener: ConnectWiFiActionListener? = null) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！")
            return
        }

        val action = WiFiNormalConnectAction(SSID, type, password, listener)
        action.timeout = if (null == mWiFiConfig) -1 else mWiFiConfig!!.timeOut
        mWiFiSupportService?.addAction(action)
    }

    /**
     * 通过已经存在的配置连接WiFi
     */
    fun connectWiFi(configuration: WifiConfiguration, listener: ConnectWiFiActionListener? = null) {
        if (null == mWiFiSupportService) {
            return
        }

        var SSID = configuration.SSID
        val size = SSID.length
        SSID = SSID.substring(1, size - 1)

        val action = WiFiDirectConnectAction(SSID, configuration, listener)
        action.timeout = mWiFiConfig!!.timeOut
        mWiFiSupportService?.addAction(action)
    }

    /**
     * 移除WiFi
     */
    fun removeWiFi(SSID: String, listener: RemoveWiFiActionListener? = null) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！")
            return
        }

        val action = WiFiRemoveAction(SSID, listener)
        mWiFiSupportService?.addAction(action)
    }

    /**
     * 销毁资源
     */
    fun destroy() {
        mWiFiSupportService?.destroy()

        mWiFiSupportService = null
        isInit = false
    }

}
