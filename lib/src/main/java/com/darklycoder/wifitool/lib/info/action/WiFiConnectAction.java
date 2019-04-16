package com.darklycoder.wifitool.lib.info.action;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darklycoder.wifitool.lib.utils.WiFiLogUtils;
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;

import java.lang.ref.WeakReference;

/**
 * 连接WiFi
 */
public abstract class WiFiConnectAction extends IWiFiAction {

    public String SSID;
    public ConnectWiFiActionListener listener;
    public long timeout = 1000 * 15;//超时时间，默认15s
    private WeakReference<Handler> mHandler;
    private Runnable mDelayRunnable;

    WiFiConnectAction(@NonNull String SSID, @Nullable ConnectWiFiActionListener listener) {
        this.SSID = SSID;
        this.listener = listener;
    }

    @Override
    public void end() {
        super.end();

        stopDelayCheck();
    }

    /**
     * 开始延时检测
     */
    public void startDelayCheck(Handler handler, Runnable runnable) {
        this.mHandler = new WeakReference<>(handler);
        this.mDelayRunnable = runnable;

        try {
            handler.postDelayed(runnable, this.timeout);
            WiFiLogUtils.d("开启超时检测，" + toString());

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    /**
     * 结束延时检测
     */
    private void stopDelayCheck() {
        try {
            if (null == this.mDelayRunnable) {
                return;
            }

            this.mHandler.get().removeCallbacks(this.mDelayRunnable);
            this.mDelayRunnable = null;
            WiFiLogUtils.d("超时检测关闭，" + toString());

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

}
