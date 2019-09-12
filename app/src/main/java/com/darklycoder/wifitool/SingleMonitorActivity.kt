package com.darklycoder.wifitool

import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.text.TextUtils
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener
import com.darklycoder.wifitool.lib.type.Types
import com.darklycoder.wifitool.lib.type.Types.ConnectResultType.Companion.DIRECT_PASSWORD_ERROR
import com.darklycoder.wifitool.lib.type.Types.ConnectResultType.Companion.SUCCESS
import com.darklycoder.wifitool.lib.type.Types.RemoveResultType.Companion.SYSTEM_LIMIT_ERROR
import com.darklycoder.wifitool.lib.type.WiFiConnectType

/**
 * 单个事件监听demo
 */
class SingleMonitorActivity : BaseMonitorActivity() {

    override fun getScanActionListener(): ScanWiFiActionListener? {
        return object : ScanWiFiActionListener {

            override fun onStart() {
                mTvStatus?.text = "扫描中..."
                mBtnScan?.isEnabled = false
            }

            override fun onResult(type: Int, list: List<WiFiScanInfo>?) {
                if (type == Types.ScanResultType.SUCCESS) {
                    mBtnScan?.isEnabled = true
                    mTvStatus?.text = "扫描结束"
                }

                if (null != list) {
                    mData.clear()
                    mData.addAll(list)
                    mAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun getConnectActionListener(info: WiFiScanInfo): ConnectWiFiActionListener? {
        return object : ConnectWiFiActionListener {

            override fun onStart() {
                mTvStatus?.text = "${info.scanResult?.SSID} 连接中..."
                refreshData(info.scanResult?.SSID, WiFiConnectType.CONNECTING)

                notifyState(NetworkInfo.DetailedState.CONNECTING)
            }

            override fun onCreateConfig(configuration: WifiConfiguration) {
                for (data in mData) {
                    if (!TextUtils.isEmpty(info.scanResult?.SSID) && data.scanResult?.SSID == info.scanResult?.SSID) {
                        data.configuration = configuration
                        break
                    }
                }

                refreshData(info.scanResult?.SSID, WiFiConnectType.CONNECTING)

                notifyState(NetworkInfo.DetailedState.CONNECTING)
            }

            override fun onResult(type: Int) {
                if (SUCCESS == type) {
                    mTvStatus?.text = "${info.scanResult?.SSID} 已连接"
                    refreshData(info.scanResult!!.SSID, WiFiConnectType.CONNECTED)

                    notifyState(NetworkInfo.DetailedState.CONNECTED)

                    return
                }

                mTvStatus?.text = "${info.scanResult?.SSID} 连接失败，$type"
                refreshData(info.scanResult?.SSID, WiFiConnectType.DISCONNECTED)

                notifyState(NetworkInfo.DetailedState.DISCONNECTED)

                if (DIRECT_PASSWORD_ERROR == type) {
                    showInputDialog(info, 1)
                }
            }
        }
    }

    override fun getRemoveActionListener(SSID: String): RemoveWiFiActionListener? {
        return object : RemoveWiFiActionListener {
            override fun onStart() {

            }

            override fun onResult(type: Int) {
                if (SYSTEM_LIMIT_ERROR == type) {
                    mTvStatus?.text = "$SSID 删除失败！"
                    showDelErrorDialog()

                    return
                }

                mTvStatus?.text = "$SSID 删除成功！"
                for (connectInfo in mData) {
                    if (!TextUtils.isEmpty(SSID) && SSID == connectInfo.scanResult?.SSID) {
                        connectInfo.configuration = null
                        break
                    }
                }
                refreshData(SSID, WiFiConnectType.DISCONNECTED)
            }
        }
    }

}
