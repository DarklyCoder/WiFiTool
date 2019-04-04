package com.darklycoder.wifitool;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.darklycoder.wifitool.lib.WiFiModule;
import com.darklycoder.wifitool.lib.info.WiFiRemoveStatusInfo;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;
import com.darklycoder.wifitool.lib.interfaces.WiFiListener;
import com.darklycoder.wifitool.lib.interfaces.impl.WiFiListenerImpl;
import com.darklycoder.wifitool.lib.type.WiFGetListType;
import com.darklycoder.wifitool.lib.type.WiFiCipherType;
import com.darklycoder.wifitool.lib.type.WiFiConnectFailType;
import com.darklycoder.wifitool.lib.type.WiFiConnectType;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.ArrayList;
import java.util.List;

public class Test2Activity extends AppCompatActivity {

    private static final String TAG = "Test2Activity";
    private final String TAG_FRAG = "InputWiFiPasswordDialog";

    private Button mBtnScan;
    private TextView mTvStatus;
    private ListView mListView;

    private List<WiFiScanInfo> mData;
    private ListDataAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initParams();
        initEvents();
    }

    private void initViews() {
        mBtnScan = findViewById(R.id.btn_scan);
        mTvStatus = findViewById(R.id.tv_status);
        mListView = findViewById(R.id.list_view);
    }

    private void initParams() {
        mData = new ArrayList<>();
        mAdapter = new ListDataAdapter(this, mData);
        mListView.setAdapter(mAdapter);

        //添加监听
        WiFiModule.getInstance().addWiFiListener(TAG, mListener);
    }

    private void initEvents() {
        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(Test2Activity.this)
                        .runtime()
                        .permission(Permission.ACCESS_FINE_LOCATION)
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                WiFiModule.getInstance().startScan();
                            }

                        })
                        .onDenied(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                Toast.makeText(Test2Activity.this, "请授予位置权限", Toast.LENGTH_LONG).show();
                            }
                        })
                        .start();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WiFiScanInfo info = mData.get(position);

                if (info.connectType != WiFiConnectType.CONNECTED.type) {
                    //输入密码，连接
                    showInputDialog(info);

                } else {
                    showDelDialog(info);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                WiFiScanInfo info = mData.get(position);
                if (null != info.configuration) {
                    showDelDialog(info);
                }

                return true;
            }
        });
    }

    /**
     * 删除wifi
     */
    private void showDelDialog(final WiFiScanInfo info) {
        TipDialog.showTipDialog(this,
                "确定要删除" + "\"" + info.scanResult.SSID + "\"吗？",
                "删除后需要重新输入密码",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WiFiModule.getInstance().removeWiFi(info.scanResult.SSID);
                    }
                }
        );
    }

    /**
     * 输入密码连接wifi
     */
    private void showInputDialog(WiFiScanInfo info) {
        if (WiFiCipherType.WIFI_CIPHER_NO_PASS == info.getCipherType()) {
            WiFiModule.getInstance().connectWiFi(info.scanResult.SSID, WiFiCipherType.WIFI_CIPHER_NO_PASS, null);

        } else {
            InputWiFiPasswordDialog dialog = new InputWiFiPasswordDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable("info", info);
            dialog.setArguments(bundle);

            dialog.show(getSupportFragmentManager(), TAG_FRAG);
        }
    }

    /**
     * 删除失败，提示用户去系统设置操作
     */
    private void showDelErrorDialog() {
        hideInputWiFiPasswordDialog();
        TipDialog.showTipDialog(this,
                "无法忘记网络",
                "由于系统限制，需要到系统设置->WiFi/WLAN中忘记网络",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    private void hideInputWiFiPasswordDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAG);
        if (null != fragment && fragment.isVisible() && fragment instanceof InputWiFiPasswordDialog) {
            ((InputWiFiPasswordDialog) fragment).dismissAllowingStateLoss();
        }
    }

    private WiFiListener mListener = new WiFiListenerImpl() {

        @Override
        public void onStartScan() {
            mTvStatus.setText("扫描中...");
            mBtnScan.setEnabled(false);
        }

        @Override
        public void onCloseWiFi() {
            mTvStatus.setText("WiFi已关闭");

            mData.clear();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDataChange(WiFGetListType type, List<WiFiScanInfo> list) {
            if (type == WiFGetListType.TYPE_SCAN) {
                mBtnScan.setEnabled(true);
                mTvStatus.setText("扫描结束");
            }

            mData.clear();
            mData.addAll(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onWiFiStartConnect(String SSID) {
            mTvStatus.setText(SSID + "连接中...");
            refreshData(SSID, WiFiConnectType.CONNECTING);

            notifyState(NetworkInfo.DetailedState.CONNECTING);
        }

        @Override
        public void onWiFiCreateConfig(String SSID, WifiConfiguration configuration) {
            for (WiFiScanInfo info : mData) {
                if (!TextUtils.isEmpty(SSID) && SSID.equals(info.scanResult.SSID)) {
                    info.configuration = configuration;
                    break;
                }
            }

            refreshData(SSID, WiFiConnectType.CONNECTING);

            notifyState(NetworkInfo.DetailedState.CONNECTING);
        }

        @Override
        public void onWiFiConnected(String SSID, boolean isInit) {
            mTvStatus.setText(isInit + " || " + SSID + "已连接");
            refreshData(SSID, WiFiConnectType.CONNECTED);

            notifyState(NetworkInfo.DetailedState.CONNECTED);
        }

        @Override
        public void onWiFiConnectFail(String SSID, WiFiConnectFailType type) {
            mTvStatus.setText(SSID + "连接失败，" + type.name());
            refreshData(SSID, WiFiConnectType.DISCONNECTED);

            notifyState(NetworkInfo.DetailedState.DISCONNECTED);
        }

        @Override
        public void onWiFiRemoveResult(WiFiRemoveStatusInfo info) {
            if (!info.isSuccess) {
                mTvStatus.setText(info.SSID + "删除失败！");
                showDelErrorDialog();

                return;
            }

            mTvStatus.setText(info.SSID + "删除成功！");
            for (WiFiScanInfo connectInfo : mData) {
                if (!TextUtils.isEmpty(info.SSID) && info.SSID.equals(connectInfo.scanResult.SSID)) {
                    connectInfo.configuration = null;
                    break;
                }
            }
            refreshData(info.SSID, WiFiConnectType.DISCONNECTED);
        }
    };

    private void refreshData(String SSID, WiFiConnectType connectType) {
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

    private void notifyState(NetworkInfo.DetailedState state) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAG);
        if (null != fragment && fragment.isVisible() && fragment instanceof InputWiFiPasswordDialog) {
            ((InputWiFiPasswordDialog) fragment).connectCallBack(state);
        }
    }

    @Override
    protected void onDestroy() {
        WiFiModule.getInstance().removeWiFiListener(TAG);
        super.onDestroy();
    }
}
