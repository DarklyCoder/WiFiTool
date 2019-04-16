package com.darklycoder.wifitool.lib;

/**
 * WiFi配置
 */
public class WiFiConfig {

    //默认超时时间
    private static final long DEFAULT_TIME_OUT = 1000 * 15;

    public long timeOut = DEFAULT_TIME_OUT;

    private WiFiConfig() {
    }

    public static class Builder {
        private WiFiConfig config;

        public Builder() {
            config = new WiFiConfig();
        }

        public Builder setTimeOut(long time) {
            config.timeOut = time;
            return this;
        }

        public WiFiConfig build() {
            return config;
        }
    }

}
