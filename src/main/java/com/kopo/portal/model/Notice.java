package com.kopo.portal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 공지사항 게시글을 표현하는 모델 클래스.
 */
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    private String writer;
    private LocalDateTime createdAt;

    /** 작성한 교직원 → staffs.id 외래키 (초기 데모 공지는 null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    public Notice() {
    }

    public Notice(String title, String content, String writer, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdAt = createdAt;
    }

    /** 교직원이 관리자 화면에서 작성하는 공지 */
    public Notice(String title, String content, Staff staff, LocalDateTime createdAt) {
        this(title, content, staff.getDepartment(), createdAt);
        this.staff = staff;
    }

    public Staff getStaff() {
        return staff;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
