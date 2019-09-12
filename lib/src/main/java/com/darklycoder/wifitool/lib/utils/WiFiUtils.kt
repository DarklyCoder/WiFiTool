package com.darklycoder.wifitool.lib.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.wifi.*
import android.os.Build
import android.text.TextUtils
import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.type.WiFiCipherType
import com.darklycoder.wifitool.lib.type.WiFiConnectType
import java.util.*

/**
 * WiFi工具类
 */
object WiFiUtils {

    /**
     * WiFi是否可用
     *
     * @return 是否执行成功
     */
    fun isWiFiEnable(manager: WifiManager?): Boolean {
        try {
            return manager?.isWifiEnabled ?: false

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return false
    }

    /**
     * 开始扫描
     *
     * @return 是否执行成功
     */
    fun startScan(manager: WifiManager?): Boolean {
        try {
            return manager?.startScan() ?: false

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return false
    }

    /**
     * 设置WiFi状态
     *
     * @return 是否执行成功
     */
    fun setWifiEnabled(manager: WifiManager?, isOpen: Boolean): Boolean {
        try {
            return manager?.setWifiEnabled(isOpen) ?: false

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return false
    }

    /**
     * 获取扫描到的WiFi列表
     */
    fun getScanList(manager: WifiManager?): List<WiFiScanInfo> {
        if (null == manager) {
            return ArrayList()
        }

        val curSSID = if (isActiveWifi(manager)) manager.connectionInfo.ssid else ""

        val results = noSameName(manager.scanResults)
        val list = ArrayList<WiFiScanInfo>()
        var isCur: Boolean
        var curInfo: WiFiScanInfo? = null//当前连接上的wifi
        val normalList = ArrayList<WiFiScanInfo>()//不存在配置的列表
        val existConfigList = ArrayList<WiFiScanInfo>()//存在配置的列表

        for (result in results) {
            val connectInfo = WiFiScanInfo()
            connectInfo.scanResult = result
            connectInfo.configuration = getExistConfig(manager, result.SSID)

            isCur = !TextUtils.isEmpty(curSSID) && curSSID == "\"" + result.SSID + "\""
            connectInfo.connectType = if (isCur) WiFiConnectType.CONNECTED.type else WiFiConnectType.DISCONNECTED.type
            connectInfo.level = WifiManager.calculateSignalLevel(result.level, 4) + 1

            if (isCur) {
                // 当前已连接
                curInfo = connectInfo

            } else {
                if (null != connectInfo.configuration) {
                    // 存在配置
                    existConfigList.add(connectInfo)

                } else {
                    normalList.add(connectInfo)
                }
            }
        }

        // 优先把保存了配置的放在上面
        if (null != curInfo) {
            list.add(curInfo)
        }

        existConfigList.sort()
        list.addAll(existConfigList)

        normalList.sort()
        list.addAll(normalList)

        return list
    }

    /**
     * 获取当前连接的WiFi
     */
    fun getConnectedWifiInfo(manager: WifiManager?): WifiInfo? {
        val isActive = isActiveWifi(manager)

        return if (isActive) manager?.connectionInfo else null
    }

    /**
     * 是否有当前可用的WiFi连接
     */
    fun isActiveWifi(manager: WifiManager?): Boolean {
        if (null == manager) {
            return false
        }

        val info = manager.connectionInfo ?: return false

        val ssid = info.ssid

        return (SupplicantState.COMPLETED == info.supplicantState
                && !TextUtils.isEmpty(ssid)
                && !ssid.equals("0x", ignoreCase = true)
                && "<unknown ssid>" != ssid)
    }

    /**
     * 关闭所有连接
     */
    fun closeAllConnect(manager: WifiManager?) {
        if (null == manager) {
            return
        }

        // FIXME: 2019/4/16 防止自动连接，小米等手机会弹出权限框
        for (c in manager.configuredNetworks) {
            manager.disableNetwork(c.networkId)
        }

        // 断开后会自动连接WiFi
        //        manager.disconnect();
    }

    /**
     * 连接WiFi
     */
    fun connectWiFi(manager: WifiManager?, SSID: String, type: WiFiCipherType, pwd: String?, context: Context?): WiFiCreateConfigStatusInfo {
        val info = WiFiCreateConfigStatusInfo()
        info.SSID = SSID
        info.isSuccess = false

        if (null == manager) {
            return info
        }

        // 先关闭当前所有已连接的网络
        closeAllConnect(manager)

        getExistConfig(manager, SSID) ?: return addWifi(manager, SSID, type, pwd) // 不存在旧的配置，添加WiFi

        // 存在旧的配置，先尝试移除WiFi，移除成功，重新添加WiFi
        return if (removeWiFi(manager, SSID, context).isSuccess) addWifi(manager, SSID, type, pwd) else info
    }

    /**
     * 根据networkId连接WiFi
     */
    fun enableNetwork(manager: WifiManager?, networkId: Int): Boolean {
        return null != manager && manager.enableNetwork(networkId, true)
    }

    /**
     * 移除WiFi
     *
     * Android6.0 之后应用只能删除自己创建的WIFI网络
     */
    fun removeWiFi(manager: WifiManager?, SSID: String, context: Context?): WiFiRemoveStatusInfo {
        val info = WiFiRemoveStatusInfo()
        info.SSID = SSID

        if (null == manager) {
            return info
        }

        val config = getExistConfig(manager, SSID)

        if (null == config) {
            //如果不存在配置，默认是删除成功
            info.isSuccess = true
            return info
        }

        if (config.networkId == -1) {
            info.isSuccess = false
            WiFiLogUtils.d("networkId 非法！")
            return info
        }

        val isSystemApp = isSystemApplication(context)
        if (isSystemApp) {
            WiFiLogUtils.d("是系统App，可以直接删除！")

            info.isSuccess = (manager.disableNetwork(config.networkId)
                    && manager.removeNetwork(config.networkId)
                    && manager.saveConfiguration())

            return info
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // 6.0之前
            info.isSuccess = (manager.disableNetwork(config.networkId)
                    && manager.removeNetwork(config.networkId)
                    && manager.saveConfiguration())

            return info
        }

        try {
            // 获取当前WiFi的创建者
            val field = config.javaClass.getDeclaredField("creatorName")
            field.isAccessible = true

            val creatorName = field.get(config)

            WiFiLogUtils.d("field:$field||creatorName：$creatorName")

            if (context?.packageName == creatorName) {
                WiFiLogUtils.d("是当前app创建的WiFi，可以直接删除！")

                info.isSuccess = (manager.disableNetwork(config.networkId)
                        && manager.removeNetwork(config.networkId)
                        && manager.saveConfiguration())
            }

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return info
    }

    /**
     * 添加WiFi到系统
     */
    private fun addWifi(manager: WifiManager, SSID: String, type: WiFiCipherType, pwd: String?): WiFiCreateConfigStatusInfo {
        val configInfo = WiFiCreateConfigStatusInfo()
        configInfo.SSID = SSID

        try {
            val configuration = createConfiguration(SSID, type, pwd)
            configuration.networkId = manager.addNetwork(configuration)

            configInfo.configuration = configuration
            configInfo.isSuccess = configuration.networkId != -1

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return configInfo
    }

    /**
     * 创建配置
     */
    private fun createConfiguration(SSID: String, type: WiFiCipherType, password: String?): WifiConfiguration {
        val config = WifiConfiguration()

        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()

        config.SSID = "\"" + SSID + "\""

        when (type) {
            WiFiCipherType.WIFI_CIPHER_NO_PASS ->
                // config.wepKeys[0] = "";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)

            WiFiCipherType.WIFI_CIPHER_WEP -> {
                if (!password.isNullOrEmpty()) {
                    if (isHexWepKey(password)) {
                        config.wepKeys[0] = password

                    } else {
                        config.wepKeys[0] = "\"" + password + "\""
                    }
                }

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.wepTxKeyIndex = 0
            }

            WiFiCipherType.WIFI_CIPHER_WPA -> {
                config.preSharedKey = "\"" + password + "\""
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                config.status = WifiConfiguration.Status.ENABLED
            }

            else -> {
            }
        }// config.wepTxKeyIndex = 0;

        return config
    }

    /**
     * 获取是否已经存在的配置
     */
    private fun getExistConfig(manager: WifiManager?, SSID: String): WifiConfiguration? {
        if (null == manager) {
            return null
        }

        try {
            val existingConfigs = manager.configuredNetworks

            for (existingConfig in existingConfigs) {
                if (existingConfig.SSID == "\"" + SSID + "\"") {
                    return existingConfig
                }
            }

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return null
    }

    /**
     * 去除同名WIFI
     */
    private fun noSameName(oldSr: List<ScanResult>): List<ScanResult> {
        val newSr = ArrayList<ScanResult>()
        for (result in oldSr) {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID)) {
                newSr.add(result)
            }
        }
        return newSr
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     */
    private fun containName(sr: List<ScanResult>, name: String): Boolean {
        for (result in sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID == name) {
                return true
            }
        }

        return false
    }

    private fun isHexWepKey(wepKey: String): Boolean {
        val len = wepKey.length

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        return (len == 10 || len == 26 || len == 58) && isHex(wepKey)
    }

    private fun isHex(key: String): Boolean {
        for (i in key.length - 1 downTo 0) {
            val c = key[i]
            if (!(c in '0'..'9' || c in 'A'..'F' || c in 'a'..'f')) {
                return false
            }
        }
        return true
    }

    private fun isSystemApplication(context: Context?): Boolean {
        try {
            if (null == context) {
                return false
            }

            val packageManager = context.packageManager

            val app = packageManager.getApplicationInfo(context.packageName, 0)
            return app.flags and ApplicationInfo.FLAG_SYSTEM > 0

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }

        return false
    }

}
