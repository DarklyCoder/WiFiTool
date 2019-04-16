package com.darklycoder.wifitool;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.darklycoder.wifitool.lib.WiFiModule;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.ConnectWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.RemoveWiFiActionListener;
import com.darklycoder.wifitool.lib.interfaces.ScanWiFiActionListener;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMonitorActivity extends AppCompatActivity {

    static final String TAG = GlobalMonitorActivity.class.getSimpleName();
    static final String TAG_FRAG = "InputWiFiPasswordDialog";

    Button mBtnScan;
    TextView mTvStatus;
    ListView mListView;

    List<WiFiScanInfo> mData;
    ListDataAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        initViews();
        initParams();
        initEvents();
    }

    private void initViews() {
        mBtnScan = findViewById(R.id.btn_scan);
        mTvStatus = findViewById(R.id.tv_status);
        mListView = findViewById(R.id.list_view);
    }

    void initParams() {
        mData = new ArrayList<>();
        mAdapter = new ListDataAdapter(this, mData);
        mListView.setAdapter(mAdapter);
    }

    private void initEvents() {
        mBtnScan.setOnClickListener(v -> AndPermission
                .with(this)
                .runtime()
                .permission(Permission.ACCESS_FINE_LOCATION)
                .onGranted(data -> WiFiModule.getInstance().startScan(getScanActionListener()))
                .onDenied(data -> Toast.makeText(this, "请授予位置权限", Toast.LENGTH_LONG).show())
                .start()
        );

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            WiFiScanInfo info = mData.get(position);

            if (info.connectType != WiFiConnectType.CONNECTED.type) {
                //输入密码，连接
                if (null != info.configuration) {
                    WiFiModule.getInstance().connectWiFi(info.configuration, getConnectActionListener(info));

                } else {
                    showInputDialog(info, 0);
                }

            } else {
                showDelDialog(info);
            }
        });

        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            WiFiScanInfo info = mData.get(position);
            if (null != info.configuration) {
                showDelDialog(info);
            }

            return true;
        });
    }

    ScanWiFiActionListener getScanActionListener() {
        return null;
    }

    ConnectWiFiActionListener getConnectActionListener(WiFiScanInfo info) {
        return null;
    }

    RemoveWiFiActionListener getRemoveActionListener(String SSID) {
        return null;
    }

    /**
     * 删除wifi
     */
    void showDelDialog(final WiFiScanInfo info) {
        TipDialog.showTipDialog(this,
                "确定要删除" + "\"" + info.scanResult.SSID + "\"吗？",
                "删除后需要重新输入密码",
                v -> WiFiModule.getInstance().removeWiFi(info.scanResult.SSID, getRemoveActionListener(info.scanResult.SSID))
        );
    }

    /**
     * 输入密码连接wifi
     */
    void showInputDialog(WiFiScanInfo info, int type) {
        if (WiFiCipherType.WIFI_CIPHER_NO_PASS == info.getCipherType()) {
            WiFiModule.getInstance().connectWiFi(info.scanResult.SSID, WiFiCipherType.WIFI_CIPHER_NO_PASS, null);

        } else {
            InputWiFiPasswordDialog dialog = new InputWiFiPasswordDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable("info", info);
            bundle.putInt("connectType", type);
            dialog.setArguments(bundle);

            dialog.show(getSupportFragmentManager(), TAG_FRAG);

            dialog.setConnectListener(getConnectActionListener(info));
        }
    }

    /**
     * 删除失败，提示用户去系统设置操作
     */
    void showDelErrorDialog() {
        hideInputWiFiPasswordDialog();
        TipDialog.showTipDialog(this,
                "无法忘记网络",
                "由于系统限制，需要到系统设置->WiFi/WLAN中忘记网络",
                v -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
        );
    }

    void hideInputWiFiPasswordDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAG);
        if (null != fragment && fragment.isVisible() && fragment instanceof InputWiFiPasswordDialog) {
            ((InputWiFiPasswordDialog) fragment).dismissAllowingStateLoss();
        }
    }

    WiFiScanInfo findScanInfo(String SSID) {
        for (WiFiScanInfo scanInfo : mData) {
            if (SSID.equals(scanInfo.scanResult.SSID)) {
                return scanInfo;
            }
        }
        return null;
    }

    void refreshData(String SSID, WiFiConnectType connectType) {
        if (TextUtils.isEmpty(SSID)) {
            return;
        }

        for (WiFiScanInfo info : mData) {
            if (!TextUtils.isEmpty(SSID) && SSID.equals(info.scanResult.SSID)) {
                info.connectType = connectType.type;

            } else {
                info.connectType = WiFiConnectType.DISCONNECTED.type;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    void notifyState(NetworkInfo.DetailedState state) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAG);
        if (null != fragment && fragment.isVisible() && fragment instanceof InputWiFiPasswordDialog) {
            ((InputWiFiPasswordDialog) fragment).connectCallBack(state);
        }
    }

}
