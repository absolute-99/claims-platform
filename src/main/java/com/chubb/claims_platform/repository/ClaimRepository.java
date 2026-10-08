package com.chubb.claims_platform.claim.repository;

import com.chubb.claims_platform.claim.entity.Claim;
import com.chubb.claims_platform.claim.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);

    List<Claim> findByAssignedToAndStatusOrderByCreatedAtAsc(
            UUID assignedTo,
            ClaimStatus status
    );

    List<Claim> findByAssignedToOrderByCreatedAtAsc(UUID assignedTo);

    long countByStatus(ClaimStatus status);

    @Query("""
            SELECT COALESCE(SUM(c.estimatedLiability), 0)
            FROM Claim c
            WHERE c.status NOT IN (
                com.chubb.claims_platform.claim.entity.ClaimStatus.REJECTED,
                com.chubb.claims_platform.claim.entity.ClaimStatus.SETTLED
            )
            """)
    BigDecimal calculateOutstandingLiability();
}