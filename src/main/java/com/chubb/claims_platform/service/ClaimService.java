package com.chubb.claims_platform.claim.service;

import com.chubb.claims_platform.claim.dto.ClaimHistoryResponse;
import com.chubb.claims_platform.claim.dto.ClaimResponse;
import com.chubb.claims_platform.claim.dto.CreateClaimRequest;
import com.chubb.claims_platform.claim.entity.Claim;
import com.chubb.claims_platform.claim.entity.ClaimHistory;
import com.chubb.claims_platform.claim.entity.ClaimStatus;
import com.chubb.claims_platform.claim.event.ClaimEvent;
import com.chubb.claims_platform.claim.event.ClaimEventPublisher;
import com.chubb.claims_platform.claim.repository.ClaimHistoryRepository;
import com.chubb.claims_platform.claim.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository claimHistoryRepository;
    private final ClaimEventPublisher claimEventPublisher;

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimHistoryRepository claimHistoryRepository,
            ClaimEventPublisher claimEventPublisher
    ) {
        this.claimRepository = claimRepository;
        this.claimHistoryRepository = claimHistoryRepository;
        this.claimEventPublisher = claimEventPublisher;
    }

    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request) {

        Claim claim = new Claim();

        claim.setId(UUID.randomUUID());
        claim.setClaimantId(request.getClaimantId());
        claim.setMarket(request.getMarket());
        claim.setClaimType(request.getClaimType());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setDescription(request.getDescription());
        claim.setEstimatedLiability(request.getEstimatedLiability());

        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimNumber("CLM-" + UUID.randomUUID());

        LocalDateTime now = LocalDateTime.now();

        claim.setCreatedAt(now);
        claim.setUpdatedAt(now);

        Claim savedClaim = claimRepository.save(claim);

        saveHistory(
                savedClaim,
                null,
                ClaimStatus.SUBMITTED,
                null,
                "Claim submitted"
        );

        return toResponse(savedClaim);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimByClaimNumber(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        return toResponse(claim);
    }

    // =========================
    // WORK QUEUE
    // =========================

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByStatus(ClaimStatus status) {

        return claimRepository
                .findByStatusOrderByCreatedAtAsc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByOfficer(UUID officerId) {

        return claimRepository
                .findByAssignedToOrderByCreatedAtAsc(officerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimHistoryResponse> getClaimHistory(
            String claimNumber
    ) {

        Claim claim = findClaim(claimNumber);

        return claimHistoryRepository
                .findByClaimIdOrderByChangedAtAsc(claim.getId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public ClaimResponse assignClaim(
            String claimNumber,
            UUID officerId
    ) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.SUBMITTED,
                ClaimStatus.ASSIGNED
        );

        claim.setAssignedTo(officerId);

        changeStatus(
                claim,
                ClaimStatus.ASSIGNED,
                officerId,
                "Claim assigned to officer"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse startReview(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.ASSIGNED,
                ClaimStatus.UNDER_REVIEW
        );

        changeStatus(
                claim,
                ClaimStatus.UNDER_REVIEW,
                claim.getAssignedTo(),
                "Claim review started"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse requestInformation(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.INFORMATION_REQUIRED
        );

        changeStatus(
                claim,
                ClaimStatus.INFORMATION_REQUIRED,
                claim.getAssignedTo(),
                "Additional information requested"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse resumeReview(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.INFORMATION_REQUIRED,
                ClaimStatus.UNDER_REVIEW
        );

        changeStatus(
                claim,
                ClaimStatus.UNDER_REVIEW,
                claim.getAssignedTo(),
                "Additional information received; review resumed"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse approveClaim(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.APPROVED
        );

        changeStatus(
                claim,
                ClaimStatus.APPROVED,
                claim.getAssignedTo(),
                "Claim approved"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse rejectClaim(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.UNDER_REVIEW,
                ClaimStatus.REJECTED
        );

        changeStatus(
                claim,
                ClaimStatus.REJECTED,
                claim.getAssignedTo(),
                "Claim rejected"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse settleClaim(String claimNumber) {

        Claim claim = findClaim(claimNumber);

        validateTransition(
                claim,
                ClaimStatus.APPROVED,
                ClaimStatus.SETTLED
        );

        changeStatus(
                claim,
                ClaimStatus.SETTLED,
                claim.getAssignedTo(),
                "Claim settled"
        );

        Claim savedClaim = claimRepository.save(claim);

        return toResponse(savedClaim);
    }

    private void changeStatus(
            Claim claim,
            ClaimStatus newStatus,
            UUID changedBy,
            String reason
    ) {

        ClaimStatus previousStatus = claim.getStatus();

        claim.setStatus(newStatus);
        claim.setUpdatedAt(LocalDateTime.now());

        saveHistory(
                claim,
                previousStatus,
                newStatus,
                changedBy,
                reason
        );

        // Publish lifecycle event to Kafka
        ClaimEvent event = new ClaimEvent(
                claim.getId(),
                claim.getClaimNumber(),
                previousStatus,
                newStatus,
                changedBy,
                claim.getEstimatedLiability(),
                LocalDateTime.now()
        );

        claimEventPublisher.publish(event);
    }

    private void saveHistory(
            Claim claim,
            ClaimStatus fromStatus,
            ClaimStatus toStatus,
            UUID changedBy,
            String reason
    ) {

        ClaimHistory history = new ClaimHistory();

        history.setId(UUID.randomUUID());
        history.setClaimId(claim.getId());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);

        claimHistoryRepository.save(history);
    }

    private Claim findClaim(String claimNumber) {

        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Claim not found: " + claimNumber
                        )
                );
    }

    private void validateTransition(
            Claim claim,
            ClaimStatus expectedCurrentStatus,
            ClaimStatus targetStatus
    ) {

        if (claim.getStatus() != expectedCurrentStatus) {

            throw new IllegalStateException(
                    "Invalid claim transition. Claim "
                            + claim.getClaimNumber()
                            + " is currently "
                            + claim.getStatus()
                            + " and cannot move to "
                            + targetStatus
            );
        }
    }

    private ClaimResponse toResponse(Claim claim) {

        ClaimResponse response = new ClaimResponse();

        response.setId(claim.getId());
        response.setClaimNumber(claim.getClaimNumber());
        response.setClaimantId(claim.getClaimantId());
        response.setMarket(claim.getMarket());
        response.setClaimType(claim.getClaimType());
        response.setIncidentDate(claim.getIncidentDate());
        response.setDescription(claim.getDescription());
        response.setStatus(claim.getStatus());
        response.setEstimatedLiability(claim.getEstimatedLiability());
        response.setAssignedTo(claim.getAssignedTo());

        return response;
    }

    private ClaimHistoryResponse toHistoryResponse(
            ClaimHistory history
    ) {

        ClaimHistoryResponse response =
                new ClaimHistoryResponse();

        response.setId(history.getId());
        response.setClaimId(history.getClaimId());
        response.setFromStatus(history.getFromStatus());
        response.setToStatus(history.getToStatus());
        response.setChangedBy(history.getChangedBy());
        response.setChangedAt(history.getChangedAt());
        response.setReason(history.getReason());

        return response;
    }
}