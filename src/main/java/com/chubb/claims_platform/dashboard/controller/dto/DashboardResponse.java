package com.chubb.claims_platform.dashboard.controller.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    private long totalClaims;
    private long submitted;
    private long assigned;
    private long underReview;
    private long informationRequired;
    private long approved;
    private long rejected;
    private long settled;
    private BigDecimal outstandingLiability;

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getSubmitted() {
        return submitted;
    }

    public void setSubmitted(long submitted) {
        this.submitted = submitted;
    }

    public long getAssigned() {
        return assigned;
    }

    public void setAssigned(long assigned) {
        this.assigned = assigned;
    }

    public long getUnderReview() {
        return underReview;
    }

    public void setUnderReview(long underReview) {
        this.underReview = underReview;
    }

    public long getInformationRequired() {
        return informationRequired;
    }

    public void setInformationRequired(long informationRequired) {
        this.informationRequired = informationRequired;
    }

    public long getApproved() {
        return approved;
    }

    public void setApproved(long approved) {
        this.approved = approved;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public long getSettled() {
        return settled;
    }

    public void setSettled(long settled) {
        this.settled = settled;
    }

    public BigDecimal getOutstandingLiability() {
        return outstandingLiability;
    }

    public void setOutstandingLiability(BigDecimal outstandingLiability) {
        this.outstandingLiability = outstandingLiability;
    }
}