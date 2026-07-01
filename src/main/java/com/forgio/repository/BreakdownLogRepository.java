package com.forgio.repository;

import com.forgio.entity.BreakdownLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BreakdownLogRepository extends JpaRepository<BreakdownLog, UUID> {
    
    List<BreakdownLog> findByFactory_FactoryId(UUID factoryId);
    Optional<BreakdownLog> findByLogIdAndFactory_FactoryId(UUID logId, UUID factoryId);
}
