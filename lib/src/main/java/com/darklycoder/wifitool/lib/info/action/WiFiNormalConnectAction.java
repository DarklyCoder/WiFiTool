package com.darklycoder.wifitool.lib.info.action;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;

/**
 * 通过密码连接WiFi
 */
public class WiFiNormalConnectAction extends WiFiConnectAction {

    public WiFiCipherType cipherType;
    public String password;

    public WiFiNormalConnectAction(@NonNull String SSID, @NonNull WiFiCipherType cipherType, @Nullable String password, @Nullable ConnectWiFiActionListener listener) {
        super(SSID, listener);

        this.cipherType = cipherType;
        this.password = password;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " | " + SSID + " | " + cipherType.name();
    }
}
