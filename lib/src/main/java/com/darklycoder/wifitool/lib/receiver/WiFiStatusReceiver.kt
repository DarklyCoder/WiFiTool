package com.darklycoder.wifitool.lib.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.text.TextUtils

import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener

/**
 * WiFi状态接受
 */
class WiFiStatusReceiver(private val mCallback: WiFiStatusListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (null == mCallback || action.isNullOrEmpty()) {
            return
        }

        when (action) {
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> mCallback.handleScanResultsChanged(intent)

            WifiManager.WIFI_STATE_CHANGED_ACTION -> mCallback.handleWiFiStateChanged(intent)

            WifiManager.NETWORK_STATE_CHANGED_ACTION -> mCallback.handleNetStateChanged(intent)

            WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> mCallback.handleSupplicantStateChanged(intent)

            else -> {
            }
        }
    }

}
