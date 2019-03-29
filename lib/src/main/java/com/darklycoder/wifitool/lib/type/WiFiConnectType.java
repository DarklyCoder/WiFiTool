package com.darklycoder.wifitool.lib.type;

/**
 * WiFi连接状态
 */
public enum WiFiConnectType {

    DISCONNECTED(0, "未连接"),
    CONNECTING(1, "连接中"),
    CONNECTED(2, "已连接");

    public int type;
    public String state;

    WiFiConnectType(int type, String state) {
        this.type = type;
        this.state = state;
    }

}
