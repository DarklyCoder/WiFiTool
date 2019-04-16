package com.darklycoder.wifitool.lib.info.action;

import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;

/**
 * 通过配置直接连接
 */
public class WiFiDirectConnectAction extends WiFiConnectAction {

    public WifiConfiguration configuration;

    public WiFiDirectConnectAction(@NonNull String SSID, @NonNull WifiConfiguration configuration, @Nullable ConnectWiFiActionListener listener) {
        super(SSID, listener);
        this.configuration = configuration;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " | " + SSID;
    }

}
