CREATE TABLE market_transactions (
    transaction_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id            UUID NOT NULL REFERENCES market_listings(listing_id),
    buyer_factory_id      UUID NOT NULL REFERENCES factories(factory_id),
    buyer_user_id         UUID NOT NULL REFERENCES users(user_id),
    destination_branch_id UUID NOT NULL REFERENCES branches(branch_id),
    quantity              DECIMAL(12,3) NOT NULL,
    amount_ghs            DECIMAL(12,2) NOT NULL,
    paystack_reference    VARCHAR(100) NOT NULL UNIQUE,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_market_tx_reference ON market_transactions(paystack_reference);
CREATE INDEX idx_market_tx_buyer     ON market_transactions(buyer_factory_id);