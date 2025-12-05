package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.UserProfile;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.service.UserProfileService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;

    private final UserProfileService userProfileService;

    @PutMapping("user-profiles/{id}")
    @ApiMessage("Update user profile by id")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable("id") Long id,
            @Valid @RequestBody UserProfile userProfile)
            throws IdInvalidException {

        log.info("UserInfo Update: {}", userProfile);
        log.info("UserInfo Update: {} {} {} {} {}", userProfile, userProfile.getFacebookLink(),
                userProfile.getLinkedinLink(),
                userProfile.getGithubLink(), userProfile.getTwitterLink());

        User currentUser = this.userService.fetchUserById(id);

        if (currentUser == null) {
            throw new IdInvalidException(
                    "User with id " + id + " không tồn tại.");
        }

        UserProfile updatedProfile = this.userProfileService.updateUserProfile(id, userProfile);

        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/user-profiles/{id}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable("id") Long id) {
        UserProfile userProfile = this.userProfileService.getUserProfileById(id);
        if (userProfile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userProfile);
    }

}
