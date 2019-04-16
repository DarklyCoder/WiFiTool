package com.darklycoder.wifitool.lib.type;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Types {

    @IntDef({
            ScanResultType.UNKNOWN,
            ScanResultType.FREQUENTLY_SCAN_ERROR,
            ScanResultType.OPEN_WIFI_ERROR,
            ScanResultType.SUCCESS,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanResultType {
        int UNKNOWN = -1;//未知错误
        int FREQUENTLY_SCAN_ERROR = 0;//频繁扫描
        int OPEN_WIFI_ERROR = 1;//开启WiFi失败
        int SUCCESS = 2;//扫描成功
    }

    @IntDef({
            RemoveResultType.SYSTEM_LIMIT_ERROR,
            RemoveResultType.SUCCESS,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemoveResultType {
        int SYSTEM_LIMIT_ERROR = 0;//系统限制，删除失败
        int SUCCESS = 1;//删除成功
    }

    @IntDef({
            ConnectResultType.UNKNOWN,
            ConnectResultType.SYSTEM_LIMIT_ERROR,
            ConnectResultType.TIMEOUT_ERROR,
            ConnectResultType.PASSWORD_ERROR,
            ConnectResultType.DIRECT_PASSWORD_ERROR,
            ConnectResultType.SUCCESS,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectResultType {
        int UNKNOWN = -1;//未知错误
        int SYSTEM_LIMIT_ERROR = 0;//系统限制，删除失败
        int TIMEOUT_ERROR = 1;//连接超时
        int PASSWORD_ERROR = 2;//密码错误
        int DIRECT_PASSWORD_ERROR = 3;//直连密码错误
        int SUCCESS = 5;//连接成功
    }

    @IntDef({
            ActionStateType.WAITING,
            ActionStateType.PROCESS,
            ActionStateType.END,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionStateType {
        int WAITING = 0;//待执行
        int PROCESS = 1;//执行中
        int END = 2;//执行完成
    }


    @IntDef({
            ConnectSuccessType.NOT_MATCH,
            ConnectSuccessType.NORMAL,
            ConnectSuccessType.SYSTEM,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectSuccessType {
        int NOT_MATCH = 1;//不匹配
        int NORMAL = 2;//正常处理
        int SYSTEM = 3;//没有执行操作时连接成功
    }

}
