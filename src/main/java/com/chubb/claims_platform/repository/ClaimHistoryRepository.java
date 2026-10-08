package com.chubb.claims_platform.claim.repository;

import com.chubb.claims_platform.claim.entity.ClaimHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimHistoryRepository
        extends JpaRepository<ClaimHistory, UUID> {

    List<ClaimHistory> findByClaimIdOrderByChangedAtAsc(UUID claimId);
}