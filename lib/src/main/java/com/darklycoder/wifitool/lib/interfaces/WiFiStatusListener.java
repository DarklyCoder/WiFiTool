package com.darklycoder.wifitool.lib.interfaces;

import android.content.Intent;

import com.darklycoder.wifitool.lib.WiFiConfig;
import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.WiFGetListType;
import com.darklycoder.wifitool.lib.type.WiFiOperateStatus;

import java.util.List;

/**
 * WiFi状态回调
 */
public interface WiFiStatusListener {

    /**
     * 处理扫描列表
     */
    void handleScanResultsChanged(Intent intent);

    /**
     * 处理WiFi状态
     */
    void handleWiFiStateChanged(Intent intent);

    /**
     * 处理网络状态
     */
    void handleNetStateChanged(Intent intent);

    /**
     * 处理WiFi状态(密码错误回调)
     */
    void handleSupplicantStateChanged(Intent intent);

    /**
     * 通知准备扫描
     */
    void notifyStartScan();

    /**
     * 通知开始准备连接
     *
     * @param connectType 1：表示直接连接
     */
    void notifyStartConnect(String SSID, WiFiConfig config, int connectType);

    /**
     * 通知开始准备连接状态
     */
    void notifyStartConnectStatus(WiFiCreateConfigStatusInfo info);

    /**
     * 通知WiFi列表数据变化
     */
    void notifyWiFiList(WiFGetListType type, List<WiFiScanInfo> list);

    /**
     * 通知移除WiFi状态
     */
    void notifyRemoveStatus(WiFiRemoveStatusInfo info);

    /**
     * 获取WiFi操作状态
     */
    WiFiOperateStatus getWiFiOperateStatus();

    /**
     * 释放资源
     */
    void destroy();

}
