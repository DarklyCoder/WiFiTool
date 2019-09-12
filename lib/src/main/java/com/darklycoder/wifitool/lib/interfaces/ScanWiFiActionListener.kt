package com.darklycoder.wifitool.lib.interfaces

import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.type.Types

interface ScanWiFiActionListener : IActionListener {

    fun onResult(@Types.ScanResultType type: Int, list: List<WiFiScanInfo>?)
}
