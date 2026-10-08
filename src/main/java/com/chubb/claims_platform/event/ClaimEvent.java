package com.chubb.claims_platform.claim.event;

import com.chubb.claims_platform.claim.entity.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ClaimEvent {

    private UUID claimId;
    private String claimNumber;
    private ClaimStatus fromStatus;
    private ClaimStatus toStatus;
    private UUID changedBy;
    private BigDecimal estimatedLiability;
    private LocalDateTime occurredAt;

    public ClaimEvent() {
    }

    public ClaimEvent(
            UUID claimId,
            String claimNumber,
            ClaimStatus fromStatus,
            ClaimStatus toStatus,
            UUID changedBy,
            BigDecimal estimatedLiability,
            LocalDateTime occurredAt
    ) {
        this.claimId = claimId;
        this.claimNumber = claimNumber;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.estimatedLiability = estimatedLiability;
        this.occurredAt = occurredAt;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
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

    public BigDecimal getEstimatedLiability() {
        return estimatedLiability;
    }

    public void setEstimatedLiability(BigDecimal estimatedLiability) {
        this.estimatedLiability = estimatedLiability;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}