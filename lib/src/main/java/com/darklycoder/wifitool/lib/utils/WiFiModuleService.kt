package com.darklycoder.wifitool.lib.utils

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import com.darklycoder.wifitool.lib.info.action.*
import com.darklycoder.wifitool.lib.interfaces.WiFiListener
import com.darklycoder.wifitool.lib.interfaces.WiFiSupportListener
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiStatusImpl
import com.darklycoder.wifitool.lib.receiver.WiFiStatusReceiver
import com.darklycoder.wifitool.lib.type.Types
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType
import com.darklycoder.wifitool.lib.type.WiFiGetListType
import java.lang.ref.WeakReference
import java.util.*

/**
 * 处理WiFiAction
 */
class WiFiModuleService(context: Context) {

    private val mContext: WeakReference<Context> = WeakReference(context.applicationContext)
    private var mWifiManager: WifiManager? = null
    private val actionList = Collections.synchronizedList(ArrayList<IWiFiAction>())
    private val mListeners = HashMap<String, WiFiListener>() // 存放WiFi状态监听回调
    private var stopFlag = false
    private val mHandler: Handler // 主线程
    private var mStatusReceiver: WiFiStatusReceiver? = null
    private val mWiFiStatusImpl: WiFiStatusImpl

    init {
        this.mWifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        this.mHandler = Handler(Looper.getMainLooper())
        this.mWiFiStatusImpl = WiFiStatusImpl(wiFiSupportListener)

        registerReceiver()

        WorkThread(mHandler).start()
    }

    private val wiFiSupportListener: WiFiSupportListener
        get() = object : WiFiSupportListener {

            override val currentAction: IWiFiAction?
                get() {
                    synchronized(actionList) {
                        for (action in actionList) {
                            if (Types.ActionStateType.PROCESS == action.actionState) {
                                return action
                            }
                        }

                        return null
                    }
                }

            override val connectedWifiInfo: WifiInfo?
                get() = WiFiUtils.getConnectedWifiInfo(mWifiManager)

            override fun onWiFiClose() {
                for ((_, value) in mListeners) {
                    value.onCloseWiFi()
                }
            }

            override fun onWiFiListChange() {
                val list = WiFiUtils.getScanList(mWifiManager)

                for ((_, value) in mListeners) {
                    value.onDataChange(WiFiGetListType.TYPE_SORT, list)
                }
            }

            override fun doneScanAction(action: WiFiScanAction) {
                val list = WiFiUtils.getScanList(mWifiManager)

                action.listener?.onResult(Types.ScanResultType.SUCCESS, list)

                action.end()

                for ((_, value) in mListeners) {
                    value.onDataChange(WiFiGetListType.TYPE_SCAN, list)
                }
            }

            override fun doneConnectSuccess(SSID: String, type: Int) {
                for ((_, value) in mListeners) {
                    value.onWiFiConnected(SSID, Types.ConnectSuccessType.SYSTEM == type)
                }
            }

            override fun doneConnectFail(action: WiFiConnectAction) {
                if (action is WiFiDirectConnectAction) {
                    for ((_, value) in mListeners) {
                        value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.DIRECT_PASSWORD_ERROR)
                    }

                    return
                }
                for ((_, value) in mListeners) {
                    value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.PASSWORD_ERROR)
                }
            }
        }

    /**
     * 注册监听广播
     */
    private fun registerReceiver() {
        try {
            this.mStatusReceiver = WiFiStatusReceiver(mWiFiStatusImpl)

            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)//监听wifi列表变化（开启一个热点或者关闭一个热点）
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)//监听wifi是开关变化的状态
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)//监听wifi连接状态广播,是否连接了一个有效路由
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)//监听wifi连接失败

            this.mContext.get()?.registerReceiver(this.mStatusReceiver, intentFilter)

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    /**
     * 取消广播注册
     */
    private fun unregisterReceiver() {
        try {
            this.mContext.get()?.unregisterReceiver(this.mStatusReceiver)

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    /**
     * 添加操作
     */
    fun addAction(action: IWiFiAction) {
        synchronized(actionList) {
            val absent = !actionList.contains(action)
            if (absent) {
                actionList.add(action)
                WiFiLogUtils.d("已加入待执行队列中，$action")
            }
        }
    }

    /**
     * 添加WiFi状态监听
     *
     * @param key      唯一标识
     * @param listener 监听回调
     */
    fun addWiFiListener(key: String, listener: WiFiListener) {
        try {
            if (mListeners.containsKey(key)) {
                return
            }

            mListeners[key] = listener

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    /**
     * 移除WiFi状态监听
     *
     * @param key 唯一标识
     */
    fun removeWiFiListener(key: String) {
        try {
            if (!mListeners.containsKey(key)) {
                return
            }

            mListeners.remove(key)

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    /**
     * 销毁资源
     */
    fun destroy() {
        try {
            stopFlag = true
            unregisterReceiver()
            mHandler.removeCallbacksAndMessages(null)

            synchronized(actionList) {
                actionList.clear()
            }
            mListeners.clear()

            mWifiManager = null

            WiFiLogUtils.d("销毁资源结束")

        } catch (e: Exception) {
            WiFiLogUtils.e(e)
        }
    }

    private inner class WorkThread internal constructor(internal var mHandler: Handler) : Thread() {

        override fun run() {
            super.run()
            while (!stopFlag) {
                synchronized(actionList) {
                    if (actionList.isNotEmpty()) {
                        // 始终获取第一个操作
                        val action = actionList[0]

                        if (Types.ActionStateType.WAITING == action.actionState) {
                            dispatchAction(action, mHandler)

                        } else {
                            if (Types.ActionStateType.END == action.actionState) {
                                actionList.remove(action)
                                WiFiLogUtils.d("执行完毕，移除，$action")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 分发WiFi操作事件
     */
    private fun dispatchAction(action: IWiFiAction, handler: Handler) {
        // 检测WiFi是否开启
        val isWiFiEnable = WiFiUtils.isWiFiEnable(mWifiManager)

        if (action is WiFiDisableAction) {
            action.setState(Types.ActionStateType.PROCESS)
            WiFiLogUtils.d("开始执行，$action")

            if (!isWiFiEnable) {
                // 不可用
                action.end()
                return
            }

            // 禁用WiFi
            WiFiUtils.setWifiEnabled(mWifiManager, false)
            return
        }

        if (action is WiFiEnableAction) {
            action.setState(Types.ActionStateType.PROCESS)
            WiFiLogUtils.d("开始执行，$action")

            if (isWiFiEnable) {
                // 可用
                action.end()
                return
            }

            // 启用WiFi
            WiFiUtils.setWifiEnabled(mWifiManager, true)
            return
        }

        if (!isWiFiEnable) {
            // 插入打开WiFi事件，阻塞后续WiFi操作
            insertOpenWiFiAction()
            return
        }

        action.setState(Types.ActionStateType.PROCESS)
        WiFiLogUtils.d("开始执行，$action")

        when (action) {
            is WiFiScanAction -> handleWiFiScanAction(action, handler)
            is WiFiNormalConnectAction -> handleWiFiNormalConnectAction(action, handler)
            is WiFiDirectConnectAction -> handleWiFiDirectConnectAction(action, handler)
            is WiFiRemoveAction -> handleWiFiRemoveAction(action, handler)
            else -> {
                WiFiLogUtils.d("不支持此操作，$action")
                action.end()
            }
        }
    }

    /**
     * 插入打开WiFi事件
     */
    private fun insertOpenWiFiAction() {
        synchronized(actionList) {
            val openAction = WiFiEnableAction()

            if (actionList.isEmpty()) {
                actionList.add(0, openAction)

                return
            }

            val action = actionList[0]
            if (action is WiFiEnableAction) {
                return
            }

            actionList.add(0, openAction)
        }
    }

    /**
     * 处理wifi扫描事件
     */
    private fun handleWiFiScanAction(action: WiFiScanAction, handler: Handler) {
        handler.post {
            action.listener?.onStart()

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onStartScan()
            }
        }

        val success = WiFiUtils.startScan(mWifiManager)
        if (!success) {
            val list = WiFiUtils.getScanList(mWifiManager)

            handler.post {
                action.listener?.onResult(Types.ScanResultType.FREQUENTLY_SCAN_ERROR, list)

                action.end()

                // 回调全局监听
                for ((_, value) in mListeners) {
                    value.onDataChange(WiFiGetListType.TYPE_SCAN, list)
                }
            }
        }
    }

    private fun handleWiFiNormalConnectAction(action: WiFiNormalConnectAction, handler: Handler) {
        handler.post {
            action.listener?.onStart()

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onWiFiStartConnect(action.SSID)
            }
        }

        // 超时处理
        startDelayCheck(action, handler)

        val statusInfo = WiFiUtils.connectWiFi(mWifiManager, action.SSID, action.cipherType, action.password, mContext.get())
        val configuration = statusInfo.configuration

        if (null == configuration) {
            handler.post {
                WiFiLogUtils.d("配置创建失败，$action")

                action.listener?.onResult(Types.ConnectResultType.SYSTEM_LIMIT_ERROR)

                action.end()

                // 回调全局监听
                for ((_, value) in mListeners) {
                    value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.SYSTEM_LIMIT_ERROR)
                }
            }

            return
        }

        // 配置创建成功
        handler.post {
            WiFiLogUtils.d("配置创建成功，$action")

            action.listener?.onCreateConfig(configuration)

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onWiFiCreateConfig(action.SSID, configuration)
            }

            // 连接WiFi
            val success = WiFiUtils.enableNetwork(mWifiManager, configuration.networkId)

            if (!success) {
                // 连接失败
                WiFiLogUtils.d("连接WiFi失败，$action")

                action.listener?.onResult(Types.ConnectResultType.UNKNOWN)

                action.end()

                // 回调全局监听
                for ((_, value) in mListeners) {
                    value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.UNKNOWN)
                }
            }
        }
    }

    private fun handleWiFiDirectConnectAction(action: WiFiDirectConnectAction, handler: Handler) {
        handler.post {
            action.listener?.onStart()

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onWiFiStartConnect(action.SSID)
            }
        }

        // 连接超时处理
        startDelayCheck(action, handler)

        WiFiUtils.closeAllConnect(mWifiManager)
        val success = WiFiUtils.enableNetwork(mWifiManager, action.configuration.networkId)

        if (!success) {
            // 连接失败
            handler.post {
                WiFiLogUtils.d("直连WiFi失败，$action")

                action.listener?.onResult(Types.ConnectResultType.UNKNOWN)

                action.end()

                // 回调全局监听
                for ((_, value) in mListeners) {
                    value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.UNKNOWN)
                }
            }
        }
    }

    private fun handleWiFiRemoveAction(action: WiFiRemoveAction, handler: Handler) {
        handler.post {
            action.listener?.onStart()
        }

        val statusInfo = WiFiUtils.removeWiFi(mWifiManager, action.SSID, mContext.get())

        handler.post {
            WiFiLogUtils.d("删除WiFi ${statusInfo.isSuccess} | $action")

            if (statusInfo.isSuccess) {
                action.listener?.onResult(Types.RemoveResultType.SUCCESS)

            } else {
                action.listener?.onResult(Types.RemoveResultType.SYSTEM_LIMIT_ERROR)
            }

            action.end()

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onWiFiRemoveResult(statusInfo)
            }
        }
    }

    /**
     * 开始超时检测
     */
    private fun startDelayCheck(action: WiFiConnectAction, handler: Handler) {
        if (action.timeout <= 1000 * 3) {
            WiFiLogUtils.d("超时时间设置小于3秒，不予超时检测，$action")
            return
        }

        action.startDelayCheck(handler, Runnable {
            if (Types.ActionStateType.END == action.actionState) {
                WiFiLogUtils.d("已经结束掉了，忽略连接WiFi超时，$action")
                return@Runnable
            }

            WiFiLogUtils.d("连接WiFi超时，$action ，关闭wifi连接")
            WiFiUtils.closeAllConnect(mWifiManager)

            action.listener?.onResult(Types.ConnectResultType.TIMEOUT_ERROR)
            action.end()

            // 回调全局监听
            for ((_, value) in mListeners) {
                value.onWiFiConnectFail(action.SSID, WiFiConnectFailType.TIMEOUT_ERROR)
            }
        })
    }

}
