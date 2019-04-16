package com.darklycoder.wifitool.lib.interfaces;

import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.type.Types;
import com.darklycoder.wifitool.lib.info.action.IWiFiAction;
import com.darklycoder.wifitool.lib.info.action.WiFiConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction;

public interface WiFiSupportListener {

    /**
     * 获取当前执行的action
     */
    @Nullable
    IWiFiAction getCurrentAction();

    /**
     * 获取当前了解的WiFi信息
     */
    WifiInfo getConnectedWifiInfo();

    void onWiFiClose();

    void onWiFiListChange();

    void doneScanAction(@NonNull WiFiScanAction action);

    void doneConnectSuccess(@NonNull String SSID, @Types.ConnectSuccessType int type);

    void doneConnectFail(@NonNull WiFiConnectAction action);

}
