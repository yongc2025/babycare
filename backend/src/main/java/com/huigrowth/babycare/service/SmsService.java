package com.huigrowth.babycare.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务（模拟实现）
 * <p>
 * 当前为模拟实现，验证码固定为 123456。
 * 后续接入真实 SMS 网关后替换 sendSms 实现即可。
 */
@Slf4j
@Service
public class SmsService {

    private static final String MOCK_CODE = "123456";
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * 发送验证码到指定手机号
     *
     * @param phone 手机号
     * @return 验证码（生产环境不应返回，仅当前模拟用）
     */
    public String sendVerificationCode(String phone) {
        // 模拟生成 6 位验证码
        String code = String.format("%06d", random.nextInt(999999));
        codeStore.put(phone, code);
        log.info("[模拟短信] 发送验证码 {} 到手机 {}", code, phone);
        return code;
    }

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String phone, String code) {
        String stored = codeStore.get(phone);
        if (stored == null) {
            // 兼容测试环境：允许使用固定码 123456
            return MOCK_CODE.equals(code);
        }
        boolean match = stored.equals(code);
        if (match) {
            codeStore.remove(phone); // 一次性验证码，验证后移除
        }
        return match;
    }

    /**
     * 清除手机号的验证码记录
     */
    public void clearCode(String phone) {
        codeStore.remove(phone);
    }
}
