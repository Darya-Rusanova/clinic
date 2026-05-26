package com.example.clinic.repository;

import com.example.clinic.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByClient_UserIdOrderByCreatedAtDesc(Integer clientId, Pageable pageable);

    long countByClient_UserIdAndIsReadFalse(Integer clientId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.client.userId = :clientId AND n.isRead = false")
    void markAllAsRead(Integer clientId);
}