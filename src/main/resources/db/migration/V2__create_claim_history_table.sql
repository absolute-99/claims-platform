CREATE TABLE claim_history (
    id UUID PRIMARY KEY,

    claim_id UUID NOT NULL,

    from_status VARCHAR(30),

    to_status VARCHAR(30) NOT NULL,

    changed_by UUID,

    changed_at TIMESTAMP NOT NULL,

    reason VARCHAR(500),

    CONSTRAINT fk_claim_history_claim
        FOREIGN KEY (claim_id)
        REFERENCES claims(id)
);

CREATE INDEX idx_claim_history_claim_id
    ON claim_history (claim_id, changed_at);