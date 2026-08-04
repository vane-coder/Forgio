package com.forgio.repository;

import com.forgio.entity.Notification;
import com.forgio.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByFactory_FactoryIdOrderBySentAtDesc(UUID factoryId);
   List<Notification> findBySentBy_UserIdOrderBySentAtDesc(UUID userId);

    /**
     * Notifications visible to a specific recipient: broadcasts (no target) plus
     * anything targeted at the recipient's role and/or department. A null role or
     * dept parameter (recipient has none) only matches the untargeted broadcasts.
     */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.factory.factoryId = :factoryId
              AND (n.targetRole IS NULL OR n.targetRole = :role)
              AND (n.targetDept IS NULL OR n.targetDept.deptId = :deptId)
            ORDER BY n.sentAt DESC
            """)
    List<Notification> findVisibleTo(@Param("factoryId") UUID factoryId,
                                     @Param("role") UserRole role,
                                     @Param("deptId") UUID deptId);
}