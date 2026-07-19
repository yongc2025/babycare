package com.huigrowth.babycare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（T080 预留）
 * <p>
 * 正式接入时需填写 appId 和 appSecret。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.mini-program")
public class WeChatConfig {

    /** 微信小程序 AppID */
    private String appId = "";

    /** 微信小程序 AppSecret */
    private String appSecret = "";
}
