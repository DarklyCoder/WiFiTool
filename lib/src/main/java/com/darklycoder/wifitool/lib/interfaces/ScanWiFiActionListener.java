package com.darklycoder.wifitool.lib.interfaces;

import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.Types;

import java.util.List;

public interface ScanWiFiActionListener extends IActionListener {

    void onResult(@Types.ScanResultType int type, @Nullable List<WiFiScanInfo> list);
}
