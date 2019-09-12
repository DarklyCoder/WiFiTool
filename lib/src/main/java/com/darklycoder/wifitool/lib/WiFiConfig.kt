package com.darklycoder.wifitool.lib

/**
 * WiFi配置
 */
class WiFiConfig private constructor() {

    var timeOut = DEFAULT_TIME_OUT

    class Builder {
        private val config: WiFiConfig = WiFiConfig()

        fun setTimeOut(time: Long): Builder {
            config.timeOut = time
            return this
        }

        fun build(): WiFiConfig {
            return config
        }
    }

    companion object {

        // 默认超时时间
        private const val DEFAULT_TIME_OUT = (1000 * 15).toLong()
    }

}
