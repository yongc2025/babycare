package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Announcement;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    List<Announcement> findByClassroomOrderByCreatedAtDesc(Classroom classroom);

    List<Announcement> findByOrganizationAndStatusOrderByPublishedAtDesc(
            Organization organization,
            Announcement.AnnouncementStatus status);
}
