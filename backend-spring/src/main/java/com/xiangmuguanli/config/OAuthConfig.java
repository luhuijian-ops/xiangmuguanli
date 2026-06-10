package com.xiangmuguanli.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OAuthConfig {

    private WeChatConfig wechat = new WeChatConfig();
    private DingTalkConfig dingtalk = new DingTalkConfig();

    public WeChatConfig getWechat() {
        return wechat;
    }

    public void setWechat(WeChatConfig wechat) {
        this.wechat = wechat;
    }

    public DingTalkConfig getDingtalk() {
        return dingtalk;
    }

    public void setDingtalk(DingTalkConfig dingtalk) {
        this.dingtalk = dingtalk;
    }

    public static class WeChatConfig {
        private String appId;
        private String appSecret;
        private String redirectUri;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }

    public static class DingTalkConfig {
        private String appId;
        private String appSecret;
        private String redirectUri;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
