package vn.hoidanit.jobhunter.domain;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.SecurityUtil;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String experience;

    private String education;

    private String personalWebsite;

    private String nationality;

    private String maritalStatus;

    private String biography;

    private String facebookLink;

    private String linkedinLink;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_profile_skill",
        joinColumns = @JoinColumn(name = "user_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;

    private String twitterLink;

    private String githubLink;

    @OneToOne
    @JoinColumn(name = "user_id") // khóa ngoại
    @JsonIgnore
    private User user;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.updatedAt = Instant.now();
    }
}
