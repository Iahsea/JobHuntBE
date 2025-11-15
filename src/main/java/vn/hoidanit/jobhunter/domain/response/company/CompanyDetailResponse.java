package vn.hoidanit.jobhunter.domain.response.company;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.response.job.JobSummaryResponse;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
public class CompanyDetailResponse {
    private long id;
    private String name;
    private String description;
    private String address;
    private String logo;
    private String coverImage;
    private String website;
    private String companySize;
    private LocalDate foundedDate;
    private Integer employeeCount;
    private String benefits;

    private List<JobSummaryResponse> jobs;
}
