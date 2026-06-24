-- ============================================================
-- Forgio Database Schema – V1 Initial Migration
-- Multi-tenant: every company-scoped table includes factory_id
-- ============================================================

-- Enums
CREATE TYPE user_role AS ENUM ('MANAGER', 'DEPT_HEAD', 'WORKER', 'DRIVER', 'SYSTEM_ADMIN');
CREATE TYPE machine_status AS ENUM ('RUNNING', 'STOPPED', 'MAINTENANCE');
CREATE TYPE shipment_status AS ENUM ('PENDING', 'DEPARTED', 'IN_TRANSIT', 'ARRIVED');
CREATE TYPE notification_type AS ENUM ('MEETING', 'ALERT', 'WEATHER', 'GENERAL');
CREATE TYPE listing_status AS ENUM ('ACTIVE', 'SOLD', 'CANCELLED');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');
CREATE TYPE subscription_plan AS ENUM ('BASIC', 'PRO');

-- ── Factories (top-level tenant) ─────────────────────────────
CREATE TABLE factories (
    factory_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    location      VARCHAR(500),
    industry      VARCHAR(100),
    plan          subscription_plan NOT NULL DEFAULT 'BASIC',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Companies (multi-branch support) ─────────────────────────
-- A factory belongs to a company; one company can own many factories (branches).
CREATE TABLE companies (
    company_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE factories ADD COLUMN company_id UUID REFERENCES companies(company_id);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    user_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id     UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL,
    phone          VARCHAR(20) UNIQUE NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           user_role NOT NULL DEFAULT 'WORKER',
    department_id  UUID,                        -- FK added after departments table
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    fcm_token      VARCHAR(500),                -- Firebase device token for push notifications
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_factory ON users(factory_id);
CREATE INDEX idx_users_phone ON users(phone);

-- ── Permissions (fine-grained per user) ──────────────────────
CREATE TABLE permissions (
    perm_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    can_view_reports        BOOLEAN DEFAULT FALSE,
    can_manage_workers      BOOLEAN DEFAULT FALSE,
    can_approve_marketplace BOOLEAN DEFAULT FALSE,
    can_send_notifications  BOOLEAN DEFAULT FALSE,
    can_manage_machines     BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, factory_id)
);

-- ── Departments ───────────────────────────────────────────────
CREATE TABLE departments (
    dept_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    head_user_id  UUID REFERENCES users(user_id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_departments_factory ON departments(factory_id);

-- Back-fill FK on users
ALTER TABLE users ADD CONSTRAINT fk_users_department
    FOREIGN KEY (department_id) REFERENCES departments(dept_id);

-- ── Refresh Tokens ────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    token_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token         VARCHAR(1000) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ NOT NULL,
    revoked       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Raw Materials ─────────────────────────────────────────────
CREATE TABLE raw_materials (
    material_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id        UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    unit              VARCHAR(50) NOT NULL,
    quantity_in_stock DECIMAL(12,3) NOT NULL DEFAULT 0,
    reorder_level     DECIMAL(12,3) NOT NULL DEFAULT 0,
    cost_per_unit     DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_materials_factory ON raw_materials(factory_id);

-- ── Production Entries ────────────────────────────────────────
CREATE TABLE production_entries (
    entry_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id         UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    worker_id          UUID NOT NULL REFERENCES users(user_id),
    dept_id            UUID REFERENCES departments(dept_id),
    product_name       VARCHAR(255) NOT NULL,
    quantity_produced  DECIMAL(12,3) NOT NULL,
    shift              VARCHAR(50),
    entry_date         DATE NOT NULL DEFAULT CURRENT_DATE,
    is_locked          BOOLEAN NOT NULL DEFAULT FALSE,  -- locked 24h after submission
    notes              TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_production_factory ON production_entries(factory_id);
CREATE INDEX idx_production_date    ON production_entries(entry_date);
CREATE INDEX idx_production_worker  ON production_entries(worker_id);

-- ── Material Usage ────────────────────────────────────────────
CREATE TABLE material_usage (
    usage_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id      UUID NOT NULL REFERENCES production_entries(entry_id) ON DELETE CASCADE,
    material_id   UUID NOT NULL REFERENCES raw_materials(material_id),
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    quantity_used DECIMAL(12,3) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usage_factory ON material_usage(factory_id);

-- ── Waste Records ─────────────────────────────────────────────
CREATE TABLE waste_records (
    waste_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id      UUID NOT NULL REFERENCES production_entries(entry_id) ON DELETE CASCADE,
    material_id   UUID NOT NULL REFERENCES raw_materials(material_id),
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    waste_amount  DECIMAL(12,3) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_waste_factory ON waste_records(factory_id);

-- ── Machines ──────────────────────────────────────────────────
CREATE TABLE machines (
    machine_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id        UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    status            machine_status NOT NULL DEFAULT 'RUNNING',
    last_service_date DATE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_machines_factory ON machines(factory_id);

-- ── Breakdown Logs ────────────────────────────────────────────
CREATE TABLE breakdown_logs (
    log_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    machine_id    UUID NOT NULL REFERENCES machines(machine_id) ON DELETE CASCADE,
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    reported_by   UUID REFERENCES users(user_id),
    cause         TEXT,
    start_time    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_time      TIMESTAMPTZ,
    resolved      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_breakdowns_factory  ON breakdown_logs(factory_id);
CREATE INDEX idx_breakdowns_machine  ON breakdown_logs(machine_id);

-- ── Notifications ─────────────────────────────────────────────
CREATE TABLE notifications (
    notif_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    sent_by       UUID REFERENCES users(user_id),
    target_role   user_role,
    target_dept   UUID REFERENCES departments(dept_id),
    message       TEXT NOT NULL,
    type          notification_type NOT NULL DEFAULT 'GENERAL',
    sent_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifs_factory ON notifications(factory_id);

-- ── News Feed ─────────────────────────────────────────────────
CREATE TABLE news_feed (
    post_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id    UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    author_id     UUID REFERENCES users(user_id),
    content       TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_feed_factory ON news_feed(factory_id);

-- ── Marketplace Listings ──────────────────────────────────────
CREATE TABLE market_listings (
    listing_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    factory_id     UUID NOT NULL REFERENCES factories(factory_id) ON DELETE CASCADE,
    material_id    UUID NOT NULL REFERENCES raw_materials(material_id),
    quantity       DECIMAL(12,3) NOT NULL,
    price_per_unit DECIMAL(12,2) NOT NULL,
    status         listing_status NOT NULL DEFAULT 'ACTIVE',
    description    TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_listings_factory ON market_listings(factory_id);
CREATE INDEX idx_listings_status  ON market_listings(status);

-- ── Marketplace Transactions ──────────────────────────────────
CREATE TABLE market_transactions (
    tx_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_factory_id   UUID NOT NULL REFERENCES factories(factory_id),
    seller_factory_id  UUID NOT NULL REFERENCES factories(factory_id),
    listing_id         UUID NOT NULL REFERENCES market_listings(listing_id),
    quantity_purchased DECIMAL(12,3) NOT NULL,
    amount             DECIMAL(12,2) NOT NULL,
    status             transaction_status NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Advertisements ────────────────────────────────────────────
CREATE TABLE advertisements (
    ad_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name     VARCHAR(255) NOT NULL,
    content          TEXT NOT NULL,
    target_industry  VARCHAR(100),
    start_date       DATE NOT NULL,
    end_date         DATE NOT NULL,
    impressions      INT NOT NULL DEFAULT 0,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Branches ──────────────────────────────────────────────────
CREATE TABLE branches (
    branch_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id   UUID NOT NULL REFERENCES companies(company_id) ON DELETE CASCADE,
    factory_id   UUID NOT NULL REFERENCES factories(factory_id),
    name         VARCHAR(255) NOT NULL,
    location     VARCHAR(500),
    manager_id   UUID REFERENCES users(user_id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_branches_company ON branches(company_id);

-- ── Shipments ─────────────────────────────────────────────────
CREATE TABLE shipments (
    shipment_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_branch_id   UUID NOT NULL REFERENCES branches(branch_id),
    to_branch_id     UUID NOT NULL REFERENCES branches(branch_id),
    driver_id        UUID REFERENCES users(user_id),
    company_id       UUID NOT NULL REFERENCES companies(company_id),
    status           shipment_status NOT NULL DEFAULT 'PENDING',
    notes            TEXT,
    departed_at      TIMESTAMPTZ,
    arrived_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shipments_company ON shipments(company_id);
CREATE INDEX idx_shipments_status  ON shipments(status);

-- ── Shipment Items ────────────────────────────────────────────
CREATE TABLE shipment_items (
    item_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id  UUID NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    description  VARCHAR(500) NOT NULL,
    quantity     DECIMAL(12,3) NOT NULL,
    unit         VARCHAR(50)
);

-- ── GPS Logs ──────────────────────────────────────────────────
CREATE TABLE gps_logs (
    log_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id  UUID NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    latitude     DECIMAL(10,7) NOT NULL,
    longitude    DECIMAL(10,7) NOT NULL,
    recorded_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gps_shipment ON gps_logs(shipment_id);
CREATE INDEX idx_gps_time     ON gps_logs(recorded_at);
