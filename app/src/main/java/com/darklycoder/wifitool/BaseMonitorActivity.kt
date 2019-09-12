package com.darklycoder.wifitool

import android.content.Intent
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darklycoder.wifitool.lib.WiFiModule
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener
import com.darklycoder.wifitool.lib.type.WiFiCipherType
import com.darklycoder.wifitool.lib.type.WiFiConnectType
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission

abstract class BaseMonitorActivity : AppCompatActivity() {

    var mBtnScan: Button? = null
    var mTvStatus: TextView? = null
    var mListView: ListView? = null

    lateinit var mData: ArrayList<WiFiScanInfo>
    var mAdapter: ListDataAdapter? = null

    companion object {

        internal val TAG = GlobalMonitorActivity::class.java.simpleName
        internal val TAG_FRAG = "InputWiFiPasswordDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        initViews()
        initParams()
        initEvents()
    }

    private fun initViews() {
        mBtnScan = findViewById(R.id.btn_scan)
        mTvStatus = findViewById(R.id.tv_status)
        mListView = findViewById(R.id.list_view)
    }

    internal open fun initParams() {
        mData = ArrayList()
        mAdapter = ListDataAdapter(this, mData)
        mListView?.adapter = mAdapter
    }

    private fun initEvents() {
        mBtnScan?.setOnClickListener {
            AndPermission
                    .with(this)
                    .runtime()
                    .permission(Permission.ACCESS_FINE_LOCATION)
                    .onGranted { WiFiModule.startScan(getScanActionListener()) }
                    .onDenied { Toast.makeText(this, "请授予位置权限", Toast.LENGTH_LONG).show() }
                    .start()
        }

        mListView?.setOnItemClickListener { _, _, position, _ ->
            val info = mData[position]

            if (info.connectType != WiFiConnectType.CONNECTED.type) {
                // 输入密码，连接
                val configuration = info.configuration
                if (null != configuration) {
                    WiFiModule.connectWiFi(configuration, getConnectActionListener(info))

                } else {
                    showInputDialog(info, 0)
                }

            } else {
                showDelDialog(info)
            }
        }

        mListView?.setOnItemLongClickListener { _, _, position, _ ->
            val info = mData[position]
            if (null != info.configuration) {
                showDelDialog(info)
            }

            true
        }
    }

    open fun getScanActionListener(): ScanWiFiActionListener? {
        return null
    }

    open fun getConnectActionListener(info: WiFiScanInfo): ConnectWiFiActionListener? {
        return null
    }

    open fun getRemoveActionListener(SSID: String): RemoveWiFiActionListener? {
        return null
    }

    /**
     * 删除wifi
     */
    private fun showDelDialog(info: WiFiScanInfo) {
        val ssid = info.scanResult?.SSID
        if (ssid.isNullOrEmpty()) {
            return
        }
        TipDialog.showTipDialog(this,
                "确定要删除\"${ssid}\"吗？",
                "删除后需要重新输入密码",
                View.OnClickListener {
                    WiFiModule.removeWiFi(ssid, getRemoveActionListener(ssid))
                })
    }

    /**
     * 输入密码连接wifi
     */
    fun showInputDialog(info: WiFiScanInfo, type: Int) {
        if (WiFiCipherType.WIFI_CIPHER_NO_PASS === info.cipherType) {

            WiFiModule.connectWiFi(info.scanResult?.SSID!!)

        } else {
            val dialog = InputWiFiPasswordDialog()
            val bundle = Bundle()
            bundle.putParcelable("info", info)
            bundle.putInt("connectType", type)
            dialog.arguments = bundle

            dialog.show(supportFragmentManager, TAG_FRAG)

            dialog.setConnectListener(getConnectActionListener(info))
        }
    }

    /**
     * 删除失败，提示用户去系统设置操作
     */
    internal fun showDelErrorDialog() {
        hideInputWiFiPasswordDialog()
        TipDialog.showTipDialog(this,
                "无法忘记网络",
                "由于系统限制，需要到系统设置->WiFi/WLAN中忘记网络",
                View.OnClickListener {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
        )
    }

    private fun hideInputWiFiPasswordDialog() {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAG)
        if (null != fragment && fragment.isVisible && fragment is InputWiFiPasswordDialog) {
            fragment.dismissAllowingStateLoss()
        }
    }

    internal fun findScanInfo(SSID: String): WiFiScanInfo? {
        for (scanInfo in mData) {
            if (SSID == scanInfo.scanResult!!.SSID) {
                return scanInfo
            }
        }
        return null
    }

    internal fun refreshData(SSID: String?, connectType: WiFiConnectType) {
        if (TextUtils.isEmpty(SSID)) {
            return
        }

        for (info in mData) {
            if (!TextUtils.isEmpty(SSID) && SSID == info.scanResult!!.SSID) {
                info.connectType = connectType.type

            } else {
                info.connectType = WiFiConnectType.DISCONNECTED.type
            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    internal fun notifyState(state: NetworkInfo.DetailedState) {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_FRAG)
        if (null != fragment && fragment.isVisible && fragment is InputWiFiPasswordDialog) {
            fragment.connectCallBack(state)
        }
    }

}
