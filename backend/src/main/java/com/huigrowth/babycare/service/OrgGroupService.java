package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.OrgGroupCreateRequest;
import com.huigrowth.babycare.dto.OrgGroupResponse;
import com.huigrowth.babycare.entity.OrgGroup;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.OrgGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 集团/品牌服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgGroupService {

    private final OrgGroupRepository orgGroupRepository;

    @Transactional
    public OrgGroupResponse create(OrgGroupCreateRequest request) {
        if (orgGroupRepository.existsByCode(request.getCode())) {
            throw new BusinessException("集团编码已存在");
        }

        OrgGroup group = new OrgGroup();
        group.setName(request.getName());
        group.setCode(request.getCode());
        group.setDescription(request.getDescription());
        group.setLogo(request.getLogo());
        group.setContactPerson(request.getContactPerson());
        group.setContactPhone(request.getContactPhone());
        group.setAddress(request.getAddress());

        OrgGroup saved = orgGroupRepository.save(group);
        log.info("创建集团品牌: {} ({})", saved.getName(), saved.getCode());
        return convertToResponse(saved);
    }

    public List<OrgGroupResponse> listAll() {
        return orgGroupRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public OrgGroupResponse getById(Long id) {
        OrgGroup group = orgGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("集团品牌不存在"));
        return convertToResponse(group);
    }

    @Transactional
    public OrgGroupResponse update(Long id, OrgGroupCreateRequest request) {
        OrgGroup group = orgGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("集团品牌不存在"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setLogo(request.getLogo());
        group.setContactPerson(request.getContactPerson());
        group.setContactPhone(request.getContactPhone());
        group.setAddress(request.getAddress());

        OrgGroup saved = orgGroupRepository.save(group);
        log.info("更新集团品牌: {} ({})", saved.getName(), saved.getCode());
        return convertToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        OrgGroup group = orgGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("集团品牌不存在"));
        group.setStatus(OrgGroup.GroupStatus.DISABLED);
        orgGroupRepository.save(group);
        log.info("禁用集团品牌: {} ({})", group.getName(), group.getCode());
    }

    public OrgGroupResponse convertToResponse(OrgGroup group) {
        OrgGroupResponse resp = new OrgGroupResponse();
        resp.setId(group.getId());
        resp.setName(group.getName());
        resp.setCode(group.getCode());
        resp.setDescription(group.getDescription());
        resp.setLogo(group.getLogo());
        resp.setContactPerson(group.getContactPerson());
        resp.setContactPhone(group.getContactPhone());
        resp.setAddress(group.getAddress());
        resp.setStatus(group.getStatus());
        resp.setCreatedAt(group.getCreatedAt());
        resp.setUpdatedAt(group.getUpdatedAt());
        return resp;
    }
}
