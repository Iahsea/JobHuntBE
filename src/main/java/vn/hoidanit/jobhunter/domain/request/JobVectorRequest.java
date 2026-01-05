package vn.hoidanit.jobhunter.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobVectorRequest {
    @JsonProperty("job_id")
    private String jobId;

    private String name;
    private String description;
    private String location;
    private String salary;
    private String level;

    @JsonProperty("job_type")
    private String jobType;

    @JsonProperty("years_of_experience")
    private String yearsOfExperience;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("work_mode")
    private String workMode;
}
