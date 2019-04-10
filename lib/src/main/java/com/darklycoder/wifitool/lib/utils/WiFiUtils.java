package com.darklycoder.wifitool.lib.utils;

import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.darklycoder.wifitool.lib.WiFiModule;
import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WiFi工具类
 */
public final class WiFiUtils {

    /**
     * WiFi是否可用
     *
     * @return 是否执行成功
     */
    public static boolean isWiFiEnable(WifiManager manager) {
        try {
            if (null == manager) {
                return false;
            }

            return manager.isWifiEnabled();

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }

        return false;
    }

    /**
     * 开始扫描
     *
     * @return 是否执行成功
     */
    public static boolean startScan(WifiManager manager) {
        try {
            if (null == manager) {
                return false;
            }

            return manager.startScan();

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }

        return false;
    }

    /**
     * 设置WiFi状态
     *
     * @return 是否执行成功
     */
    public static boolean setWifiEnabled(WifiManager manager, boolean isOpen) {
        try {
            if (null == manager) {
                return false;
            }

            return manager.setWifiEnabled(isOpen);

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }

        return false;
    }

    /**
     * 获取扫描到的WiFi列表
     */
    public static List<WiFiScanInfo> getScanList(WifiManager manager) {
        if (null == manager) {
            return new ArrayList<>();
        }

        String curSSID = isActiveWifi(manager) ? manager.getConnectionInfo().getSSID() : "";

        List<ScanResult> results = noSameName(manager.getScanResults());
        List<WiFiScanInfo> list = new ArrayList<>();
        boolean isCur;
        WiFiScanInfo curInfo = null;//当前连接上的wifi
        List<WiFiScanInfo> normalList = new ArrayList<>();//不存在配置的列表
        List<WiFiScanInfo> existConfigList = new ArrayList<>();//存在配置的列表

        for (ScanResult result : results) {
            WiFiScanInfo connectInfo = new WiFiScanInfo();
            connectInfo.scanResult = result;
            connectInfo.configuration = getExistConfig(manager, result.SSID);

            isCur = (!TextUtils.isEmpty(curSSID) && curSSID.equals("\"" + result.SSID + "\""));
            connectInfo.connectType = (isCur ? WiFiConnectType.CONNECTED.type : WiFiConnectType.DISCONNECTED.type);
            connectInfo.level = WifiManager.calculateSignalLevel(result.level, 4) + 1;

            if (isCur) {
                //当前已连接
                curInfo = connectInfo;

            } else {
                if (null != connectInfo.configuration) {
                    //存在配置
                    existConfigList.add(connectInfo);

                } else {
                    normalList.add(connectInfo);
                }
            }
        }

        //优先把保存了配置的放在上面
        if (null != curInfo) {
            list.add(curInfo);
        }

        Collections.sort(existConfigList);
        list.addAll(existConfigList);

        Collections.sort(normalList);
        list.addAll(normalList);

        return list;
    }

    /**
     * 获取当前连接的WiFi
     */
    public static WifiInfo getConnectedWifiInfo(WifiManager manager) {
        boolean isActive = isActiveWifi(manager);

        if (isActive) {
            return manager.getConnectionInfo();
        }

        return null;
    }

    /**
     * 是否有当前可用的WiFi连接
     */
    public static boolean isActiveWifi(WifiManager manager) {
        if (null == manager) {
            return false;
        }

        WifiInfo info = manager.getConnectionInfo();
        if (null == info) {
            return false;
        }

        String ssid = info.getSSID();

        return SupplicantState.COMPLETED == info.getSupplicantState()
                && !TextUtils.isEmpty(ssid)
                && !ssid.equalsIgnoreCase("0x")
                && !"<unknown ssid>".equals(ssid);
    }

    /**
     * 关闭所有连接
     */
    public static void closeAllConnect(WifiManager manager) {
        if (null == manager) {
            return;
        }

        for (WifiConfiguration c : manager.getConfiguredNetworks()) {
            manager.disableNetwork(c.networkId);
        }
    }

    /**
     * 连接WiFi
     */
    public static WiFiCreateConfigStatusInfo connectWiFi(WifiManager manager, String SSID, WiFiCipherType type, String pwd, String pkg) {
        WiFiCreateConfigStatusInfo info = new WiFiCreateConfigStatusInfo();
        info.SSID = SSID;
        info.isSuccess = false;

        //先关闭当前所有已连接的网络
        closeAllConnect(manager);

        WifiConfiguration config = getExistConfig(manager, SSID);

        if (null == config) {
            //不存在旧的配置，添加WiFi
            return addWifi(manager, SSID, type, pwd);
        }

        //存在旧的配置，先尝试移除WiFi
        if (removeWiFi(manager, SSID, pkg).isSuccess) {
            //移除成功，重新添加WiFi
            return addWifi(manager, SSID, type, pwd);
        }

        return info;
    }

    /**
     * 根据networkId连接WiFi
     */
    public static boolean enableNetwork(WifiManager manager, int networkId) {
        return null != manager && manager.enableNetwork(networkId, true);
    }

    /**
     * 移除WiFi
     * <p>Android6.0 之后应用只能删除自己创建的WIFI网络</p>
     */
    public static WiFiRemoveStatusInfo removeWiFi(WifiManager manager, String SSID, String pkg) {
        WiFiRemoveStatusInfo info = new WiFiRemoveStatusInfo();
        info.SSID = SSID;

        if (null == manager) {
            return info;
        }

        WifiConfiguration config = getExistConfig(manager, SSID);

        if (null == config) {
            //如果不存在配置，默认是删除成功
            info.isSuccess = true;
            return info;
        }

        try {
            if (config.networkId != -1) {

                //获取当前WiFi的创建者
                Field field = config.getClass().getDeclaredField("creatorName");
                field.setAccessible(true);
                Object creatorName = field.get(config);
                boolean isSystemApp = WiFiModule.getInstance().isSystemApplication();

                WiFiLogUtils.d("isSystemApp:" + isSystemApp + "||field:" + field + "||creatorName：" + creatorName);

                if (pkg.equals(creatorName) || isSystemApp) {

                    info.isSuccess = manager.disableNetwork(config.networkId)
                            && manager.removeNetwork(config.networkId)
                            && manager.saveConfiguration();
                }
            }

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }

        return info;
    }

    /**
     * 添加WiFi到系统
     */
    private static WiFiCreateConfigStatusInfo addWifi(WifiManager manager, String SSID, WiFiCipherType type, String pwd) {
        WifiConfiguration configuration = createConfiguration(SSID, type, pwd);
        configuration.networkId = manager.addNetwork(configuration);

        WiFiCreateConfigStatusInfo configInfo = new WiFiCreateConfigStatusInfo();
        configInfo.SSID = SSID;
        configInfo.configuration = configuration;
        configInfo.isSuccess = (configuration.networkId != -1);

        return configInfo;
    }

    /**
     * 创建配置
     */
    private static WifiConfiguration createConfiguration(String SSID, WiFiCipherType type, String password) {
        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + SSID + "\"";

        switch (type) {
            case WIFI_CIPHER_NO_PASS:
                // config.wepKeys[0] = "";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                // config.wepTxKeyIndex = 0;
                break;

            case WIFI_CIPHER_WEP:
                if (!TextUtils.isEmpty(password)) {
                    if (isHexWepKey(password)) {
                        config.wepKeys[0] = password;

                    } else {
                        config.wepKeys[0] = "\"" + password + "\"";
                    }
                }

                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                break;

            case WIFI_CIPHER_WPA:
                config.preSharedKey = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.status = WifiConfiguration.Status.ENABLED;

            default:
                break;
        }

        return config;
    }

    /**
     * 获取是否已经存在的配置
     */
    private static WifiConfiguration getExistConfig(WifiManager manager, String SSID) {
        if (null == manager) {
            return null;
        }

        try {
            List<WifiConfiguration> existingConfigs = manager.getConfiguredNetworks();

            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }

        return null;
    }

    /**
     * 去除同名WIFI
     */
    private static List<ScanResult> noSameName(List<ScanResult> oldSr) {
        List<ScanResult> newSr = new ArrayList<>();
        for (ScanResult result : oldSr) {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID)) {
                newSr.add(result);
            }
        }
        return newSr;
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     */
    private static boolean containName(List<ScanResult> sr, String name) {
        for (ScanResult result : sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        return (len == 10 || len == 26 || len == 58) && isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f')) {
                return false;
            }
        }
        return true;
    }

}
