package com.forgio.service;

import com.forgio.dto.request.BranchRequest;
import com.forgio.dto.response.BranchResponse;
import com.forgio.entity.Branch;
import com.forgio.entity.Company;
import com.forgio.entity.Factory;
import com.forgio.entity.User;
import com.forgio.enums.SubscriptionPlan;
import com.forgio.exception.ResourceNotFoundException;
import com.forgio.repository.BranchRepository;
import com.forgio.repository.CompanyRepository;
import com.forgio.repository.FactoryRepository;
import com.forgio.repository.MachineRepository;
import com.forgio.repository.UserRepository;
import com.forgio.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;
    private final MachineRepository machineRepository;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /** List my company's branches, main branch first. */
    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches() {
        Factory myFactory = factoryRepository.findById(TenantContext.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        if (myFactory.getCompany() == null) {
            return List.of();
        }

        List<Branch> branches = branchRepository
                .findByCompany_CompanyIdOrderByCreatedAtAsc(myFactory.getCompany().getCompanyId());

        UUID mainBranchId = branches.isEmpty() ? null : branches.get(0).getBranchId();
        return branches.stream()
                .map(b -> toResponse(b, b.getBranchId().equals(mainBranchId)))
                .toList();
    }

    /** Create a new branch under my company */
    @Transactional
    public BranchResponse createBranch(BranchRequest req) {
        Factory myFactory = factoryRepository.findById(TenantContext.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));
        User creator = currentUser();

        Company company = myFactory.getCompany();
        if (company == null) {
            company = companyRepository.save(Company.builder().name(myFactory.getName()).build());

            myFactory.setCompany(company);
            factoryRepository.save(myFactory);

            Branch mainBranch = Branch.builder()
                    .company(company)
                    .factory(myFactory)
                    .name(myFactory.getName())
                    .location(myFactory.getLocation())
                    .build();
            branchRepository.save(mainBranch);
        }

        Factory newFactory = Factory.builder()
                .name(req.name())
                .location(req.location())
                .plan(SubscriptionPlan.BASIC)
                .active(true)
                .company(company)
                .build();
        newFactory = factoryRepository.save(newFactory);

        Branch branch = Branch.builder()
                .company(company)
                .factory(newFactory)
                .name(req.name())
                .location(req.location())
                .manager(creator)
                .build();
        branch = branchRepository.save(branch);

        return toResponse(branch, false);
    }

    private BranchResponse toResponse(Branch branch, boolean isMain) {
        UUID branchFactoryId = branch.getFactory().getFactoryId();
        long workerCount = userRepository.findByFactory_FactoryId(branchFactoryId).size();
        long machineCount = machineRepository.findByFactory_FactoryId(branchFactoryId).size();

        return new BranchResponse(
                branch.getBranchId(),
                branch.getName(),
                branch.getLocation(),
                workerCount,
                machineCount,
                isMain);
    }
}
