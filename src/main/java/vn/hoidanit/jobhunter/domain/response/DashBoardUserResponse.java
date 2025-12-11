package vn.hoidanit.jobhunter.domain.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashBoardUserResponse {
    private long totalApplied;
    private long totalFavorites;
    private long unreadNotifications;
}
