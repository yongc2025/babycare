package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.DataDictCreateRequest;
import com.huigrowth.babycare.dto.DataDictResponse;
import com.huigrowth.babycare.entity.DataDict;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.DataDictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据字典服务
 */
@Service
@RequiredArgsConstructor
public class DataDictService {

    private final DataDictRepository dataDictRepository;

    public List<String> listDictTypes() {
        return dataDictRepository.findDistinctDictTypeBy();
    }

    public List<DataDictResponse> listByType(String dictType) {
        return dataDictRepository.findByDictTypeOrderBySortOrderAsc(dictType).stream()
                .map(DataDictResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DataDictResponse create(DataDictCreateRequest request) {
        if (dataDictRepository.existsByDictTypeAndItemCode(request.getDictType(), request.getItemCode())) {
            throw new BusinessException("该字典类型下已存在相同的项编码");
        }
        DataDict d = new DataDict();
        d.setDictType(request.getDictType());
        d.setDictName(request.getDictName());
        d.setItemCode(request.getItemCode());
        d.setItemValue(request.getItemValue());
        d.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        d.setRemark(request.getRemark());
        d.setStatus(DataDict.DictStatus.ACTIVE);
        d = dataDictRepository.save(d);
        return DataDictResponse.fromEntity(d);
    }

    @Transactional
    public void delete(Long id) {
        dataDictRepository.deleteById(id);
    }

    @Transactional
    public DataDictResponse update(Long id, DataDictCreateRequest request) {
        DataDict d = dataDictRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字典项不存在"));
        if (request.getItemValue() != null) d.setItemValue(request.getItemValue());
        if (request.getDictName() != null) d.setDictName(request.getDictName());
        if (request.getSortOrder() != null) d.setSortOrder(request.getSortOrder());
        if (request.getRemark() != null) d.setRemark(request.getRemark());
        d = dataDictRepository.save(d);
        return DataDictResponse.fromEntity(d);
    }
}
