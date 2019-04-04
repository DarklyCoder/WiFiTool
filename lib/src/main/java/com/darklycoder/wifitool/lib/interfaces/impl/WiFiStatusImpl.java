package com.darklycoder.wifitool.lib.interfaces.impl;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.WiFiConfig;
import com.darklycoder.wifitool.lib.WiFiModule;
import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener;
import com.darklycoder.wifitool.lib.type.WiFGetListType;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiOperateStatus;
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理WiFi状态回调
 */
public class WiFiStatusImpl implements WiFiStatusListener {

    private static final int WHAT_TIME_OUT = 1;

    private String waitForConnectSSID = null;//待连接的WiFi
    private WiFiOperateStatus mOperateStatus = WiFiOperateStatus.IDLE;//当前状态
    private HashMap<String, WiFiListener> mListeners;
    private Handler mHandler;
    private boolean isTimeOut = false;//是否是超时
    private long connectedTime = 0;//连接成功时间

    public WiFiStatusImpl(HashMap<String, WiFiListener> listeners) {
        this.mListeners = listeners;
        this.mHandler = new Handler(new StatusCallBack());
    }

    @Override
    public void handleScanResultsChanged(Intent intent) {
        WiFiModule.getInstance().getScanList(WiFGetListType.TYPE_SCAN);
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
                this.mOperateStatus = WiFiOperateStatus.CLOSED;
                break;
            }

            case WifiManager.WIFI_STATE_DISABLING: {
                //正在关闭
                WiFiLogUtils.d("WIFI关闭中");

                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onCloseWiFi();
                }

                this.mOperateStatus = WiFiOperateStatus.CLOSING;
                break;
            }

            case WifiManager.WIFI_STATE_ENABLED: {
                //已经打开
                WiFiLogUtils.d("WIFI已打开:" + mOperateStatus.state);
                if (WiFiOperateStatus.OPENING == mOperateStatus) {
                    WiFiLogUtils.d("WIFI已打开");
                    WiFiModule.getInstance().startScan();
                    return;
                }

                this.mOperateStatus = WiFiOperateStatus.OPENED;
                break;
            }

            case WifiManager.WIFI_STATE_ENABLING: {
                //正在打开
                WiFiLogUtils.d("打开WIFI中...");
                this.mOperateStatus = WiFiOperateStatus.OPENING;
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
            WifiInfo wifiInfo = WiFiModule.getInstance().getConnectedWifiInfo();
            if (null == wifiInfo) {
                WiFiLogUtils.d("当前连接的 WifiInfo 为空");
                return;
            }

            String SSID = wifiInfo.getSSID();
            int size = SSID.length();
            SSID = SSID.substring(1, size - 1);

            if (!TextUtils.isEmpty(waitForConnectSSID) && !waitForConnectSSID.equals(SSID)) {
                //待连接的和当前连接的不一致
                WiFiLogUtils.d("待连接的和当前连接的不一致-》waitForConnectSSID：" + waitForConnectSSID + " ||SSID：" + SSID);
                return;
            }

            boolean isInit = TextUtils.isEmpty(this.waitForConnectSSID);

            this.waitForConnectSSID = SSID;

            if (System.currentTimeMillis() - connectedTime <= 500) {
                WiFiLogUtils.d("过滤 " + waitForConnectSSID + " WIFI连接成功");

                this.waitForConnectSSID = null;
                return;
            }

            connectedTime = System.currentTimeMillis();

            WiFiLogUtils.d(waitForConnectSSID + " WIFI连接成功");

            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiConnected(waitForConnectSSID, isInit);
            }

            //连接成功
            mHandler.removeMessages(WHAT_TIME_OUT);
            this.mOperateStatus = WiFiOperateStatus.CONNECTED;
            this.waitForConnectSSID = null;

            WiFiModule.getInstance().getScanList(WiFGetListType.TYPE_SORT);
        }
    }

    @Override
    public void handleSupplicantStateChanged(Intent intent) {
        if (TextUtils.isEmpty(waitForConnectSSID)) {
            return;
        }

        int errorResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);

        if (errorResult == WifiManager.ERROR_AUTHENTICATING) {
            this.mOperateStatus = WiFiOperateStatus.CONNECT_FAIL;

            mHandler.removeMessages(WHAT_TIME_OUT);

            if (isTimeOut) {
                isTimeOut = false;
                WiFiLogUtils.d("由于 " + waitForConnectSSID + " WIFI连接超时，忽略密码错误");
                return;
            }

            WiFiLogUtils.d(waitForConnectSSID + " WIFI连接失败，密码错误");

            //密码错误
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiConnectFail(waitForConnectSSID, WiFiConnectFailType.PASSWORD_ERROR);
            }

            this.waitForConnectSSID = null;
        }
    }

    @Override
    public void notifyStartScan() {
        if (WiFiModule.getInstance().isWiFiEnable()) {
            this.mOperateStatus = WiFiOperateStatus.SCANNING;

        } else {
            this.mOperateStatus = WiFiOperateStatus.CLOSED;
        }

        WiFiLogUtils.d("开始扫描WIFI列表！");

        for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
            entry.getValue().onStartScan();
        }
    }

    @Override
    public void notifyStartConnect(String SSID, WiFiConfig config) {
        WiFiLogUtils.d("开始连接 " + SSID);

        this.waitForConnectSSID = SSID;
        this.mOperateStatus = WiFiOperateStatus.CONNECTING;

        for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
            entry.getValue().onWiFiStartConnect(SSID);
        }

        //超时检测
        mHandler.removeMessages(WHAT_TIME_OUT);
        Message message = Message.obtain();
        message.what = WHAT_TIME_OUT;
        message.obj = SSID;
        mHandler.sendMessageDelayed(message, config.timeOut);
    }

    @Override
    public void notifyStartConnectStatus(WiFiCreateConfigStatusInfo info) {
        if (info.isSuccess()) {
            WiFiLogUtils.d("开始扫描WIFI，" + info.SSID + " 配置创建成功！");

            this.mOperateStatus = WiFiOperateStatus.CONNECTING;

            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiCreateConfig(info.SSID, info.configuration);
            }

            WiFiModule.getInstance().enableNetwork(info.configuration.networkId);

            return;
        }

        WiFiLogUtils.d("连接 " + info.SSID + " WIFI失败，系统限制，无法移除WIFI！");

        for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
            entry.getValue().onWiFiConnectFail(info.SSID, WiFiConnectFailType.SYSTEM_LIMIT_ERROR);
        }

        //初始连接失败，移除超时检测
        mHandler.removeMessages(WHAT_TIME_OUT);
        this.mOperateStatus = WiFiOperateStatus.CONNECT_FAIL;
        this.waitForConnectSSID = null;
    }

    @Override
    public void notifyWiFiList(WiFGetListType type, List<WiFiScanInfo> list) {
        if (type == WiFGetListType.TYPE_SCAN) {
            WiFiLogUtils.d("WIFI列表扫描结束！");
            this.mOperateStatus = WiFiOperateStatus.SCANNED;

        } else {
            WiFiLogUtils.d("WIFI列表排序刷新！");
        }

        for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
            entry.getValue().onDataChange(type, list);
        }
    }

    @Override
    public void notifyRemoveStatus(WiFiRemoveStatusInfo info) {
        for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
            entry.getValue().onWiFiRemoveResult(info);
        }

        WiFiLogUtils.d(info.SSID + " WIFI移除 " + info.isSuccess);

        WiFiModule.getInstance().getScanList(WiFGetListType.TYPE_SORT);
    }

    @Override
    public WiFiOperateStatus getWiFiOperateStatus() {

        return mOperateStatus;
    }

    @Override
    public void destroy() {
        try {
            mHandler.removeCallbacksAndMessages(null);

            this.mOperateStatus = WiFiOperateStatus.IDLE;
            this.waitForConnectSSID = null;

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    private class StatusCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case WHAT_TIME_OUT:
                    //检查有没有超时
                    mHandler.removeMessages(WHAT_TIME_OUT);

                    if (WiFiOperateStatus.CONNECTING != mOperateStatus) {
                        return true;
                    }

                    if (TextUtils.isEmpty(waitForConnectSSID)) {
                        return true;
                    }

                    if (!waitForConnectSSID.equals(msg.obj)) {
                        return true;
                    }

                    isTimeOut = true;
                    WiFiLogUtils.d("连接 " + msg.obj + " WIFI失败，连接超时！");

                    for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                        entry.getValue().onWiFiConnectFail(waitForConnectSSID, WiFiConnectFailType.TIMEOUT_ERROR);
                    }

                    mOperateStatus = WiFiOperateStatus.CONNECT_FAIL;
                    waitForConnectSSID = null;

                    //手动关闭连接
                    WiFiModule.getInstance().closeAllConnect();
                    break;

                default:
                    break;
            }

            return true;
        }
    }

}
