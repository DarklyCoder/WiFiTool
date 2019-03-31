package com.darklycoder.wifitool;

import android.app.Application;

import com.darklycoder.wifitool.lib.WiFiConfig;
import com.darklycoder.wifitool.lib.WiFiModule;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化
        WiFiConfig config = new WiFiConfig.Builder()
                .setTimeOut(1000 * 4)
                .build();
        WiFiModule.getInstance().setWiFiConfig(config);
        WiFiModule.getInstance().init(this);
    }


}
