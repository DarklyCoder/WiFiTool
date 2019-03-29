package com.darklycoder.wifitool.lib.info;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;

import java.io.Serializable;

/**
 * WiFi扫描信息
 */
public class WiFiScanInfo implements Serializable, Comparable<WiFiScanInfo> {

    //扫描结果
    public ScanResult scanResult;
    //连接过的WiFi配置，可能为空
    public WifiConfiguration configuration;
    //WiFi型号强度(1~4)
    public int level;
    //连接状态：0 未连接，1 正在连接，2 已连接
    public int connectType = WiFiConnectType.DISCONNECTED.type;

    @Override
    public int compareTo(WiFiScanInfo o) {
        //按照信号强度从大到小排序
        return o.level - level;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "\"SSID\":\"" + scanResult.SSID + "\"," +
                "\"type\":" + getCipherType().ordinal() + "," +
                "\"level\":" + level +
                "}";
    }

    /**
     * 返回WiFi加密类型
     *
     * @return WiFiCipherType
     */
    public WiFiCipherType getCipherType() {
        if (null == scanResult) {
            return WiFiCipherType.WIFI_CIPHER_INVALID;
        }

        String capabilities = scanResult.capabilities;

        if (TextUtils.isEmpty(capabilities)) {
            return WiFiCipherType.WIFI_CIPHER_INVALID;
        }

        if (capabilities.contains("WPA") || capabilities.contains("wpa") || capabilities.contains("WPA2") || capabilities.contains("WPS")) {
            return WiFiCipherType.WIFI_CIPHER_WPA;
        }

        if (capabilities.contains("WEP") || capabilities.contains("wep")) {
            return WiFiCipherType.WIFI_CIPHER_WEP;
        }

        return WiFiCipherType.WIFI_CIPHER_NO_PASS;
    }

}
