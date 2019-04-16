package com.darklycoder.wifitool.lib.interfaces;

import com.darklycoder.wifitool.lib.type.Types;

public interface RemoveWiFiActionListener extends IActionListener {

    void onResult(@Types.RemoveResultType int type);
}
