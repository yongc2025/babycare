package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.SystemConfigResponse;
import com.huigrowth.babycare.dto.SystemConfigUpdateRequest;
import com.huigrowth.babycare.entity.SystemConfig;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public List<SystemConfigResponse> listAll() {
        return systemConfigRepository.findAll().stream()
                .map(SystemConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SystemConfigResponse getByKey(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException("配置项不存在"));
        return SystemConfigResponse.fromEntity(config);
    }

    @Transactional
    public SystemConfigResponse update(String configKey, SystemConfigUpdateRequest request) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException("配置项不存在"));
        if (request.getConfigValue() != null) config.setConfigValue(request.getConfigValue());
        if (request.getConfigName() != null) config.setConfigName(request.getConfigName());
        if (request.getConfigGroup() != null) config.setConfigGroup(request.getConfigGroup());
        if (request.getStatus() != null) {
            config.setStatus(SystemConfig.ConfigStatus.valueOf(request.getStatus()));
        }
        if (request.getRemark() != null) config.setRemark(request.getRemark());
        config = systemConfigRepository.save(config);
        return SystemConfigResponse.fromEntity(config);
    }
}
