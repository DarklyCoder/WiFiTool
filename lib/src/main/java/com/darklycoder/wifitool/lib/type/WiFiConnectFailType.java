package com.darklycoder.wifitool.lib.type;

public enum WiFiConnectFailType {

    PASSWORD_ERROR,//密码错误
    DIRECT_PASSWORD_ERROR,//直接连接，密码错误
    TIMEOUT_ERROR,//连接超时
    SYSTEM_LIMIT_ERROR,//系统限制
    NORMAL_CLOSE,//正常关闭
}
