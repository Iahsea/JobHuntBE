package vn.hoidanit.jobhunter.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.converter.SocialLinksConverter;
import vn.hoidanit.jobhunter.util.SecurityUtil;

@Table(name = "companies")
@Entity
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "name không được để trống")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private String address;       // trụ sở chính

    private String logo;          // logo nhỏ
    private String coverImage;    // banner công ty

    private String website;       // website chính thức
    private String companySize;   // 1-10, 10-50, 50-200...

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate foundedDate;     // ngày thành lập
    private Integer employeeCount;        // số lượng nhân viên

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Convert(converter = SocialLinksConverter.class)
    @Column(columnDefinition = "JSON")
    private SocialLinks socialLinks;


    // lợi ích khi join

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Job> jobs;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}

