package com.chubb.claims_platform.claim.controller;

import com.chubb.claims_platform.claim.dto.ClaimHistoryResponse;
import com.chubb.claims_platform.claim.dto.ClaimResponse;
import com.chubb.claims_platform.claim.dto.CreateClaimRequest;
import com.chubb.claims_platform.claim.entity.ClaimStatus;
import com.chubb.claims_platform.claim.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request
    ) {
        ClaimResponse response = claimService.createClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) UUID assignedTo
    ) {

        if (status != null) {
            return ResponseEntity.ok(
                    claimService.getClaimsByStatus(status)
            );
        }

        if (assignedTo != null) {
            return ResponseEntity.ok(
                    claimService.getClaimsByOfficer(assignedTo)
            );
        }

        return ResponseEntity.ok(
                claimService.getClaimsByStatus(ClaimStatus.SUBMITTED)
        );
    }

    @GetMapping("/{claimNumber}")
    public ResponseEntity<ClaimResponse> getClaim(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.getClaimByClaimNumber(claimNumber);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimNumber}/history")
    public ResponseEntity<List<ClaimHistoryResponse>> getClaimHistory(
            @PathVariable String claimNumber
    ) {
        List<ClaimHistoryResponse> history =
                claimService.getClaimHistory(claimNumber);

        return ResponseEntity.ok(history);
    }

    @PostMapping("/{claimNumber}/assign")
    public ResponseEntity<ClaimResponse> assignClaim(
            @PathVariable String claimNumber,
            @RequestParam UUID officerId
    ) {
        ClaimResponse response =
                claimService.assignClaim(claimNumber, officerId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/review")
    public ResponseEntity<ClaimResponse> startReview(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.startReview(claimNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/information-request")
    public ResponseEntity<ClaimResponse> requestInformation(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.requestInformation(claimNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/resume-review")
    public ResponseEntity<ClaimResponse> resumeReview(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.resumeReview(claimNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.approveClaim(claimNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.rejectClaim(claimNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimNumber}/settle")
    public ResponseEntity<ClaimResponse> settleClaim(
            @PathVariable String claimNumber
    ) {
        ClaimResponse response =
                claimService.settleClaim(claimNumber);

        return ResponseEntity.ok(response);
    }
}