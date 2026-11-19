package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * 채용정보 게시글 엔티티.
 */
@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String company;

    @Column(name = "job_role", nullable = false, length = 100)
    private String role;

    @Column(length = 30)
    private String region;

    private LocalDate deadline;
    private LocalDate requestedDate;

    @Column(length = 30)
    private String employmentType;

    @Column(length = 50)
    private String salary;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String requirements;

    protected JobPosting() {
    }

    public JobPosting(String company, String role, String region,
                      LocalDate deadline, LocalDate requestedDate,
                      String employmentType, String salary,
                      String description, String requirements) {
        this.company = company;
        this.role = role;
        this.region = region;
        this.deadline = deadline;
        this.requestedDate = requestedDate;
        this.employmentType = employmentType;
        this.salary = salary;
        this.description = description;
        this.requirements = requirements;
    }

    public Long getId() { return id; }
    public String getCompany() { return company; }
    public String getRole() { return role; }
    public String getRegion() { return region; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDate getRequestedDate() { return requestedDate; }
    public String getEmploymentType() { return employmentType; }
    public String getSalary() { return salary; }
    public String getDescription() { return description; }
    public String getRequirements() { return requirements; }
}
