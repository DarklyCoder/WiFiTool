package com.darklycoder.wifitool

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.darklycoder.wifitool.lib.info.WiFiScanInfo
import com.darklycoder.wifitool.lib.type.WiFiCipherType
import com.darklycoder.wifitool.lib.type.WiFiConnectType

class ListDataAdapter(private val mContext: Context, private val mData: List<WiFiScanInfo>?) : BaseAdapter() {

    override fun getCount(): Int {
        return mData?.size ?: 0
    }

    override fun getItem(position: Int): Any {
        return mData!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var tempView = convertView
        val viewHolder: ViewHolder

        if (null == tempView) {
            viewHolder = ViewHolder()
            tempView = LayoutInflater.from(mContext).inflate(R.layout.item_network_config, parent, false)
            viewHolder.mIvStatusConnect = tempView?.findViewById(R.id.iv_status_connect)
            viewHolder.mProgressBar = tempView?.findViewById(R.id.progress_bar)
            viewHolder.mTvNetName = tempView?.findViewById(R.id.tv_net_name)
            viewHolder.mIvStatusWifi = tempView?.findViewById(R.id.iv_status_wifi)
            viewHolder.mIvStatusLock = tempView?.findViewById(R.id.iv_status_lock)
            tempView?.tag = viewHolder

        } else {
            viewHolder = tempView.tag as ViewHolder
        }

        val info = mData!![position]

        if (null != info.configuration) {
            if (info.connectType == WiFiConnectType.CONNECTED.type) {
                viewHolder.mTvNetName?.text = info.scanResult!!.SSID

            } else {
                viewHolder.mTvNetName?.text = "${info.scanResult?.SSID}（已保存）"
            }

        } else {
            viewHolder.mTvNetName?.text = info.scanResult?.SSID
        }

        val noPass = WiFiCipherType.WIFI_CIPHER_NO_PASS === info.cipherType
        if (noPass) {
            viewHolder.mIvStatusLock?.visibility = View.GONE
        } else {
            viewHolder.mIvStatusLock?.visibility = View.VISIBLE
        }

        when {
            info.connectType == WiFiConnectType.CONNECTED.type -> {
                viewHolder.mIvStatusConnect!!.visibility = View.VISIBLE
                viewHolder.mProgressBar?.visibility = View.INVISIBLE
            }

            info.connectType == WiFiConnectType.CONNECTING.type -> {
                // 连接中
                viewHolder.mIvStatusConnect?.visibility = View.INVISIBLE
                viewHolder.mProgressBar?.visibility = View.VISIBLE
            }

            else -> {
                viewHolder.mIvStatusConnect?.visibility = View.INVISIBLE
                viewHolder.mProgressBar?.visibility = View.INVISIBLE
            }
        }

        when (info.level) {
            1 -> viewHolder.mIvStatusWifi?.setImageResource(R.drawable.ic_wifi_1)

            2 -> viewHolder.mIvStatusWifi?.setImageResource(R.drawable.ic_wifi_2)

            3 -> viewHolder.mIvStatusWifi?.setImageResource(R.drawable.ic_wifi_3)

            else -> viewHolder.mIvStatusWifi?.setImageResource(R.drawable.ic_wifi)
        }

        return tempView!!
    }

    internal class ViewHolder {
        var mIvStatusConnect: ImageView? = null
        var mProgressBar: ProgressBar? = null
        var mTvNetName: TextView? = null
        var mIvStatusWifi: ImageView? = null
        var mIvStatusLock: ImageView? = null
    }

}
