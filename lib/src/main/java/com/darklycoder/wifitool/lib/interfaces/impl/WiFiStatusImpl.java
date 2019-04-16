package com.darklycoder.wifitool.lib.interfaces.impl;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.darklycoder.wifitool.lib.info.action.IWiFiAction;
import com.darklycoder.wifitool.lib.info.action.WiFiConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiDirectConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiDisableAction;
import com.darklycoder.wifitool.lib.info.action.WiFiEnableAction;
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction;
import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener;
import com.darklycoder.wifitool.lib.interfaces.WiFiSupportListener;
import com.darklycoder.wifitool.lib.type.Types;
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils;

public class WiFiStatusImpl implements WiFiStatusListener {

    private WiFiSupportListener mSupportListener;

    public WiFiStatusImpl(WiFiSupportListener listener) {
        this.mSupportListener = listener;
    }

    @Override
    public void handleScanResultsChanged(Intent intent) {
        if (null == mSupportListener) {
            return;
        }

        IWiFiAction action = mSupportListener.getCurrentAction();
        if (action instanceof WiFiScanAction) {
            //扫描结束
            WiFiLogUtils.d("扫描结束，" + action.toString());
            mSupportListener.doneScanAction((WiFiScanAction) action);
            return;
        }

        //WiFi列表发生变动
        mSupportListener.onWiFiListChange();
    }

    @Override
    public void handleWiFiStateChanged(Intent intent) {
        if (null == intent) {
            return;
        }

        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED: {
                //WIFI处于关闭状态
                WiFiLogUtils.d("WIFI已关闭");

                IWiFiAction action = (null == mSupportListener) ? null : mSupportListener.getCurrentAction();
                if (action instanceof WiFiDisableAction) {
                    action.end();
                }

                if (null != mSupportListener) {
                    mSupportListener.onWiFiClose();
                }
                break;
            }

            case WifiManager.WIFI_STATE_DISABLING: {
                //正在关闭
                WiFiLogUtils.d("WIFI关闭中");
                break;
            }

            case WifiManager.WIFI_STATE_ENABLED: {
                //已经打开
                WiFiLogUtils.d("WIFI已打开");

                //判断当前执行的action是否是WiFiScanAction
                IWiFiAction action = (null == mSupportListener) ? null : mSupportListener.getCurrentAction();
                if (action instanceof WiFiEnableAction) {
                    action.end();
                }
                break;
            }

            case WifiManager.WIFI_STATE_ENABLING: {
                //正在打开
                WiFiLogUtils.d("打开WIFI中...");
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void handleNetStateChanged(Intent intent) {
        if (null == intent) {
            return;
        }

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        NetworkInfo.DetailedState state = info.getDetailedState();

        if (NetworkInfo.DetailedState.CONNECTED == state) {
            if (null == mSupportListener) {
                return;
            }

            WifiInfo wifiInfo = mSupportListener.getConnectedWifiInfo();
            if (null == wifiInfo) {
                WiFiLogUtils.d("当前连接的 WifiInfo 为空");
                return;
            }

            String SSID = wifiInfo.getSSID();
            int size = SSID.length();
            SSID = SSID.substring(1, size - 1);

            //连接成功
            IWiFiAction action = mSupportListener.getCurrentAction();

            if (action instanceof WiFiConnectAction) {
                WiFiConnectAction wiFiConnectAction = (WiFiConnectAction) action;

                if (!wiFiConnectAction.SSID.equals(SSID)) {
                    WiFiLogUtils.d("当前" + wiFiConnectAction.SSID + "与" + SSID + "不一致！");
                    mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.NOT_MATCH);

                    return;
                }

                if (null != wiFiConnectAction.listener) {
                    wiFiConnectAction.listener.onResult(Types.ConnectResultType.SUCCESS);
                }

                mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.NORMAL);

                WiFiLogUtils.d("WiFi连接成功，" + action.toString());
                action.end();

                return;
            }

            WiFiLogUtils.d("WiFi连接成功，" + SSID);
            mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.SYSTEM);
        }
    }

    @Override
    public void handleSupplicantStateChanged(Intent intent) {
        if (null == intent) {
            return;
        }

        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        int errorResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);

        if (null != state && state == SupplicantState.DISCONNECTED && errorResult == WifiManager.ERROR_AUTHENTICATING) {

            if (null == mSupportListener) {
                return;
            }

            //密码错误
            IWiFiAction action = mSupportListener.getCurrentAction();

            if (action instanceof WiFiConnectAction) {

                if (Types.ActionStateType.END == action.getActionState()) {
                    return;
                }

                if (null != ((WiFiConnectAction) action).listener) {
                    if (action instanceof WiFiDirectConnectAction) {
                        ((WiFiConnectAction) action).listener.onResult(Types.ConnectResultType.DIRECT_PASSWORD_ERROR);

                    } else {
                        ((WiFiConnectAction) action).listener.onResult(Types.ConnectResultType.PASSWORD_ERROR);
                    }
                }

                mSupportListener.doneConnectFail(((WiFiConnectAction) action));
                WiFiLogUtils.d("WiFi连接失败，密码错误，" + action.toString());
                action.end();
            }
        }
    }

}
