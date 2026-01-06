package vn.hoidanit.jobhunter.domain.request;

import lombok.Data;

@Data
public class SepayQrRequest {
    private Long userId;
    private Long planId;
}