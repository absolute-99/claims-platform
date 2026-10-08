package com.chubb.claims_platform.claim.service;

import com.chubb.claims_platform.claim.dto.ClaimResponse;
import com.chubb.claims_platform.claim.dto.CreateClaimRequest;
import com.chubb.claims_platform.claim.entity.Claim;
import com.chubb.claims_platform.claim.entity.ClaimHistory;
import com.chubb.claims_platform.claim.entity.ClaimStatus;
import com.chubb.claims_platform.claim.event.ClaimEventPublisher;
import com.chubb.claims_platform.claim.repository.ClaimHistoryRepository;
import com.chubb.claims_platform.claim.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimHistoryRepository claimHistoryRepository;

    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @InjectMocks
    private ClaimService claimService;

    private String claimNumber;
    private UUID officerId;
    private Claim claim;

    @BeforeEach
    void setUp() {
        claimNumber = "CLM-TEST-001";
        officerId = UUID.randomUUID();

        claim = new Claim();
        claim.setId(UUID.randomUUID());
        claim.setClaimNumber(claimNumber);
        claim.setClaimantId(UUID.randomUUID());
        claim.setMarket("SG");
        claim.setClaimType("MOTOR");
        claim.setIncidentDate(LocalDate.now());
        claim.setDescription("Vehicle accident");
        claim.setEstimatedLiability(new BigDecimal("25000.00"));
        claim.setStatus(ClaimStatus.SUBMITTED);
    }

    @Test
    void shouldAssignSubmittedClaim() {
        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.assignClaim(claimNumber, officerId);

        assertEquals(ClaimStatus.ASSIGNED, response.getStatus());
        assertEquals(officerId, response.getAssignedTo());

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldStartReviewForAssignedClaim() {
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim.setAssignedTo(officerId);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.startReview(claimNumber);

        assertEquals(ClaimStatus.UNDER_REVIEW, response.getStatus());

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldRequestInformationDuringReview() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setAssignedTo(officerId);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.requestInformation(claimNumber);

        assertEquals(
                ClaimStatus.INFORMATION_REQUIRED,
                response.getStatus()
        );

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldResumeReviewAfterInformationReceived() {
        claim.setStatus(ClaimStatus.INFORMATION_REQUIRED);
        claim.setAssignedTo(officerId);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.resumeReview(claimNumber);

        assertEquals(ClaimStatus.UNDER_REVIEW, response.getStatus());

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldApproveClaimUnderReview() {
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setAssignedTo(officerId);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.approveClaim(claimNumber);

        assertEquals(ClaimStatus.APPROVED, response.getStatus());

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldSettleApprovedClaim() {
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setAssignedTo(officerId);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response =
                claimService.settleClaim(claimNumber);

        assertEquals(ClaimStatus.SETTLED, response.getStatus());

        verify(claimHistoryRepository).save(any(ClaimHistory.class));
        verify(claimEventPublisher).publish(any());
    }

    @Test
    void shouldRejectInvalidTransition() {
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findByClaimNumber(claimNumber))
                .thenReturn(Optional.of(claim));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> claimService.approveClaim(claimNumber)
        );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid claim transition")
        );

        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any());
    }
}