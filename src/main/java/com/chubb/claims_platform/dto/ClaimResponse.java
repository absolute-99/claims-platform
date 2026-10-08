package com.chubb.claims_platform.claim.dto;

import com.chubb.claims_platform.claim.entity.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ClaimResponse {

    private UUID id;

    private String claimNumber;

    private UUID claimantId;

    private String market;

    private String claimType;

    private LocalDate incidentDate;

    private String description;

    private ClaimStatus status;

    private BigDecimal estimatedLiability;

    private UUID assignedTo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public UUID getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(UUID claimantId) {
        this.claimantId = claimantId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getClaimType() {
        return claimType;
    }

    public void setClaimType(String claimType) {
        this.claimType = claimType;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public BigDecimal getEstimatedLiability() {
        return estimatedLiability;
    }

    public void setEstimatedLiability(BigDecimal estimatedLiability) {
        this.estimatedLiability = estimatedLiability;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }
}