package com.forgio.dto.response;

import java.util.UUID;

public record BranchResponse(
        UUID branchId,
        String name,
        String location,
        long workerCount,
        long machineCount,
        boolean isMain
) {}
