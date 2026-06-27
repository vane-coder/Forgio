-- Departments
CREATE TABLE departments (
    dept_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id   UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for tenant-scoped lookups
CREATE INDEX idx_departments_factory ON departments(factory_id);