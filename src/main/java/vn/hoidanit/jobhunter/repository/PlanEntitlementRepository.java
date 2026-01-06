package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.hoidanit.jobhunter.domain.PlanEntitlement;
import vn.hoidanit.jobhunter.util.constant.AccessAction;

import java.util.List;
import java.util.Optional;

public interface PlanEntitlementRepository extends JpaRepository<PlanEntitlement, Long> {

    @Query("SELECT pe FROM PlanEntitlement pe WHERE pe.plan.id = :planId")
    List<PlanEntitlement> findAllByPlanId(Long planId);

    @Query("SELECT pe FROM PlanEntitlement pe WHERE pe.plan.id = :planId AND pe.action = :action")
    Optional<PlanEntitlement> findByPlanAndAction(Long planId, AccessAction action);
}
