package com.chubb.claims_platform.claim.dto;

import com.chubb.claims_platform.claim.entity.ClaimStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClaimHistoryResponse {

    private UUID id;

    private UUID claimId;

    private ClaimStatus fromStatus;

    private ClaimStatus toStatus;

    private UUID changedBy;

    private LocalDateTime changedAt;

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
