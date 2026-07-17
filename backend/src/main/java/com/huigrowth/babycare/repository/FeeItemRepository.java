package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.FeeItem;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeItemRepository extends JpaRepository<FeeItem, Long> {

    List<FeeItem> findByOrganizationOrderByCreatedAtDesc(Organization organization);
}
