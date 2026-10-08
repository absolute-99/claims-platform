package com.chubb.claims_platform.dashboard.controller.Service;

import com.chubb.claims_platform.claim.entity.ClaimStatus;
import com.chubb.claims_platform.claim.repository.ClaimRepository;
import com.chubb.claims_platform.dashboard.controller.dto.DashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ClaimRepository claimRepository;

    public DashboardService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {

        DashboardResponse response = new DashboardResponse();

        response.setTotalClaims(claimRepository.count());

        response.setSubmitted(
                claimRepository.countByStatus(ClaimStatus.SUBMITTED)
        );

        response.setAssigned(
                claimRepository.countByStatus(ClaimStatus.ASSIGNED)
        );

        response.setUnderReview(
                claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW)
        );

        response.setInformationRequired(
                claimRepository.countByStatus(ClaimStatus.INFORMATION_REQUIRED)
        );

        response.setApproved(
                claimRepository.countByStatus(ClaimStatus.APPROVED)
        );

        response.setRejected(
                claimRepository.countByStatus(ClaimStatus.REJECTED)
        );

        response.setSettled(
                claimRepository.countByStatus(ClaimStatus.SETTLED)
        );

        response.setOutstandingLiability(
                claimRepository.calculateOutstandingLiability()
        );

        return response;
    }
}