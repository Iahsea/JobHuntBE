package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.PlanEntitlement;
import vn.hoidanit.jobhunter.service.PlanEntitlementService;
import vn.hoidanit.jobhunter.domain.request.PlanEntitlementRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plan-entitlements")
public class PlanEntitlementController {

    private final PlanEntitlementService service;

    public PlanEntitlementController(PlanEntitlementService service) {
        this.service = service;
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<PlanEntitlement>> getByPlan(@PathVariable Long planId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getByPlan(planId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanEntitlement> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlanEntitlement> create(@RequestBody PlanEntitlementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanEntitlement> update(@PathVariable Long id, @RequestBody PlanEntitlementRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
