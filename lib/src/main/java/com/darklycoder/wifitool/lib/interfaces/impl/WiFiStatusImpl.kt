package com.darklycoder.wifitool.lib.interfaces.impl

import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import com.darklycoder.wifitool.lib.info.action.*
import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener
import com.darklycoder.wifitool.lib.interfaces.WiFiSupportListener
import com.darklycoder.wifitool.lib.type.Types
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils

/**
 * wifi状态处理
 */
class WiFiStatusImpl(private val mSupportListener: WiFiSupportListener) : WiFiStatusListener {

    override fun handleScanResultsChanged(intent: Intent) {
        val action = mSupportListener.currentAction
        if (action is WiFiScanAction) {
            // 扫描结束
            WiFiLogUtils.d("扫描结束，$action")
            mSupportListener.doneScanAction(action)
            return
        }

        // WiFi列表发生变动
        mSupportListener.onWiFiListChange()
    }

    override fun handleWiFiStateChanged(intent: Intent) {
        val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
        WiFiLogUtils.d("WiFiStateChanged-> $state")

        when (state) {
            WifiManager.WIFI_STATE_DISABLED -> {
                // WIFI处于关闭状态
                WiFiLogUtils.d("WIFI已关闭")

                val action = mSupportListener.currentAction
                if (action is WiFiDisableAction) {
                    action.end()
                }

                mSupportListener.onWiFiClose()
            }

            WifiManager.WIFI_STATE_DISABLING -> {
                // 正在关闭
                WiFiLogUtils.d("WIFI关闭中")
            }

            WifiManager.WIFI_STATE_ENABLED -> {
                // 已经打开
                WiFiLogUtils.d("WIFI已打开")

                // 判断当前执行的action是否是WiFiScanAction
                val action = mSupportListener.currentAction
                if (action is WiFiEnableAction) {
                    action.end()
                }
            }

            WifiManager.WIFI_STATE_ENABLING -> {
                // 正在打开
                WiFiLogUtils.d("打开WIFI中...")
            }

            else -> {
            }
        }
    }

    override fun handleNetStateChanged(intent: Intent) {
        val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
        val state = info?.detailedState

        WiFiLogUtils.d("NetStateChanged-> $state")

        if (NetworkInfo.DetailedState.CONNECTED == state) {
            val wifiInfo = mSupportListener.connectedWifiInfo
            if (null == wifiInfo) {
                WiFiLogUtils.d("当前连接的 WifiInfo 为空")
                return
            }

            // 去掉双引号
            var SSID = wifiInfo.ssid
            val size = SSID.length
            SSID = SSID.substring(1, size - 1)

            // 连接成功
            val action = mSupportListener.currentAction

            if (action is WiFiConnectAction) {
                if (action.SSID != SSID) {
                    WiFiLogUtils.d("当前" + action.SSID + "与" + SSID + "不一致！")

                    mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.NOT_MATCH)

                    return
                }

                action.listener?.onResult(Types.ConnectResultType.SUCCESS)

                mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.NORMAL)

                WiFiLogUtils.d("WiFi连接成功，$action")
                action.end()

                return
            }

            WiFiLogUtils.d("WiFi连接成功，$SSID")

            mSupportListener.doneConnectSuccess(SSID, Types.ConnectSuccessType.SYSTEM)
        }
    }

    override fun handleSupplicantStateChanged(intent: Intent) {
        val state = intent.getParcelableExtra<SupplicantState>(WifiManager.EXTRA_NEW_STATE)
        val errorResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1)

        WiFiLogUtils.d("SupplicantStateChanged-> $state | $errorResult")

        if (SupplicantState.DISCONNECTED == state && WifiManager.ERROR_AUTHENTICATING == errorResult) {
            // 密码错误
            val action = mSupportListener.currentAction

            if (action is WiFiConnectAction) {

                if (Types.ActionStateType.END == action.actionState) {
                    return
                }

                if (action is WiFiDirectConnectAction) {
                    action.listener?.onResult(Types.ConnectResultType.DIRECT_PASSWORD_ERROR)

                } else {
                    action.listener?.onResult(Types.ConnectResultType.PASSWORD_ERROR)
                }

                mSupportListener.doneConnectFail(action)
                WiFiLogUtils.d("WiFi连接失败，密码错误，$action")
                action.end()
            }
        }
    }

}
