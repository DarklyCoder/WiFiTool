package com.darklycoder.wifitool.lib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener;

/**
 * WiFi状态接受
 */
public class WiFiStatusReceiver extends BroadcastReceiver {

    private WiFiStatusListener mCallback;

    public WiFiStatusReceiver(WiFiStatusListener callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (null == mCallback || null == action || TextUtils.isEmpty(action)) {
            return;
        }

        switch (action) {
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                mCallback.handleScanResultsChanged(intent);
                break;

            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                mCallback.handleWiFiStateChanged(intent);
                break;

            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                mCallback.handleNetStateChanged(intent);
                break;

            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                mCallback.handleSupplicantStateChanged(intent);
                break;

            default:
                break;
        }
    }

}
