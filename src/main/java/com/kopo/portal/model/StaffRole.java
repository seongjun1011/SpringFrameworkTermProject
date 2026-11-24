package com.kopo.portal.model;

/**
 * 교직원 역할.
 * 학사관리자는 학적·수업·게시판 관리 메뉴를, 나머지(결재자)는 결재함을 사용한다.
 * label은 화면 표시용이자 결재단계(ApprovalStep.role)에 저장된 결재자 역할명과 같다.
 */
public enum StaffRole {
    ACADEMIC_ADMIN("학사관리자"),
    STAFF("담당직원"),
    DEPT_HEAD("학과장"),
    SCHOLARSHIP_COMMITTEE("장학위원회"),
    DEAN("학생처장");

    private final String label;

    StaffRole(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    public boolean isAcademicAdmin() { return this == ACADEMIC_ADMIN; }

    /** 휴학·장학금 결재를 처리하는 역할인지 */
    public boolean isApprover() { return this != ACADEMIC_ADMIN; }
}
