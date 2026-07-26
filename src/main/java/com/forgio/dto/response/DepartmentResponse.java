
package com.forgio.dto.response;

import java.util.UUID;
package com.forgio.dto.response;

import java.util.UUID;

public record DepartmentResponse(
        UUID deptId,
        String name,
        String headName,
        int workerCount
) {}

public record DepartmentResponse(
        UUID deptId,
        String name,
        String headName,
        int workerCount
) {}
