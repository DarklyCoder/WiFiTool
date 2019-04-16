package com.darklycoder.wifitool.lib.interfaces.impl;

import android.net.wifi.WifiConfiguration;

import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiGetListType;

import java.util.List;

public class WiFiListenerImpl implements WiFiListener {

    @Override
    public void onStartScan() {

    }

    @Override
    public void onCloseWiFi() {

    }

    @Override
    public void onDataChange(WiFiGetListType type, List<WiFiScanInfo> list) {

    }

    @Override
    public void onWiFiStartConnect(String SSID) {

    }

    @Override
    public void onWiFiCreateConfig(String SSID, WifiConfiguration configuration) {

    }

    @Override
    public void onWiFiConnected(String SSID, boolean isInit) {

    }

    @Override
    public void onWiFiConnectFail(String SSID, WiFiConnectFailType type) {

    }

    @Override
    public void onWiFiRemoveResult(WiFiRemoveStatusInfo info) {

    }

}
