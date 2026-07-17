package com.huigrowth.babycare.config;

import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String username;

    @Value("${app.bootstrap.admin.password:admin123}")
    private String password;

    @Value("${app.bootstrap.admin.email:admin@example.com}")
    private String email;

    @Value("${app.bootstrap.admin.nickname:系统管理员}")
    private String nickname;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        String normalizedUsername = username.trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(normalizedUsername);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(StringUtils.hasText(email) ? email.trim() : normalizedUsername + "@example.com");
        admin.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : normalizedUsername);
        admin.setRole(User.UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setPhoneVerified(false);
        userRepository.save(admin);
        log.info("已创建开发环境管理员账号：{}", normalizedUsername);
    }
}
