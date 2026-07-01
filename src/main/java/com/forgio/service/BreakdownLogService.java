package com.forgio.service;

import com.forgio.dto.request.BreakdownLogRequest;
import com.forgio.dto.response.BreakdownLogResponse;
import com.forgio.entity.BreakdownLog;
import com.forgio.entity.Factory;
import com.forgio.entity.Machine;
import com.forgio.repository.BreakdownLogRepository;
import com.forgio.security.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BreakdownLogService {

    private final BreakdownLogRepository breakdownLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public BreakdownLogService(BreakdownLogRepository breakdownLogRepository) {
        this.breakdownLogRepository = breakdownLogRepository;
    }

    public List<BreakdownLogResponse> getAllLogs() {
        // 1. Read factory strictly from token context
        UUID factoryId = TenantContext.getFactoryId();
        
        // 2. Scope every database call with that factoryId
        return breakdownLogRepository.findByFactory_FactoryId(factoryId).stream()
                .map(log -> new BreakdownLogResponse(
                        log.getLogId(),
                        log.isResolved() ? "RESOLVED" : "PENDING",
                        "Machine issue: " + log.getCause()
                )).collect(Collectors.toList());
    }

    @Transactional
    public BreakdownLogResponse createLog(BreakdownLogRequest request) {
        // 3. Never read factoryId from the request body or URL
        UUID factoryId = TenantContext.getFactoryId();

        // 4. Safely get references directly from JPA context to avoid setter name mismatches
        Machine machineReference = entityManager.getReference(Machine.class, request.getMachineId());
        Factory factoryReference = entityManager.getReference(Factory.class, factoryId);

        // Uses your unchangeable entity's exact Lombok builder layout
        BreakdownLog log = BreakdownLog.builder()
                .machine(machineReference)
                .factory(factoryReference)
                .cause(request.getDescription())
                .startTime(Instant.now())
                .resolved(false)
                .build();

        BreakdownLog saved = breakdownLogRepository.save(log);
        
        return new BreakdownLogResponse(
                saved.getLogId(),
                "CREATED",
                "Breakdown logged successfully"
        );
    }
}
