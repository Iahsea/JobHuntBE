package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.Plan;
import vn.hoidanit.jobhunter.domain.PlanEntitlement;
import vn.hoidanit.jobhunter.repository.PlanEntitlementRepository;
import vn.hoidanit.jobhunter.repository.PlanRepository;
import vn.hoidanit.jobhunter.domain.request.PlanEntitlementRequest;

import java.util.List;

@Service
public class PlanEntitlementService {

    private final PlanEntitlementRepository repo;
    private final PlanRepository planRepo;

    public PlanEntitlementService(PlanEntitlementRepository repo, PlanRepository planRepo) {
        this.repo = repo;
        this.planRepo = planRepo;
    }

    public List<PlanEntitlement> getByPlan(Long planId) {
        return repo.findAllByPlanId(planId);
    }

    public PlanEntitlement getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("ENTITLEMENT_NOT_FOUND"));
    }

    @Transactional
    public PlanEntitlement create(PlanEntitlementRequest req) {
        if (req.getPlanId() == null) throw new RuntimeException("PLAN_ID_REQUIRED");
        if (req.getAction() == null) throw new RuntimeException("ACTION_REQUIRED");

        Plan plan = planRepo.findById(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));

        repo.findByPlanAndAction(plan.getId(), req.getAction())
                .ifPresent(x -> { throw new RuntimeException("ENTITLEMENT_EXISTS"); });

        PlanEntitlement pe = PlanEntitlement.builder()
                .plan(plan)
                .action(req.getAction())
                .quotaPerPeriod(req.getQuotaPerPeriod() == null ? -1 : req.getQuotaPerPeriod())
                .consumeUnit(req.getConsumeUnit() == null ? 1 : req.getConsumeUnit())
                .build();

        return repo.save(pe);
    }

    @Transactional
    public PlanEntitlement update(Long id, PlanEntitlementRequest req) {
        PlanEntitlement pe = getById(id);
        if (req.getQuotaPerPeriod() != null) pe.setQuotaPerPeriod(req.getQuotaPerPeriod());
        if (req.getConsumeUnit() != null) pe.setConsumeUnit(req.getConsumeUnit());
        return repo.save(pe);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
