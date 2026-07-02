package com.forgio.repository;

import com.forgio.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByFactory_FactoryIdOrderBySentAtDesc(UUID factoryId);
}