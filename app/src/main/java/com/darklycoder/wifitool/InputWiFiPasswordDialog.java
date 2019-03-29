package com.darklycoder.wifitool;

import android.app.Dialog;
import android.content.Context;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.darklycoder.wifitool.lib.WiFiModule;
import com.darklycoder.wifitool.lib.info.WiFiScanInfo;

/**
 * wifi密码输入框
 */
public class InputWiFiPasswordDialog extends DialogFragment {

    TextView mTvTitle;
    EditText mEtPassword;
    TextView mTvConnect;
    ImageView mIvClose;
    TextView mTvConnectState;

    private WiFiScanInfo mInfo;
    private boolean isConnecting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_input_wifi_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (null != bundle) {
            mInfo = (WiFiScanInfo) bundle.getSerializable("info");
        }
        if (null == mInfo) {
            dismissAllowingStateLoss();
            return;
        }

        mTvTitle = view.findViewById(R.id.tv_title);
        mEtPassword = view.findViewById(R.id.et_password);
        mTvConnect = view.findViewById(R.id.tv_connect);
        mIvClose = view.findViewById(R.id.iv_close);
        mTvConnectState = view.findViewById(R.id.tv_connect_state);

        mTvTitle.setText(mInfo.scanResult.SSID);

        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshConnectBtn();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEtPassword.setFocusable(true);
        mEtPassword.setFocusableInTouchMode(true);
        mEtPassword.requestFocus();

        mEtPassword.post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtPassword.setText(null);
            }
        });

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        view.findViewById(R.id.tv_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //连接
                if (isConnecting) {
                    return;
                }

                isConnecting = true;
                WiFiModule.getInstance().connectWiFi(mInfo.scanResult.SSID, mInfo.getCipherType(), mEtPassword.getText().toString().trim());
                mTvConnect.setEnabled(!isConnecting);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(true);

        Window window = dialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = dp2px(getContext(), 280);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private boolean isEmpty(String password) {
        return TextUtils.isEmpty(password);
    }

    private boolean isValidPassword(String password) {
        if (isEmpty(password)) {
            return false;
        }

        //6~8位
        int size = password.length();
        if (size < 8) {
            return false;
        }

        return true;
    }

    private void refreshConnectBtn() {
        String password = mEtPassword.getText().toString().trim();

        mIvClose.setVisibility(isEmpty(password) ? View.GONE : View.VISIBLE);
        mTvConnect.setEnabled(!isConnecting && isValidPassword(password));
    }

    /**
     * 连接状态回调
     */
    public void connectCallBack(NetworkInfo.DetailedState state) {
        switch (state) {
            case CONNECTING:
                mTvConnectState.setText("连接中...");
                mTvConnectState.setVisibility(View.VISIBLE);
                break;

            case DISCONNECTED:
                isConnecting = false;
                refreshConnectBtn();
                mTvConnectState.setText("连接失败");
                mTvConnectState.setVisibility(View.VISIBLE);
                break;

            case CONNECTED:
                isConnecting = false;
                mTvConnectState.setText("连接成功");
                mTvConnectState.setVisibility(View.VISIBLE);
                dismissAllowingStateLoss();
                break;

            default:
                isConnecting = false;
                mTvConnectState.setVisibility(View.GONE);
                dismissAllowingStateLoss();
                break;
        }
    }

    private int dp2px(Context context, int dpValue) {
        if (null == context) {
            return dpValue;
        }

        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
