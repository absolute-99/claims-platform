package com.chubb.claims_platform.claim.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreateClaimRequest {

    @NotNull(message = "claimantId is required")
    private UUID claimantId;

    @NotBlank(message = "market is required")
    @Size(max = 10, message = "market must not exceed 10 characters")
    private String market;

    @NotBlank(message = "claimType is required")
    @Size(max = 20, message = "claimType must not exceed 20 characters")
    private String claimType;

    @NotNull(message = "incidentDate is required")
    private LocalDate incidentDate;

    @Size(max = 5000, message = "description must not exceed 5000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "estimatedLiability must be greater than or equal to 0")
    private BigDecimal estimatedLiability;

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

    public BigDecimal getEstimatedLiability() {
        return estimatedLiability;
    }

    public void setEstimatedLiability(BigDecimal estimatedLiability) {
        this.estimatedLiability = estimatedLiability;
    }
}