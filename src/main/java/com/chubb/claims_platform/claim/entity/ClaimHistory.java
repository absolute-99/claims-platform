package com.chubb.claims_platform.claim.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "claim_history",
        indexes = {
                @Index(
                        name = "idx_claim_history_claim_id",
                        columnList = "claim_id, changed_at"
                )
        }
)
public class ClaimHistory {

    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ClaimStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private ClaimStatus toStatus;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 500)
    private String reason;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public ClaimStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ClaimStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ClaimStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ClaimStatus toStatus) {
        this.toStatus = toStatus;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}