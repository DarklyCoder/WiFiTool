package com.darklycoder.wifitool.lib.interfaces

import com.darklycoder.wifitool.lib.type.Types

interface RemoveWiFiActionListener : IActionListener {

    fun onResult(@Types.RemoveResultType type: Int)
}
