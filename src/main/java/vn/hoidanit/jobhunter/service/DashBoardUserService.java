package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.DashBoardUserResponse;
import vn.hoidanit.jobhunter.util.SecurityUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardUserService {

    private final UserService userService;

    public DashBoardUserResponse getDashBoardInfo() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUserDB = this.userService.handleGetUserByUsername(email);

        Long totalAppliedJob = currentUserDB.getResumes().size() > 0 ? (long) currentUserDB.getResumes().size() : 0L;

        Long totalFavoriteJob = currentUserDB.getFavoriteJobIds().size() > 0
                ? (long) currentUserDB.getFavoriteJobIds().size()
                : 0L;

        return DashBoardUserResponse.builder()
                .totalApplied(totalAppliedJob)
                .totalFavorites(totalFavoriteJob)
                .build();
    }

}
