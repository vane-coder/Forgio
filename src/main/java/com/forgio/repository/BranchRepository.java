package com.forgio.repository;

import com.forgio.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    // All branches of my company, oldest first (the oldest is the main branch)
    List<Branch> findByCompany_CompanyIdOrderByCreatedAtAsc(UUID companyId);

    // A specific branch that belongs to my company
    Optional<Branch> findByBranchIdAndCompany_CompanyId(UUID branchId, UUID companyId);
}
