package com.darklycoder.wifitool;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.Types;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener;

import java.util.List;

/**
 * 单个事件监听demo
 */
public class SingleMonitorActivity extends BaseMonitorActivity {

    @Override
    ScanWiFiActionListener getScanActionListener() {
        return new ScanWiFiActionListener() {

            @Override
            public void onStart() {
                mTvStatus.setText("扫描中...");
                mBtnScan.setEnabled(false);
            }

            @Override
            public void onResult(int type, @Nullable List<WiFiScanInfo> list) {
                if (type == Types.ScanResultType.SUCCESS) {
                    mBtnScan.setEnabled(true);
                    mTvStatus.setText("扫描结束");
                }

                if (null != list) {
                    mData.clear();
                    mData.addAll(list);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    ConnectWiFiActionListener getConnectActionListener(final WiFiScanInfo scanInfo) {
        return new ConnectWiFiActionListener() {

            @Override
            public void onStart() {
                mTvStatus.setText(scanInfo.scanResult.SSID + "连接中...");
                refreshData(scanInfo.scanResult.SSID, WiFiConnectType.CONNECTING);

                notifyState(NetworkInfo.DetailedState.CONNECTING);
            }

            @Override
            public void onCreateConfig(WifiConfiguration configuration) {
                for (WiFiScanInfo info : mData) {
                    if (!TextUtils.isEmpty(scanInfo.scanResult.SSID) && scanInfo.scanResult.SSID.equals(info.scanResult.SSID)) {
                        info.configuration = configuration;
                        break;
                    }
                }

                refreshData(scanInfo.scanResult.SSID, WiFiConnectType.CONNECTING);

                notifyState(NetworkInfo.DetailedState.CONNECTING);
            }

            @Override
            public void onResult(int type) {
                if (Types.ConnectResultType.SUCCESS == type) {
                    mTvStatus.setText(scanInfo.scanResult.SSID + "已连接");
                    refreshData(scanInfo.scanResult.SSID, WiFiConnectType.CONNECTED);

                    notifyState(NetworkInfo.DetailedState.CONNECTED);

                    return;
                }

                mTvStatus.setText(scanInfo.scanResult.SSID + "连接失败，" + type);
                refreshData(scanInfo.scanResult.SSID, WiFiConnectType.DISCONNECTED);

                notifyState(NetworkInfo.DetailedState.DISCONNECTED);

                if (Types.ConnectResultType.DIRECT_PASSWORD_ERROR == type) {
                    showInputDialog(scanInfo, 1);
                }
            }
        };
    }

    @Override
    RemoveWiFiActionListener getRemoveActionListener(final String SSID) {
        return new RemoveWiFiActionListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onResult(int type) {
                if (Types.RemoveResultType.SYSTEM_LIMIT_ERROR == type) {
                    mTvStatus.setText(SSID + "删除失败！");
                    showDelErrorDialog();

                    return;
                }

                mTvStatus.setText(SSID + "删除成功！");
                for (WiFiScanInfo connectInfo : mData) {
                    if (!TextUtils.isEmpty(SSID) && SSID.equals(connectInfo.scanResult.SSID)) {
                        connectInfo.configuration = null;
                        break;
                    }
                }
                refreshData(SSID, WiFiConnectType.DISCONNECTED);
            }
        };
    }

}
