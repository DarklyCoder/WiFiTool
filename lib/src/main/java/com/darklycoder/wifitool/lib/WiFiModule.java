package com.darklycoder.wifitool.lib;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.darklycoder.wifitool.lib.info.WiFiCreateConfigStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.interfaces.WiFiStatusListener;
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiStatusImpl;
import com.darklycoder.wifitool.lib.receiver.WiFiStatusReceiver;
import com.darklycoder.wifitool.lib.type.WiFGetListType;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiOperateStatus;
import com.darklycoder.wifitool.lib.utils.WiFiLogUtils;
import com.darklycoder.wifitool.lib.utils.WiFiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 统一封装对外提供的WiFi相关操作
 */
public class WiFiModule {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private WeakReference<Context> mContext;
    private WifiManager mWifiManager;
    private HashMap<String, WiFiListener> mListeners = new HashMap<>();//存放WiFi状态监听回调
    private WiFiConfig mWiFiConfig = new WiFiConfig.Builder().build();

    private WiFiStatusListener mCallback;
    private WiFiStatusReceiver mStatusReceiver;

    private WiFiModule() {
    }

    private static class WiFiModuleInner {
        private static WiFiModule instance = new WiFiModule();
    }

    public static WiFiModule getInstance() {
        return WiFiModuleInner.instance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        if (null != mContext && null != mWifiManager) {
            return;
        }

        mContext = new WeakReference<>(context.getApplicationContext());
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        registerReceiver();

        WiFiLogUtils.d("初始化成功");
    }

    private void registerReceiver() {
        //注册监听广播
        mCallback = new WiFiStatusImpl(mListeners);
        mStatusReceiver = new WiFiStatusReceiver(mCallback);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//监听wifi连接失败
        mContext.get().registerReceiver(mStatusReceiver, intentFilter);
    }

    /**
     * 设置WiFi配置
     *
     * @param config WiFi配置
     */
    public void setWiFiConfig(WiFiConfig config) {
        this.mWiFiConfig = config;
    }

    /**
     * 添加WiFi状态监听
     *
     * @param key      唯一标识
     * @param listener 监听回调
     */
    public void addWiFiListener(String key, WiFiListener listener) {
        if (mListeners.containsKey(key)) {
            return;
        }

        mListeners.put(key, listener);
    }

    /**
     * 移除WiFi状态监听
     *
     * @param key 唯一标识
     */
    public void removeWiFiListener(String key) {
        if (!mListeners.containsKey(key)) {
            return;
        }

        mListeners.remove(key);
    }

    /**
     * 切换系统WiFi状态
     *
     * @param isOpen 开关状态
     */
    public void toggleWiFiEnable(boolean isOpen) {
        if (isWiFiEnable() != isOpen) {
            WiFiUtils.setWifiEnabled(mWifiManager, isOpen);
        }
    }

    /**
     * WiFi是否打开
     */
    public boolean isWiFiEnable() {
        return WiFiUtils.isWiFiEnable(mWifiManager);
    }

    /**
     * 获取当前连接的WiFi信息
     */
    public WifiInfo getConnectedWifiInfo() {
        return WiFiUtils.getConnectedWifiInfo(mWifiManager);
    }

    /**
     * 开始扫描WiFi
     * <p>
     * Android 9.0 将 WiFiManager 的 startScan() 方法标为了废弃，
     * 前台应用 2 分钟内只能使用 4 次startScan()，
     * 后台应用 30 分钟内只能调用 1次 startScan()，
     * 否则会直接返回 false 并且不会触发扫描操作
     * </p>
     */
    public void startScan() {
        if (null != mCallback) {
            if (WiFiOperateStatus.SCANNING == mCallback.getWiFiOperateStatus()) {
                WiFiLogUtils.d("WiFi扫描中，忽略此次扫描请求！");
                return;
            }

            mCallback.notifyStartScan();
        }

        if (!isWiFiEnable()) {
            //开启WiFi
            WiFiLogUtils.d("WiFi不可用，启用WiFi！");
            toggleWiFiEnable(true);

            return;
        }

        boolean isSuccess = WiFiUtils.startScan(mWifiManager);

        if (!isSuccess) {
            getScanList(WiFGetListType.TYPE_SCAN);
        }
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnect() {
        WiFiUtils.closeAllConnect(mWifiManager);
    }

    /**
     * 获取列表数据
     */
    public void getScanList(final WiFGetListType type) {
        addDisposable(Observable
                .create(new ObservableOnSubscribe<List<WiFiScanInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<WiFiScanInfo>> emitter) {
                        List<WiFiScanInfo> list = WiFiUtils.getScanList(mWifiManager);
                        emitter.onNext(list);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<List<WiFiScanInfo>>() {
                    @Override
                    public void onNext(List<WiFiScanInfo> list) {
                        if (null != mCallback) {
                            mCallback.notifyWiFiList(type, list);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != mCallback) {
                            mCallback.notifyWiFiList(type, new ArrayList<WiFiScanInfo>());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     * 连接WiFi
     *
     * @param SSID WiFi标识
     * @param type 加密类型
     * @param pwd  密码(可能为空)
     */
    public void connectWiFi(final String SSID, final WiFiCipherType type, final String pwd) {
        if (null != mCallback) {
            if (WiFiOperateStatus.SCANNING == mCallback.getWiFiOperateStatus()) {
                WiFiLogUtils.d("WiFi扫描中，忽略此次连接请求！");
                return;
            }

            mCallback.notifyStartConnect(SSID, mWiFiConfig, 0);
        }

        addDisposable(Observable
                .create(new ObservableOnSubscribe<WiFiCreateConfigStatusInfo>() {
                    @Override
                    public void subscribe(ObservableEmitter<WiFiCreateConfigStatusInfo> emitter) {
                        WiFiCreateConfigStatusInfo info = WiFiUtils.connectWiFi(mWifiManager, SSID, type, pwd, mContext.get().getPackageName());
                        emitter.onNext(info);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<WiFiCreateConfigStatusInfo>() {
                    @Override
                    public void onNext(WiFiCreateConfigStatusInfo info) {
                        if (null != mCallback) {
                            mCallback.notifyStartConnectStatus(info);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != mCallback) {
                            mCallback.notifyStartConnectStatus(new WiFiCreateConfigStatusInfo());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     * 使用已经保存过的配置连接WiFi
     *
     * @param configuration 配置
     */
    public void connectWiFi(WifiConfiguration configuration) {
        if (null != mCallback) {
            if (WiFiOperateStatus.SCANNING == mCallback.getWiFiOperateStatus()) {
                WiFiLogUtils.d("WiFi扫描中，忽略此次连接请求！");
                return;
            }

            String SSID = configuration.SSID;
            int size = SSID.length();
            SSID = SSID.substring(1, size - 1);

            mCallback.notifyStartConnect(SSID, mWiFiConfig, 1);
        }

        WiFiUtils.closeAllConnect(mWifiManager);

        enableNetwork(configuration.networkId);
    }

    /**
     * 根据networkId启用该WiFi
     *
     * @param networkId 网络id
     */
    public void enableNetwork(final int networkId) {
        addDisposable(Observable
                .create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) {
                        boolean isSuccess = WiFiUtils.enableNetwork(mWifiManager, networkId);
                        emitter.onNext(isSuccess);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean info) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     * 移除WiFi
     *
     * @param SSID WiFi标识
     */
    public void removeWiFi(final String SSID) {
        addDisposable(Observable
                .create(new ObservableOnSubscribe<WiFiRemoveStatusInfo>() {
                    @Override
                    public void subscribe(ObservableEmitter<WiFiRemoveStatusInfo> emitter) {
                        WiFiRemoveStatusInfo info = WiFiUtils.removeWiFi(mWifiManager, SSID, mContext.get().getPackageName());
                        emitter.onNext(info);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<WiFiRemoveStatusInfo>() {
                    @Override
                    public void onNext(WiFiRemoveStatusInfo info) {
                        if (null != mCallback) {
                            mCallback.notifyRemoveStatus(info);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != mCallback) {
                            mCallback.notifyRemoveStatus(new WiFiRemoveStatusInfo());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        try {
            mListeners.clear();

            clearDisposable();

            if (null != mContext.get()) {
                mContext.get().unregisterReceiver(mStatusReceiver);
            }

            if (null != mCallback) {
                mCallback.destroy();
            }

            mContext = null;
            mWifiManager = null;
            mStatusReceiver = null;
            mCallback = null;

        } catch (Exception e) {
            WiFiLogUtils.e(e);
        }
    }

    private void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    private void clearDisposable() {
        compositeDisposable.clear();
    }

}
