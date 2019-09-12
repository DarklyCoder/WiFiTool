package com.darklycoder.wifitool

import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.text.TextUtils
import com.darklycoder.wifitool.lib.WiFiModule
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiListenerImpl
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType
import com.darklycoder.wifitool.lib.type.WiFiConnectType
import com.darklycoder.wifitool.lib.type.WiFiGetListType

/**
 * 全局事件监听demo
 */
class GlobalMonitorActivity : BaseMonitorActivity() {

    private val mListener = object : WiFiListenerImpl() {

        override fun onStartScan() {
            mTvStatus?.text = "扫描中..."
            mBtnScan?.isEnabled = false
        }

        override fun onCloseWiFi() {
            mTvStatus?.text = "WiFi已关闭"

            mData.clear()
            mAdapter?.notifyDataSetChanged()
        }

        override fun onDataChange(type: WiFiGetListType, list: List<WiFiScanInfo>) {
            if (type === WiFiGetListType.TYPE_SCAN) {
                mBtnScan?.isEnabled = true
                mTvStatus?.text = "扫描结束"
            }

            mData.clear()
            mData.addAll(list)
            mAdapter?.notifyDataSetChanged()
        }

        override fun onWiFiStartConnect(SSID: String) {
            mTvStatus?.text = SSID + "连接中..."
            refreshData(SSID, WiFiConnectType.CONNECTING)

            notifyState(NetworkInfo.DetailedState.CONNECTING)
        }

        override fun onWiFiCreateConfig(SSID: String, configuration: WifiConfiguration) {
            for (info in mData) {
                if (!TextUtils.isEmpty(SSID) && SSID == info.scanResult!!.SSID) {
                    info.configuration = configuration
                    break
                }
            }

            refreshData(SSID, WiFiConnectType.CONNECTING)

            notifyState(NetworkInfo.DetailedState.CONNECTING)
        }

        override fun onWiFiConnected(SSID: String, isInit: Boolean) {
            mTvStatus?.text = isInit.toString() + " || " + SSID + "已连接"
            refreshData(SSID, WiFiConnectType.CONNECTED)

            notifyState(NetworkInfo.DetailedState.CONNECTED)
        }

        override fun onWiFiConnectFail(SSID: String, type: WiFiConnectFailType) {
            if (TextUtils.isEmpty(SSID)) {
                return
            }

            mTvStatus?.text = SSID + "连接失败，" + type.name
            refreshData(SSID, WiFiConnectType.DISCONNECTED)

            notifyState(NetworkInfo.DetailedState.DISCONNECTED)

            if (type === WiFiConnectFailType.DIRECT_PASSWORD_ERROR) {
                //直连密码错误，提示用户修改密码
                val scanInfo = findScanInfo(SSID)
                if (null != scanInfo) {
                    showInputDialog(scanInfo, 1)
                }
            }
        }

        override fun onWiFiRemoveResult(info: WiFiRemoveStatusInfo) {
            if (!info.isSuccess) {
                mTvStatus?.text = info.SSID!! + "删除失败！"
                showDelErrorDialog()

                return
            }

            mTvStatus?.text = info.SSID!! + "删除成功！"
            for (connectInfo in mData) {
                if (!TextUtils.isEmpty(info.SSID) && info.SSID == connectInfo.scanResult!!.SSID) {
                    connectInfo.configuration = null
                    break
                }
            }
            refreshData(info.SSID, WiFiConnectType.DISCONNECTED)
        }
    }

    override fun initParams() {
        super.initParams()

        // 添加监听
        WiFiModule.addWiFiListener(TAG, mListener)
    }

    override fun onDestroy() {
        WiFiModule.removeWiFiListener(TAG)
        super.onDestroy()
    }

}
