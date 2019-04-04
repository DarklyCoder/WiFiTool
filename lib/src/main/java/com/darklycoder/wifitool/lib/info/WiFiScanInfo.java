package com.darklycoder.wifitool.lib.info;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;

/**
 * WiFi扫描信息
 */
public class WiFiScanInfo implements Comparable<WiFiScanInfo>, Parcelable {

    //扫描结果
    public ScanResult scanResult;
    //连接过的WiFi配置，可能为空
    public WifiConfiguration configuration;
    //WiFi型号强度(1~4)
    public int level;
    //连接状态：0 未连接，1 正在连接，2 已连接
    public int connectType = WiFiConnectType.DISCONNECTED.type;

    @Override
    public int compareTo(WiFiScanInfo o) {
        //按照信号强度从大到小排序
        return o.level - level;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "\"SSID\":\"" + scanResult.SSID + "\"," +
                "\"type\":" + getCipherType().ordinal() + "," +
                "\"level\":" + level +
                "}";
    }

    /**
     * 返回WiFi加密类型
     *
     * @return WiFiCipherType
     */
    public WiFiCipherType getCipherType() {
        if (null == scanResult) {
            return WiFiCipherType.WIFI_CIPHER_INVALID;
        }

        String capabilities = scanResult.capabilities;

        if (TextUtils.isEmpty(capabilities)) {
            return WiFiCipherType.WIFI_CIPHER_INVALID;
        }

        if (capabilities.contains("WPA") || capabilities.contains("wpa") || capabilities.contains("WPA2") || capabilities.contains("WPS")) {
            return WiFiCipherType.WIFI_CIPHER_WPA;
        }

        if (capabilities.contains("WEP") || capabilities.contains("wep")) {
            return WiFiCipherType.WIFI_CIPHER_WEP;
        }

        return WiFiCipherType.WIFI_CIPHER_NO_PASS;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.scanResult, flags);
        dest.writeParcelable(this.configuration, flags);
        dest.writeInt(this.level);
        dest.writeInt(this.connectType);
    }

    public WiFiScanInfo() {
    }

    protected WiFiScanInfo(Parcel in) {
        this.scanResult = in.readParcelable(ScanResult.class.getClassLoader());
        this.configuration = in.readParcelable(WifiConfiguration.class.getClassLoader());
        this.level = in.readInt();
        this.connectType = in.readInt();
    }

    public static final Parcelable.Creator<WiFiScanInfo> CREATOR = new Parcelable.Creator<WiFiScanInfo>() {
        @Override
        public WiFiScanInfo createFromParcel(Parcel source) {
            return new WiFiScanInfo(source);
        }

        @Override
        public WiFiScanInfo[] newArray(int size) {
            return new WiFiScanInfo[size];
        }
    };
}
