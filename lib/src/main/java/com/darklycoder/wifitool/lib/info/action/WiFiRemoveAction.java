package com.darklycoder.wifitool.lib.info.action;

import android.support.annotation.NonNull;

import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener;

/**
 * 移除
 */
public class WiFiRemoveAction extends IWiFiAction {

    public String SSID;
    public RemoveWiFiActionListener listener;

    public WiFiRemoveAction(String SSID, RemoveWiFiActionListener listener) {
        this.SSID = SSID;
        this.listener = listener;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " | " + SSID;
    }

}
