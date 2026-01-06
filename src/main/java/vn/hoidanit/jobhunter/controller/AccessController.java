package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.util.constant.AccessAction;
import vn.hoidanit.jobhunter.service.AccessService;
import vn.hoidanit.jobhunter.domain.response.access.AccessResponse;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/check")
    public ResponseEntity<AccessResponse> check(@RequestParam("action") AccessAction action) {
        return ResponseEntity.status(HttpStatus.OK).body(this.accessService.check(action));
    }
}
