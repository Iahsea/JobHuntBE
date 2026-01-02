package vn.hoidanit.jobhunter.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.service.PresenceService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
public class PresenceController {
    private final PresenceService presenceService;

    @GetMapping("/online")
    @ApiMessage("Get all online users")
    public ResponseEntity<Set<Long>> getOnlineUsers() {
        return ResponseEntity.ok(presenceService.getAllOnlineUserIds());
    }

    @GetMapping("/status/{userId}")
    @ApiMessage("Check if user is online")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable Long userId) {
        return ResponseEntity.ok(presenceService.isOnline(userId));
    }

    @GetMapping("/connections/{userId}")
    @ApiMessage("Get connection count for user")
    public ResponseEntity<Integer> getConnectionCount(@PathVariable Long userId) {
        return ResponseEntity.ok(presenceService.getConnectionCount(userId));
    }
}

