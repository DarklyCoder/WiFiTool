package com.darklycoder.wifitool.lib;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.info.action.WiFiDirectConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiNormalConnectAction;
import com.darklycoder.wifitool.lib.info.action.WiFiRemoveAction;
import com.darklycoder.wifitool.lib.info.action.WiFiScanAction;
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils;
import com.darklycoder.wifitool.lib.utils.WiFiModuleService;

/**
 * WiFi支持类，使用之前先调用{@link  #init}方法初始化
 */
public class WiFiModule {

    private WiFiModuleService mWiFiSupportService;
    private WiFiConfig mWiFiConfig = new WiFiConfig.Builder().build();
    private boolean isInit = false;

    private WiFiModule() {
    }

    private static class WiFiSupportInner {
        private static WiFiModule instance = new WiFiModule();
    }

    public static WiFiModule getInstance() {
        return WiFiModule.WiFiSupportInner.instance;
    }

    /**
     * 设置配置,在{@link #init 之前调用}
     */
    public WiFiModule setConfig(WiFiConfig config) {
        this.mWiFiConfig = config;
        return this;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        if (isInit) {
            return;
        }

        this.mWiFiSupportService = new WiFiModuleService(context);

        WiFiLogUtils.d("初始化");

        isInit = true;
    }

    /**
     * 添加WiFi状态监听
     *
     * @param key      唯一标识
     * @param listener 监听回调
     */
    public void addWiFiListener(String key, WiFiListener listener) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！");
            return;
        }

        mWiFiSupportService.addWiFiListener(key, listener);
    }

    /**
     * 移除WiFi状态监听
     *
     * @param key 唯一标识
     */
    public void removeWiFiListener(String key) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！");
            return;
        }

        mWiFiSupportService.removeWiFiListener(key);
    }

    /**
     * 扫描WiFi
     */
    public void startScan() {
        this.startScan(null);
    }

    /**
     * 扫描WiFi
     */
    public void startScan(@Nullable ScanWiFiActionListener listener) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！");
            return;
        }

        WiFiScanAction action = new WiFiScanAction(listener);
        mWiFiSupportService.addAction(action);
    }

    /**
     * 通过密码连接WiFi
     */
    public void connectWiFi(String SSID, WiFiCipherType type, @Nullable String password) {
        this.connectWiFi(SSID, type, password, null);
    }

    /**
     * 通过密码连接WiFi
     */
    public void connectWiFi(String SSID, WiFiCipherType type, @Nullable String password, @Nullable ConnectWiFiActionListener listener) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！");
            return;
        }

        WiFiNormalConnectAction action = new WiFiNormalConnectAction(SSID, type, password, listener);
        action.timeout = (null == mWiFiConfig) ? -1 : mWiFiConfig.timeOut;
        mWiFiSupportService.addAction(action);
    }

    /**
     * 通过已经存在的配置连接WiFi
     */
    public void connectWiFi(WifiConfiguration configuration) {
        this.connectWiFi(configuration, null);
    }

    /**
     * 通过已经存在的配置连接WiFi
     */
    public void connectWiFi(WifiConfiguration configuration, @Nullable ConnectWiFiActionListener listener) {
        if (null == mWiFiSupportService) {
            return;
        }

        String SSID = configuration.SSID;
        int size = SSID.length();
        SSID = SSID.substring(1, size - 1);

        WiFiDirectConnectAction action = new WiFiDirectConnectAction(SSID, configuration, listener);
        action.timeout = mWiFiConfig.timeOut;
        mWiFiSupportService.addAction(action);
    }

    /**
     * 移除WiFi
     */
    public void removeWiFi(final String SSID) {
        this.removeWiFi(SSID, null);
    }

    /**
     * 移除WiFi
     */
    public void removeWiFi(final String SSID, @Nullable RemoveWiFiActionListener listener) {
        if (null == mWiFiSupportService) {
            WiFiLogUtils.d("请先初始化！");
            return;
        }

        WiFiRemoveAction action = new WiFiRemoveAction(SSID, listener);
        mWiFiSupportService.addAction(action);
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        if (null != mWiFiSupportService) {
            mWiFiSupportService.destroy();
        }

        isInit = false;
        mWiFiSupportService = null;
    }

}
