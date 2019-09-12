package com.darklycoder.wifitool.lib.info

import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.os.Parcelable
import com.darklycoder.wifitool.lib.type.WiFiCipherType
import com.darklycoder.wifitool.lib.type.WiFiConnectType
import kotlinx.android.parcel.Parcelize

/**
 * WiFi扫描信息
 */
@Parcelize
data class WiFiScanInfo(
        // 扫描结果
        var scanResult: ScanResult? = null,
        // 连接过的WiFi配置，可能为空
        var configuration: WifiConfiguration? = null,
        // WiFi型号强度(1~4)
        var level: Int = 0,
        // 连接状态：0 未连接，1 正在连接，2 已连接
        var connectType: Int = WiFiConnectType.DISCONNECTED.type

) : Comparable<WiFiScanInfo>, Parcelable {

    /**
     * 返回WiFi加密类型
     *
     * @return WiFiCipherType
     */
    val cipherType: WiFiCipherType
        get() {
            val capabilities = scanResult?.capabilities

            if (capabilities.isNullOrEmpty()) {
                return WiFiCipherType.WIFI_CIPHER_INVALID
            }

            if (capabilities.contains("WPA")
                    || capabilities.contains("wpa")
                    || capabilities.contains("WPA2")
                    || capabilities.contains("WPS")) {

                return WiFiCipherType.WIFI_CIPHER_WPA
            }

            return if (capabilities.contains("WEP")
                    || capabilities.contains("wep")) {

                WiFiCipherType.WIFI_CIPHER_WEP

            } else WiFiCipherType.WIFI_CIPHER_NO_PASS
        }

    override fun compareTo(other: WiFiScanInfo): Int {
        // 按照信号强度从大到小排序
        return other.level - level
    }

    override fun toString(): String {
        return "{\"SSID\":\"${scanResult?.SSID}\",\"type\":${cipherType.ordinal},\"level\":$level}"
    }

}
