package com.darklycoder.wifitool.lib.info.action;

import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener;

/**
 * 扫描WiFi
 */
public class WiFiScanAction extends IWiFiAction {

    public ScanWiFiActionListener listener;

    public WiFiScanAction(ScanWiFiActionListener listener) {
        this.listener = listener;
    }

}
