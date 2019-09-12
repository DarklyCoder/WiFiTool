package com.darklycoder.wifitool.lib.type

/**
 * WiFi连接状态
 */
enum class WiFiConnectType private constructor(var type: Int, var state: String) {

    DISCONNECTED(0, "未连接"),
    CONNECTING(1, "连接中"),
    CONNECTED(2, "已连接")

}
