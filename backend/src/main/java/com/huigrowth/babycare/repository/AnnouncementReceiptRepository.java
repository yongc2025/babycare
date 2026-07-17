package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Announcement;
import com.huigrowth.babycare.entity.AnnouncementReceipt;
import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementReceiptRepository extends JpaRepository<AnnouncementReceipt, Long> {

    Optional<AnnouncementReceipt> findByAnnouncementAndUser(Announcement announcement, User user);

    long countByAnnouncement(Announcement announcement);
}
