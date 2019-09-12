package com.darklycoder.wifitool.lib.interfaces

import android.net.wifi.WifiConfiguration

import com.darklycoder.wifitool.lib.type.Types

interface ConnectWiFiActionListener : IActionListener {

    fun onCreateConfig(configuration: WifiConfiguration)

    fun onResult(@Types.ConnectResultType type: Int)

}
