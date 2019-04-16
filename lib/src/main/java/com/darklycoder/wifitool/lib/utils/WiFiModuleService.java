package com.darklycoder.wifitool.lib.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.info.action.IWiFiAction;
import com.darklycoder.wifitool.lib.info.action.WiFiConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiDirectConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiDisableAction;
import com.darklycoder.wifitool.lib.info.action.WiFiEnableAction;
import com.darklycoder.wifitool.lib.info.action.WiFiNormalConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiRemoveAction;
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.interfaces.WiFiSupportListener;
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiStatusImpl;
import com.darklycoder.wifitool.lib.receiver.WiFiStatusReceiver;
import com.darklycoder.wifitool.lib.type.Types;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiGetListType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理WiFiAction
 */
public class WiFiModuleService {

    private WeakReference<Context> mContext;
    private WifiManager mWifiManager;
    private final List<IWiFiAction> actionList = Collections.synchronizedList(new ArrayList<>());
    private final HashMap<String, WiFiListener> mListeners = new HashMap<>();//存放WiFi状态监听回调
    private boolean stopFlag = false;
    private Handler mHandler;
    private WiFiStatusReceiver mStatusReceiver;
    private WiFiStatusImpl mWiFiStatusImpl;

    public WiFiModuleService(Context context) {
        this.mContext = new WeakReference<>(context);
        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.mHandler = new Handler(Looper.getMainLooper());
        this.mWiFiStatusImpl = new WiFiStatusImpl(getWiFiSupportListener());

        registerReceiver();

        new WorkThread(mHandler).start();
    }

    /**
     * 注册监听广播
     */
    private void registerReceiver() {
        try {
            this.mStatusReceiver = new WiFiStatusReceiver(mWiFiStatusImpl);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//监听wifi连接失败
            this.mContext.get().registerReceiver(this.mStatusReceiver, intentFilter);

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    /**
     * 取消广播注册
     */
    private void unregisterReceiver() {
        try {
            if (null != this.mContext.get()) {
                this.mContext.get().unregisterReceiver(this.mStatusReceiver);
            }

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    /**
     * 添加操作
     */
    public void addAction(IWiFiAction action) {
        synchronized (actionList) {
            boolean absent = !actionList.contains(action);
            if (absent) {
                actionList.add(action);
                WiFiLogUtils.d("已加入待执行队列中，" + action.toString());
            }
        }
    }

    /**
     * 添加WiFi状态监听
     *
     * @param key      唯一标识
     * @param listener 监听回调
     */
    public void addWiFiListener(String key, WiFiListener listener) {
        try {
            if (mListeners.containsKey(key)) {
                return;
            }

            mListeners.put(key, listener);

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    /**
     * 移除WiFi状态监听
     *
     * @param key 唯一标识
     */
    public void removeWiFiListener(String key) {
        try {
            if (!mListeners.containsKey(key)) {
                return;
            }

            mListeners.remove(key);

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    private WiFiSupportListener getWiFiSupportListener() {
        return new WiFiSupportListener() {

            @Nullable
            @Override
            public IWiFiAction getCurrentAction() {
                synchronized (actionList) {
                    for (IWiFiAction action : actionList) {
                        if (Types.ActionStateType.PROCESS == action.getActionState()) {
                            return action;
                        }
                    }

                    return null;
                }
            }

            @Override
            public WifiInfo getConnectedWifiInfo() {
                return WiFiUtils.getConnectedWifiInfo(mWifiManager);
            }

            @Override
            public void onWiFiClose() {
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onCloseWiFi();
                }
            }

            @Override
            public void onWiFiListChange() {
                //TODO 在子线程查询
                List<WiFiScanInfo> list = WiFiUtils.getScanList(mWifiManager);

                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onDataChange(WiFiGetListType.TYPE_SORT, list);
                }
            }

            @Override
            public void doneScanAction(@NonNull WiFiScanAction action) {
                //TODO 在子线程查询
                List<WiFiScanInfo> list = WiFiUtils.getScanList(mWifiManager);

                if (null != action.listener) {
                    action.listener.onResult(Types.ScanResultType.SUCCESS, list);
                }

                action.end();

                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onDataChange(WiFiGetListType.TYPE_SCAN, list);
                }
            }

            @Override
            public void doneConnectSuccess(@NonNull String SSID, int type) {
                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onWiFiConnected(SSID, Types.ConnectSuccessType.SYSTEM == type);
                }
            }

            @Override
            public void doneConnectFail(@NonNull WiFiConnectAction action) {
                if (action instanceof WiFiDirectConnectAction) {
                    // 回调全局监听
                    for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                        entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.DIRECT_PASSWORD_ERROR);
                    }

                    return;
                }

                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.PASSWORD_ERROR);
                }
            }
        };
    }

    private class WorkThread extends Thread {

        Handler mHandler;

        WorkThread(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void run() {
            super.run();

            while (!stopFlag) {
                synchronized (actionList) {
                    if (actionList.isEmpty()) {
                        continue;
                    }

                    //始终获取第一个操作
                    IWiFiAction action = actionList.get(0);

                    if (Types.ActionStateType.WAITING == action.getActionState()) {
                        dispatchAction(action, mHandler);
                        continue;
                    }

                    if (Types.ActionStateType.END == action.getActionState()) {
                        actionList.remove(action);
                        WiFiLogUtils.d("执行完毕，移除，" + action.toString());
                    }
                }
            }
        }
    }

    /**
     * 分发WiFi操作事件
     */
    private void dispatchAction(IWiFiAction action, Handler handler) {
        // 检测WiFi是否开启
        boolean isWiFiEnable = WiFiUtils.isWiFiEnable(mWifiManager);

        if (action instanceof WiFiDisableAction) {
            action.setState(Types.ActionStateType.PROCESS);
            WiFiLogUtils.d("开始执行，" + action.toString());

            if (!isWiFiEnable) {
                //不可用
                action.end();
                return;
            }

            //禁用WiFi
            WiFiUtils.setWifiEnabled(mWifiManager, false);
            return;
        }

        if (action instanceof WiFiEnableAction) {
            action.setState(Types.ActionStateType.PROCESS);
            WiFiLogUtils.d("开始执行，" + action.toString());

            if (isWiFiEnable) {
                //可用
                action.end();
                return;
            }

            //启用WiFi
            WiFiUtils.setWifiEnabled(mWifiManager, true);
            return;
        }

        if (!isWiFiEnable) {
            //插入打开WiFi事件，阻塞后续WiFi操作
            insertOpenWiFiAction();
            return;
        }

        action.setState(Types.ActionStateType.PROCESS);
        WiFiLogUtils.d("开始执行，" + action.toString());

        if (action instanceof WiFiScanAction) {
            handleWiFiScanAction((WiFiScanAction) action, handler);

        } else if (action instanceof WiFiNormalConnectAction) {
            handleWiFiNormalConnectAction((WiFiNormalConnectAction) action, handler);

        } else if (action instanceof WiFiDirectConnectAction) {
            handleWiFiDirectConnectAction((WiFiDirectConnectAction) action, handler);

        } else if (action instanceof WiFiRemoveAction) {
            handleWiFiRemoveAction((WiFiRemoveAction) action, handler);

        } else {
            WiFiLogUtils.d("不支持此操作，" + action.toString());
            action.end();
        }
    }

    /**
     * 插入打开WiFi事件
     */
    private void insertOpenWiFiAction() {
        synchronized (actionList) {
            WiFiEnableAction openAction = new WiFiEnableAction();

            if (actionList.isEmpty()) {
                actionList.add(0, openAction);

                return;
            }

            IWiFiAction action = actionList.get(0);
            if (action instanceof WiFiEnableAction) {
                return;
            }

            actionList.add(0, openAction);
        }
    }

    private void handleWiFiScanAction(final WiFiScanAction action, Handler handler) {
        handler.post(() -> {
            if (null != action.listener) {
                action.listener.onStart();
            }

            // 回调全局监听
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onStartScan();
            }
        });

        boolean success = WiFiUtils.startScan(mWifiManager);
        if (!success) {
            final List<WiFiScanInfo> list = WiFiUtils.getScanList(mWifiManager);

            handler.post(() -> {
                if (null != action.listener) {
                    action.listener.onResult(Types.ScanResultType.FREQUENTLY_SCAN_ERROR, list);
                }

                action.end();

                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onDataChange(WiFiGetListType.TYPE_SCAN, list);
                }
            });
        }
    }

    private void handleWiFiNormalConnectAction(final WiFiNormalConnectAction action, Handler handler) {
        handler.post(() -> {
            if (null != action.listener) {
                action.listener.onStart();
            }

            // 回调全局监听
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiStartConnect(action.SSID);
            }
        });

        startDelayCheck(action);

        final WiFiCreateConfigStatusInfo statusInfo = WiFiUtils.connectWiFi(mWifiManager, action.SSID,
                action.cipherType, action.password, mContext.get());

        if (!statusInfo.isSuccess()) {
            handler.post(() -> {
                WiFiLogUtils.d("配置创建失败，" + action.toString());

                if (null != action.listener) {
                    action.listener.onResult(Types.ConnectResultType.SYSTEM_LIMIT_ERROR);
                }

                action.end();

                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.SYSTEM_LIMIT_ERROR);
                }
            });

            return;
        }

        //配置创建成功
        handler.post(() -> {
            WiFiLogUtils.d("配置创建成功，" + action.toString());

            if (null != action.listener) {
                action.listener.onCreateConfig(statusInfo.configuration);
            }

            // 回调全局监听
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiCreateConfig(action.SSID, statusInfo.configuration);
            }

            //连接WiFi
            boolean success = WiFiUtils.enableNetwork(mWifiManager, statusInfo.configuration.networkId);

            if (!success) {
                //连接失败
                WiFiLogUtils.d("连接WiFi失败，" + action.toString());

                if (null != action.listener) {
                    action.listener.onResult(Types.ConnectResultType.UNKNOWN);
                }

                action.end();

                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.UNKNOWN);
                }
            }
        });
    }

    private void handleWiFiDirectConnectAction(final WiFiDirectConnectAction action, Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != action.listener) {
                    action.listener.onStart();
                }

                // 回调全局监听
                for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                    entry.getValue().onWiFiStartConnect(action.SSID);
                }
            }
        });

        startDelayCheck(action);

        WiFiUtils.closeAllConnect(mWifiManager);
        boolean success = WiFiUtils.enableNetwork(mWifiManager, action.configuration.networkId);

        if (!success) {
            //连接失败
            handler.post(new Runnable() {
                @Override
                public void run() {
                    WiFiLogUtils.d("直连WiFi失败，" + action.toString());

                    if (null != action.listener) {
                        action.listener.onResult(Types.ConnectResultType.UNKNOWN);
                    }

                    action.end();

                    // 回调全局监听
                    for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                        entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.UNKNOWN);
                    }
                }
            });
        }
    }

    private void handleWiFiRemoveAction(final WiFiRemoveAction action, Handler handler) {
        handler.post(() -> {
            if (null != action.listener) {
                action.listener.onStart();
            }
        });

        final WiFiRemoveStatusInfo statusInfo = WiFiUtils.removeWiFi(mWifiManager, action.SSID, mContext.get());

        handler.post(() -> {
            WiFiLogUtils.d("删除WiFi " + statusInfo.isSuccess + " | " + action.toString());

            if (null != action.listener) {
                if (statusInfo.isSuccess) {
                    action.listener.onResult(Types.RemoveResultType.SUCCESS);

                } else {
                    action.listener.onResult(Types.RemoveResultType.SYSTEM_LIMIT_ERROR);
                }
            }
            action.end();

            // 回调全局监听
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiRemoveResult(statusInfo);
            }
        });
    }

    /**
     * 开始超时检测
     */
    private void startDelayCheck(final WiFiConnectAction action) {
        if (action.timeout <= 1000 * 3) {
            WiFiLogUtils.d("超时时间设置小于3秒，不予超时检测，" + action.toString());
            return;
        }

        action.startDelayCheck(mHandler, () -> {
            if (Types.ActionStateType.END == action.getActionState()) {
                WiFiLogUtils.d("已经结束掉了，忽略连接WiFi超时，" + action.toString());
                return;
            }

            WiFiLogUtils.d("连接WiFi超时，" + action.toString());
            WiFiUtils.closeAllConnect(mWifiManager);

            if (null != action.listener) {
                action.listener.onResult(Types.ConnectResultType.TIMEOUT_ERROR);
            }
            action.end();

            // 回调全局监听
            for (Map.Entry<String, WiFiListener> entry : mListeners.entrySet()) {
                entry.getValue().onWiFiConnectFail(action.SSID, WiFiConnectFailType.TIMEOUT_ERROR);
            }
        });
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        try {
            stopFlag = true;
            unregisterReceiver();
            mHandler.removeCallbacksAndMessages(null);

            synchronized (actionList) {
                actionList.clear();
            }

            mWifiManager = null;

            WiFiLogUtils.d("销毁资源结束");

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

}
