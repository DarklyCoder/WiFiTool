package com.darklycoder.wifitool.lib.info;

import android.net.wifi.WifiConfiguration;

/**
 * 创建WiFi配置状态
 */
public class WiFiCreateConfigStatusInfo {

    public String SSID;
    public WifiConfiguration configuration;
    public boolean isSuccess;

    public boolean isSuccess() {
        return isSuccess && null != configuration;
    }

}
