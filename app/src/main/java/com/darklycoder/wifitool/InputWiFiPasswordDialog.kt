package com.darklycoder.wifitool

import android.content.Context
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.darklycoder.wifitool.lib.WiFiModule
import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener

/**
 * wifi密码输入框
 */
class InputWiFiPasswordDialog : DialogFragment() {

    private var mTvTitle: TextView? = null
    private var mEtPassword: EditText? = null
    private var mTvConnect: TextView? = null
    private var mIvClose: ImageView? = null
    private var mTvConnectState: TextView? = null

    private var mInfo: WiFiScanInfo? = null
    private var isConnecting = false
    private var mConnectType: Int = 0

    private var mConnectActionListener: ConnectWiFiActionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_input_wifi_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (null != bundle) {
            mInfo = bundle.getParcelable("info")
            mConnectType = bundle.getInt("connectType", 0)
        }
        if (null == mInfo) {
            dismissAllowingStateLoss()
            return
        }

        mTvTitle = view.findViewById(R.id.tv_title)
        mEtPassword = view.findViewById(R.id.et_password)
        mTvConnect = view.findViewById(R.id.tv_connect)
        mIvClose = view.findViewById(R.id.iv_close)
        mTvConnectState = view.findViewById(R.id.tv_connect_state)

        if (mConnectType == 1) {
            mTvTitle?.text = "修改" + mInfo?.scanResult?.SSID

        } else {
            mTvTitle?.text = mInfo?.scanResult?.SSID
        }

        mEtPassword?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                refreshConnectBtn()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        mEtPassword?.isFocusable = true
        mEtPassword?.isFocusableInTouchMode = true
        mEtPassword?.requestFocus()

        mEtPassword?.post {
            try {
                val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mIvClose?.setOnClickListener { mEtPassword?.setText(null) }

        view.findViewById<View>(R.id.tv_cancel).setOnClickListener { dismissAllowingStateLoss() }

        view.findViewById<View>(R.id.tv_connect).setOnClickListener {
            // 连接
            if (isConnecting) {
                return@setOnClickListener
            }

            isConnecting = true

            WiFiModule.connectWiFi(mInfo?.scanResult?.SSID!!, mInfo?.cipherType!!, mEtPassword?.text.toString().trim { it <= ' ' }, mConnectActionListener)

            mTvConnect?.isEnabled = !isConnecting
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        dialog!!.setCanceledOnTouchOutside(true)

        val window = dialog.window
        if (null != window) {
            val lp = window.attributes
            lp.gravity = Gravity.CENTER
            lp.width = dp2px(context, 280)
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = lp
        }
    }

    private fun isEmpty(password: String): Boolean {
        return TextUtils.isEmpty(password)
    }

    private fun isValidPassword(password: String): Boolean {
        if (isEmpty(password)) {
            return false
        }

        //6~8位
        val size = password.length
        return size >= 8
    }

    private fun refreshConnectBtn() {
        val password = mEtPassword?.text.toString().trim { it <= ' ' }

        mIvClose?.visibility = if (isEmpty(password)) View.GONE else View.VISIBLE
        mTvConnect?.isEnabled = !isConnecting && isValidPassword(password)
    }

    fun setConnectListener(actionListener: ConnectWiFiActionListener?) {
        this.mConnectActionListener = actionListener
    }

    /**
     * 连接状态回调
     */
    fun connectCallBack(state: NetworkInfo.DetailedState) {
        when (state) {
            NetworkInfo.DetailedState.CONNECTING -> {
                mTvConnectState?.text = "连接中..."
                mTvConnectState?.visibility = View.VISIBLE
            }

            NetworkInfo.DetailedState.DISCONNECTED -> {
                isConnecting = false
                refreshConnectBtn()
                mTvConnectState?.text = "连接失败"
                mTvConnectState?.visibility = View.VISIBLE
            }

            NetworkInfo.DetailedState.CONNECTED -> {
                isConnecting = false
                mTvConnectState?.text = "连接成功"
                mTvConnectState?.visibility = View.VISIBLE
                dismissAllowingStateLoss()
            }

            else -> {
                isConnecting = false
                mTvConnectState?.visibility = View.GONE
                dismissAllowingStateLoss()
            }
        }
    }

    private fun dp2px(context: Context?, dpValue: Int): Int {
        if (null == context) {
            return dpValue
        }

        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}
