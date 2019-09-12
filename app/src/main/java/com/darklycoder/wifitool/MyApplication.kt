package com.darklycoder.wifitool

import android.app.Application

import com.darklycoder.wifitool.lib.WiFiConfig
import com.darklycoder.wifitool.lib.WiFiModule

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        //初始化
        val config = WiFiConfig.Builder()
                .setTimeOut((1000 * 20).toLong())
                .build()

        WiFiModule.setConfig(config).init(this)
    }

}
