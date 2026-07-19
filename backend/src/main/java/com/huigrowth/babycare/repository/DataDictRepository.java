package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.DataDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataDictRepository extends JpaRepository<DataDict, Long> {

    List<DataDict> findByDictTypeOrderBySortOrderAsc(String dictType);

    List<DataDict> findByStatusOrderBySortOrderAsc(DataDict.DictStatus status);

    Optional<DataDict> findByDictTypeAndItemCode(String dictType, String itemCode);

    boolean existsByDictTypeAndItemCode(String dictType, String itemCode);

    @Query("SELECT DISTINCT d.dictType FROM DataDict d ORDER BY d.dictType")
    List<String> findDistinctDictTypeBy();
}
