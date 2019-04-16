package com.darklycoder.wifitool.lib.interfaces;

import android.net.wifi.WifiConfiguration;

import com.darklycoder.wifitool.lib.type.Types;

public interface ConnectWiFiActionListener extends IActionListener {

    void onCreateConfig(WifiConfiguration configuration);

    void onResult(@Types.ConnectResultType int type);

}
