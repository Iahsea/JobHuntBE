package vn.hoidanit.jobhunter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.response.DashBoardUserResponse;
import vn.hoidanit.jobhunter.service.DashBoardUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashBoardUserController {

    private final DashBoardUserService dashBoardUserService;

    @GetMapping("/users/dashboard")
    public ResponseEntity<DashBoardUserResponse> getDashBoardInfo() {
        return ResponseEntity.ok(this.dashBoardUserService.getDashBoardInfo());
    }

}