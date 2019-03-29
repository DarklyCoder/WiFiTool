package com.darklycoder.wifitool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;

import java.util.List;

public class ListDataAdapter extends BaseAdapter {

    private Context mContext;
    private List<WiFiScanInfo> mData;

    ListDataAdapter(Context context, List<WiFiScanInfo> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return null == mData ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_network_config, parent, false);
            viewHolder.mIvStatusConnect = convertView.findViewById(R.id.iv_status_connect);
            viewHolder.mProgressBar = convertView.findViewById(R.id.progress_bar);
            viewHolder.mTvNetName = convertView.findViewById(R.id.tv_net_name);
            viewHolder.mIvStatusWifi = convertView.findViewById(R.id.iv_status_wifi);
            viewHolder.mIvStatusLock = convertView.findViewById(R.id.iv_status_lock);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final WiFiScanInfo info = mData.get(position);

        if (null != info.configuration) {
            if (info.connectType == WiFiConnectType.CONNECTED.type) {
                viewHolder.mTvNetName.setText(info.scanResult.SSID);

            } else {
                viewHolder.mTvNetName.setText(info.scanResult.SSID + "（已保存）");
            }

        } else {
            viewHolder.mTvNetName.setText(info.scanResult.SSID);
        }

        boolean noPass = (WiFiCipherType.WIFI_CIPHER_NO_PASS == info.getCipherType());
        if (noPass) {
            viewHolder.mIvStatusLock.setVisibility(View.GONE);
        } else {
            viewHolder.mIvStatusLock.setVisibility(View.VISIBLE);
        }

        if (info.connectType == WiFiConnectType.CONNECTED.type) {
            viewHolder.mIvStatusConnect.setVisibility(View.VISIBLE);
            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);

        } else if (info.connectType == WiFiConnectType.CONNECTING.type) {
            //连接中
            viewHolder.mIvStatusConnect.setVisibility(View.INVISIBLE);
            viewHolder.mProgressBar.setVisibility(View.VISIBLE);

        } else {
            viewHolder.mIvStatusConnect.setVisibility(View.INVISIBLE);
            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        }

        switch (info.level) {
            case 1:
                viewHolder.mIvStatusWifi.setImageResource(R.drawable.ic_wifi_1);
                break;

            case 2:
                viewHolder.mIvStatusWifi.setImageResource(R.drawable.ic_wifi_2);
                break;

            case 3:
                viewHolder.mIvStatusWifi.setImageResource(R.drawable.ic_wifi_3);
                break;

            default:
                viewHolder.mIvStatusWifi.setImageResource(R.drawable.ic_wifi);
                break;
        }

        return convertView;
    }

    public static class ViewHolder {
        ImageView mIvStatusConnect;
        ProgressBar mProgressBar;
        TextView mTvNetName;
        ImageView mIvStatusWifi;
        ImageView mIvStatusLock;
    }

}
