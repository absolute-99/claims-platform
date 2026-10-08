package com.chubb.claims_platform.claim.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "claims",
        indexes = {
                @Index(
                        name = "idx_claims_work_queue",
                        columnList = "assigned_to, status, created_at"
                ),
                @Index(
                        name = "idx_claims_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_claims_market",
                        columnList = "market"
                )
        }
)
public class Claim {

    @Id
    private UUID id;

    @Column(name = "claim_number", nullable = false, unique = true)
    private String claimNumber;

    @Column(name = "claimant_id", nullable = false)
    private UUID claimantId;

    @Column(nullable = false, length = 10)
    private String market;

    @Column(name = "claim_type", nullable = false, length = 20)
    private String claimType;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @Column(name = "estimated_liability", precision = 15, scale = 2)
    private BigDecimal estimatedLiability;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}