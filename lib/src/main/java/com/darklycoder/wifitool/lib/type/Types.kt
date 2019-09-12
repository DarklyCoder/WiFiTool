package com.darklycoder.wifitool.lib.type

import androidx.annotation.IntDef

class Types {

    @IntDef(ScanResultType.UNKNOWN, ScanResultType.FREQUENTLY_SCAN_ERROR, ScanResultType.OPEN_WIFI_ERROR, ScanResultType.SUCCESS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ScanResultType {
        companion object {
            const val UNKNOWN = -1//未知错误
            const val FREQUENTLY_SCAN_ERROR = 0//频繁扫描
            const val OPEN_WIFI_ERROR = 1//开启WiFi失败
            const val SUCCESS = 2//扫描成功
        }
    }

    @IntDef(RemoveResultType.SYSTEM_LIMIT_ERROR, RemoveResultType.SUCCESS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class RemoveResultType {
        companion object {
            const val SYSTEM_LIMIT_ERROR = 0//系统限制，删除失败
            const val SUCCESS = 1//删除成功
        }
    }

    @IntDef(ConnectResultType.UNKNOWN, ConnectResultType.SYSTEM_LIMIT_ERROR, ConnectResultType.TIMEOUT_ERROR, ConnectResultType.PASSWORD_ERROR, ConnectResultType.DIRECT_PASSWORD_ERROR, ConnectResultType.SUCCESS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ConnectResultType {
        companion object {
            const val UNKNOWN = -1//未知错误
            const val SYSTEM_LIMIT_ERROR = 0//系统限制，删除失败
            const val TIMEOUT_ERROR = 1//连接超时
            const val PASSWORD_ERROR = 2//密码错误
            const val DIRECT_PASSWORD_ERROR = 3//直连密码错误
            const val SUCCESS = 5//连接成功
        }
    }

    @IntDef(ActionStateType.WAITING, ActionStateType.PROCESS, ActionStateType.END)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ActionStateType {
        companion object {
            const val WAITING = 0//待执行
            const val PROCESS = 1//执行中
            const val END = 2//执行完成
        }
    }


    @IntDef(ConnectSuccessType.NOT_MATCH, ConnectSuccessType.NORMAL, ConnectSuccessType.SYSTEM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ConnectSuccessType {
        companion object {
            const val NOT_MATCH = 1//不匹配
            const val NORMAL = 2//正常处理
            const val SYSTEM = 3//没有执行操作时连接成功
        }
    }

}
