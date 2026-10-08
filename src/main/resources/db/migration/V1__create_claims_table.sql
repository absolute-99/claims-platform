CREATE TABLE claims (
    id UUID PRIMARY KEY,

    claim_number VARCHAR(50) NOT NULL UNIQUE,

    claimant_id UUID NOT NULL,

    market VARCHAR(10) NOT NULL,

    claim_type VARCHAR(20) NOT NULL,

    incident_date DATE NOT NULL,

    description TEXT,

    status VARCHAR(30) NOT NULL,

    estimated_liability NUMERIC(15, 2),

    assigned_to UUID,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_claims_work_queue
    ON claims (assigned_to, status, created_at);

CREATE INDEX idx_claims_status
    ON claims (status);

CREATE INDEX idx_claims_market
    ON claims (market);