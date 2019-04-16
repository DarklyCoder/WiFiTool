package com.darklycoder.wifitool;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiListenerImpl;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;
import com.darklycoder.wifitool.lib.type.WiFiGetListType;
import com.darklycoder.wifitool.lib.WiFiModule;

import java.util.List;

/**
 * 全局事件监听demo
 */
public class GlobalMonitorActivity extends BaseMonitorActivity {

    @Override
    void initParams() {
        super.initParams();

        //添加监听
        WiFiModule.getInstance().addWiFiListener(TAG, mListener);
    }

    private WiFiListener mListener = new WiFiListenerImpl() {

        @Override
        public void onStartScan() {
            mTvStatus.setText("扫描中...");
            mBtnScan.setEnabled(false);
        }

        @Override
        public void onCloseWiFi() {
            mTvStatus.setText("WiFi已关闭");

            mData.clear();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDataChange(WiFiGetListType type, List<WiFiScanInfo> list) {
            if (type == WiFiGetListType.TYPE_SCAN) {
                mBtnScan.setEnabled(true);
                mTvStatus.setText("扫描结束");
            }

            mData.clear();
            mData.addAll(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onWiFiStartConnect(String SSID) {
            mTvStatus.setText(SSID + "连接中...");
            refreshData(SSID, WiFiConnectType.CONNECTING);

            notifyState(NetworkInfo.DetailedState.CONNECTING);
        }

        @Override
        public void onWiFiCreateConfig(String SSID, WifiConfiguration configuration) {
            for (WiFiScanInfo info : mData) {
                if (!TextUtils.isEmpty(SSID) && SSID.equals(info.scanResult.SSID)) {
                    info.configuration = configuration;
                    break;
                }
            }

            refreshData(SSID, WiFiConnectType.CONNECTING);

            notifyState(NetworkInfo.DetailedState.CONNECTING);
        }

        @Override
        public void onWiFiConnected(String SSID, boolean isInit) {
            mTvStatus.setText(isInit + " || " + SSID + "已连接");
            refreshData(SSID, WiFiConnectType.CONNECTED);

            notifyState(NetworkInfo.DetailedState.CONNECTED);
        }

        @Override
        public void onWiFiConnectFail(String SSID, WiFiConnectFailType type) {
            if (TextUtils.isEmpty(SSID)) {
                return;
            }

            mTvStatus.setText(SSID + "连接失败，" + type.name());
            refreshData(SSID, WiFiConnectType.DISCONNECTED);

            notifyState(NetworkInfo.DetailedState.DISCONNECTED);

            if (type == WiFiConnectFailType.DIRECT_PASSWORD_ERROR) {
                //直连密码错误，提示用户修改密码
                WiFiScanInfo scanInfo = findScanInfo(SSID);
                if (null != scanInfo) {
                    showInputDialog(scanInfo, 1);
                }
            }
        }

        @Override
        public void onWiFiRemoveResult(WiFiRemoveStatusInfo info) {
            if (!info.isSuccess) {
                mTvStatus.setText(info.SSID + "删除失败！");
                showDelErrorDialog();

                return;
            }

            mTvStatus.setText(info.SSID + "删除成功！");
            for (WiFiScanInfo connectInfo : mData) {
                if (!TextUtils.isEmpty(info.SSID) && info.SSID.equals(connectInfo.scanResult.SSID)) {
                    connectInfo.configuration = null;
                    break;
                }
            }
            refreshData(info.SSID, WiFiConnectType.DISCONNECTED);
        }
    };

    @Override
    protected void onDestroy() {
        WiFiModule.getInstance().removeWiFiListener(TAG);
        super.onDestroy();
    }

}
