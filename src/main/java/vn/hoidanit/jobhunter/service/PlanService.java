package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.Plan;
import vn.hoidanit.jobhunter.repository.PlanRepository;
import vn.hoidanit.jobhunter.domain.request.PlanRequest;

import java.util.List;

@Service
public class PlanService {
    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> findAll() {
        return planRepository.findAll();
    }

    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));
    }

    @Transactional
    public Plan create(PlanRequest req) {
        if (req.getCode() == null || req.getCode().isBlank())
            throw new RuntimeException("PLAN_CODE_REQUIRED");

        if (planRepository.findByCode(req.getCode()).isPresent())
            throw new RuntimeException("PLAN_CODE_EXISTS");

        Plan p = Plan.builder()
                .code(req.getCode())
                .name(req.getName())
                .audience(req.getAudience())
                .tier(req.getTier())
                .billingCycle(req.getBillingCycle())
                .durationMonths(req.getDurationMonths())
                .price(req.getPrice())
                .isActive(req.getIsActive() == null ? true : req.getIsActive())
                .build();
        return planRepository.save(p);
    }

    @Transactional
    public Plan update(Long id, PlanRequest req) {
        Plan p = findById(id);

        if (req.getName() != null) p.setName(req.getName());
        if (req.getAudience() != null) p.setAudience(req.getAudience());
        if (req.getTier() != null) p.setTier(req.getTier());
        if (req.getBillingCycle() != null) p.setBillingCycle(req.getBillingCycle());
        if (req.getDurationMonths() > 0) p.setDurationMonths(req.getDurationMonths());
        if (req.getPrice() != null) p.setPrice(req.getPrice());
        if (req.getIsActive() != null) p.setActive(req.getIsActive());

        return planRepository.save(p);

    }

    @Transactional
    public void delete(Long id) {
        planRepository.deleteById(id);
    }
}
