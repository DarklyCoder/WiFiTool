package com.darklycoder.wifitool.lib.interfaces;

import android.net.wifi.WifiConfiguration;

import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiGetListType;

import java.util.List;

/**
 * WiFi全局相关状态监听
 */
public interface WiFiListener {

    /**
     * 开始扫描WiFi
     */
    void onStartScan();

    /**
     * 通知WiFi关闭
     */
    void onCloseWiFi();

    /**
     * WiFi数据更新
     */
    void onDataChange(WiFiGetListType type, List<WiFiScanInfo> list);

    /**
     * 开始连接WiFi
     */
    void onWiFiStartConnect(String SSID);

    /**
     * 创建WiFi配置
     */
    void onWiFiCreateConfig(String SSID, WifiConfiguration configuration);

    /**
     * WiFi连接成功
     *
     * @param isInit 标识是否是初始连接成功
     */
    void onWiFiConnected(String SSID, boolean isInit);

    /**
     * WiFi连接失败
     *
     * @param type 失败类型
     */
    void onWiFiConnectFail(String SSID, WiFiConnectFailType type);

    /**
     * 删除WiFi状态
     */
    void onWiFiRemoveResult(WiFiRemoveStatusInfo info);

}
