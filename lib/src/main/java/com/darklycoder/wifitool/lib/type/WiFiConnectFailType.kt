package com.darklycoder.wifitool.lib.type

enum class WiFiConnectFailType {

    PASSWORD_ERROR, //密码错误
    DIRECT_PASSWORD_ERROR, //直接连接，密码错误
    TIMEOUT_ERROR, //连接超时
    SYSTEM_LIMIT_ERROR, //系统限制
    UNKNOWN
    //未知
}
