package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.*;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.UserRepository;
import com.huigrowth.babycare.util.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证服务
 * 
 * @author HuiGrowth Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SmsService smsService;

    /**
     * 用户注册
     */
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        String username = normalizeBlank(request.getUsername());
        String email = normalizeBlank(request.getEmail());
        String phone = normalizeBlank(request.getPhone());
        String nickname = normalizeBlank(request.getNickname());
        String city = normalizeBlank(request.getCity());

        // 手机号注册：如果未提供用户名，使用手机号作为用户名
        boolean phoneOnly = (username == null && phone != null);
        if (phoneOnly) {
            username = "u_" + phone;
        }

        if (username == null) {
            throw new BusinessException("用户名或手机号不能为空");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            if (phoneOnly) {
                // 手机号已注册过，尝试登录
                throw new BusinessException("该手机号已注册，请直接登录");
            }
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已被使用");
        }

        // 检查手机号是否已存在
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException("手机号已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        // 设置邮箱：如果提供了邮箱则使用，否则使用手机号占位
        if (email != null) {
            user.setEmail(email);
        } else if (phone != null) {
            user.setEmail(phone + "@phone.example.com");
        } else {
            user.setEmail(username + "@example.com");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(phone);
        // 设置昵称
        if (nickname != null) {
            user.setNickname(nickname);
        } else if (phone != null) {
            user.setNickname(phone.substring(0, 3) + "****" + phone.substring(7));
        } else {
            user.setNickname(username);
        }
        user.setCity(city);
        user.setRole(User.UserRole.PARENT);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(request.isPhoneVerified()); // 如果注册时已验证手机号

        User savedUser = userRepository.save(user);
        log.info("新用户注册成功: {} (phone={})", savedUser.getUsername(), phone);

        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(savedUser.getUsername());
        UserResponse userResponse = UserResponse.fromEntity(savedUser);

        return new JwtResponse(jwt, userResponse);
    }

    /**
     * 发送短信验证码
     */
    public void sendVerificationCode(SendCodeRequest request) {
        String phone = request.getPhone().trim();
        String code = smsService.sendVerificationCode(phone);
        log.info("验证码已发送到手机 {} (模拟: {})", phone, code);
    }

    /**
     * 验证手机号
     */
    @Transactional
    public void verifyPhone(VerifyPhoneRequest request) {
        String phone = request.getPhone().trim();
        boolean verified = smsService.verifyCode(phone, request.getCode().trim());
        if (!verified) {
            throw new BusinessException("验证码错误");
        }

        // 如果用户已登录，标记手机号为已验证
        // 未登录场景下，仅验证验证码是否正确，注册时通过 RegisterRequest.isPhoneVerified 标记
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                user.setPhoneVerified(true);
                userRepository.save(user);
                log.info("用户 {} 手机号验证成功", user.getUsername());
            });
        }
    }

    /**
     * 手机号登录（支持密码或验证码）
     */
    public JwtResponse phoneLogin(PhoneLoginRequest request) {
        String phone = request.getPhone().trim();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("该手机号未注册"));

        if (!user.getEnabled()) {
            throw new BusinessException("账户已被禁用");
        }

        if (request.isCodeLogin()) {
            // 验证码登录
            boolean verified = smsService.verifyCode(phone, request.getCode().trim());
            if (!verified) {
                throw new BusinessException("验证码错误");
            }
        } else {
            // 密码登录
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new BusinessException("请输入密码或使用验证码登录");
            }
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BadCredentialsException e) {
                throw new BusinessException("密码错误");
            }
        }

        String jwt = jwtUtils.generateJwtToken(user.getUsername());
        UserResponse userResponse = UserResponse.fromEntity(user);
        log.info("用户手机号登录成功: {} ({})", user.getUsername(), phone);

        return new JwtResponse(jwt, userResponse);
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * 用户登录
     */
    public JwtResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByEmailOrUsername(request.getEmailOrUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户是否启用
        if (!user.getEnabled()) {
            throw new BusinessException("账户已被禁用");
        }

        // 进行身份验证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserResponse userResponse = UserResponse.fromEntity(user);

        log.info("用户登录成功: {}", user.getUsername());
        
        return new JwtResponse(jwt, userResponse);
    }

    /**
     * 刷新令牌
     */
    public JwtResponse refreshToken(String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new BusinessException("无效的令牌");
        }

        String username = jwtUtils.getUsernameFromJwtToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String newToken = jwtUtils.generateJwtToken(username);
        UserResponse userResponse = UserResponse.fromEntity(user);

        return new JwtResponse(newToken, userResponse);
    }

    /**
     * 更新用户资料
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查邮箱是否被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // 重置邮箱验证状态
        }

        // 检查手机号是否被其他用户使用
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false); // 重置手机验证状态
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }

        User updatedUser = userRepository.save(user);
        log.info("用户资料更新成功: {}", user.getUsername());

        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("用户密码修改成功: {}", user.getUsername());
    }

    /**
     * 获取当前用户信息
     */
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return UserResponse.fromEntity(user);
    }
}
