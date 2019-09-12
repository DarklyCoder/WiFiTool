package com.darklycoder.wifitool.lib.info.action

import android.os.Handler
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils
import java.lang.ref.WeakReference

/**
 * 连接WiFi
 */
abstract class WiFiConnectAction internal constructor(
        var SSID: String,
        var listener: ConnectWiFiActionListener? = null

) : IWiFiAction() {

    var timeout = (1000 * 15).toLong()//超时时间，默认15s
    private var mHandler: WeakReference<Handler>? = null
    private var mDelayRunnable: Runnable? = null

    override fun end() {
        super.end()

        stopDelayCheck()
    }

    /**
     * 开始延时检测
     */
    fun startDelayCheck(handler: Handler, runnable: Runnable) {
        this.mHandler = WeakReference(handler)
        this.mDelayRunnable = runnable

        try {
            handler.postDelayed(runnable, this.timeout)
            WiFiLogUtils.d("开启超时检测，" + toString())

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    /**
     * 结束延时检测
     */
    private fun stopDelayCheck() {
        try {
            if (null == this.mDelayRunnable) {
                return
            }

            this.mHandler?.get()?.removeCallbacks(this.mDelayRunnable!!)
            this.mDelayRunnable = null
            WiFiLogUtils.d("超时检测关闭，" + toString())

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

}
