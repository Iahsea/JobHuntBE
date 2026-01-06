package vn.hoidanit.jobhunter.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Plan;
import vn.hoidanit.jobhunter.service.PlanService;
import vn.hoidanit.jobhunter.domain.request.PlanRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<List<Plan>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(planService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(planService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Plan> create(@RequestBody PlanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plan> update(@PathVariable Long id, @RequestBody PlanRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(planService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.planService.delete(id);
        return   ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
