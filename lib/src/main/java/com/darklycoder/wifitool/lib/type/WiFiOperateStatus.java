package com.darklycoder.wifitool.lib.type;

/**
 * WiFi操作状态
 */
public enum WiFiOperateStatus {

    IDLE(0, "空闲状态"),
    OPENING(1, "打开WiFi中"),
    OPENED(2, "已打开WiFi"),
    CLOSING(3, "关闭WiFi中"),
    CLOSED(4, "已关闭WiFi"),
    SCANNING(5, "扫描WiFi列表中"),
    SCANNED(6, "扫描结束"),
    CONNECTING(7, "连接WiFi中"),
    CONNECTED(8, "已连接到WiFi"),
    CONNECT_FAIL(9, "连接WiFi失败");

    public int type;
    public String state;

    WiFiOperateStatus(int type, String state) {
        this.type = type;
        this.state = state;
    }

}
