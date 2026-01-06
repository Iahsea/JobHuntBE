package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.Plan;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.PlanRepository;
import vn.hoidanit.jobhunter.repository.RoleRepository;
import vn.hoidanit.jobhunter.repository.SubscriptionRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.domain.request.SubscriptionRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final UserRepository userRepo;
    private final PlanRepository planRepo;
    private final RoleRepository roleRepo;

    public SubscriptionService(SubscriptionRepository subRepo, UserRepository userRepo, PlanRepository planRepo,
            RoleRepository roleRepo) {
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.planRepo = planRepo;
        this.roleRepo = roleRepo;
    }

    public List<Subscription> listByUser(Long userId) {
        return subRepo.findAllByUserId(userId);
    }

    public Subscription getById(Long id) {
        return subRepo.findById(id).orElseThrow(() -> new RuntimeException("SUB_NOT_FOUND"));
    }

    @Transactional
    public Subscription create(SubscriptionRequest req) {
        if (req.getUserId() == null)
            throw new RuntimeException("USER_ID_REQUIRED");
        if (req.getPlanId() == null)
            throw new RuntimeException("PLAN_ID_REQUIRED");

        User u = userRepo.findById(req.getUserId()).orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Plan p = planRepo.findById(req.getPlanId()).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));

        Subscription s = Subscription.builder()
                .user(u)
                .plan(p)
                .status(req.getStatus() == null ? "PENDING_PAYMENT" : req.getStatus())
                .build();

        // Nếu bạn tạo luôn ACTIVE để test:
        if ("ACTIVE".equalsIgnoreCase(s.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            s.setStartAt(now);
            s.setEndAt(now.plusMonths(p.getDurationMonths()));
            s.setCurrentPeriodStart(now);
            s.setCurrentPeriodEnd(now.plusMonths(1));
        }

        return subRepo.save(s);
    }

    @Transactional
    public Subscription update(Long id, SubscriptionRequest req) {
        Subscription s = getById(id);

        if (req.getPlanId() != null) {
            Plan p = planRepo.findById(req.getPlanId()).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));
            s.setPlan(p);
        }
        if (req.getStatus() != null) {
            s.setStatus(req.getStatus());
            if ("ACTIVE".equalsIgnoreCase(req.getStatus())) {
                Plan p = s.getPlan();
                LocalDateTime now = LocalDateTime.now();
                s.setStartAt(now);
                s.setEndAt(now.plusMonths(p.getDurationMonths()));
                s.setCurrentPeriodStart(now);
                s.setCurrentPeriodEnd(now.plusMonths(1));
            }
        }
        return subRepo.save(s);
    }

    @Transactional
    public void delete(Long id) {
        subRepo.deleteById(id);
    }

    @Transactional
    public Subscription activateSubscriptionAndInitMonthlyUsage(Long subscriptionId) {

        Subscription s = subRepo.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("SUB_NOT_FOUND"));

        // idempotent – webhook gọi nhiều lần vẫn an toàn
        if ("ACTIVE".equalsIgnoreCase(s.getStatus())) {
            return s;
        }

        Plan p = s.getPlan();
        LocalDateTime now = LocalDateTime.now();

        s.setStatus("ACTIVE");
        s.setStartAt(now);
        s.setEndAt(now.plusMonths(p.getDurationMonths()));

        s.setCurrentPeriodStart(now);
        s.setCurrentPeriodEnd(now.plusMonths(1));

        // UPDATE USER ROLE khi mua gói
        User user = s.getUser();
        upgradeUserRole(user, p);

        Subscription saved = subRepo.save(s);

        // Nếu bạn có SubscriptionUsage
        // initMonthlyUsage(saved);

        return saved;
    }

    /**
     * Upgrade role của user dựa vào plan đã mua
     * HR + plan VIP -> HR_VIP
     * USER + plan VIP -> USER_VIP
     */
    private void upgradeUserRole(User user, Plan plan) {
        String audience = plan.getAudience(); // "HR" hoặc "USER"
        String tier = plan.getTier(); // "BASIC" hoặc "VIP"

        if (audience == null || tier == null) {
            return; // không có đủ thông tin để upgrade
        }

        String newRoleName = null;

        // Xác định role mới dựa vào audience và tier
        if ("HR".equalsIgnoreCase(audience) && "VIP".equalsIgnoreCase(tier)) {
            newRoleName = "HR_VIP";
        } else if ("HR".equalsIgnoreCase(audience) && "BASIC".equalsIgnoreCase(tier)) {
            newRoleName = "HR";
        } else if ("USER".equalsIgnoreCase(audience) && "VIP".equalsIgnoreCase(tier)) {
            newRoleName = "USER_VIP";
        } else if ("USER".equalsIgnoreCase(audience) && "BASIC".equalsIgnoreCase(tier)) {
            newRoleName = "USER";
        }

        if (newRoleName != null) {
            Role newRole = roleRepo.findByName(newRoleName);
            if (newRole != null) {
                user.setRole(newRole);
                userRepo.save(user);
            }
        }
    }

    @Transactional
    public Subscription createPendingSubscription(Long userId, Long planId) {
        if (userId == null)
            throw new RuntimeException("USER_ID_REQUIRED");
        if (planId == null)
            throw new RuntimeException("PLAN_ID_REQUIRED");

        User u = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Plan p = planRepo.findById(planId).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));

        // CHECK: Đã có subscription active chưa
        Optional<Subscription> existingActive = subRepo.findActiveByUserId(userId);
        if (existingActive.isPresent()) {
            throw new RuntimeException("USER_ALREADY_HAS_ACTIVE_SUBSCRIPTION");
        }

        // CHECK: Có PENDING_PAYMENT chưa thanh toán không
//        List<Subscription> pending = subRepo.findByUserIdAndStatus(userId, "PENDING_PAYMENT");
//        if (!pending.isEmpty()) {
//            throw new RuntimeException("USER_HAS_PENDING_PAYMENT");
//        }

        Subscription s = Subscription.builder()
                .user(u)
                .plan(p)
                .status("PENDING_PAYMENT")
                .build();

        return subRepo.save(s);
    }

}
