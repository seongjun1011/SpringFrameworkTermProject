package com.kopo.portal.service;

import com.kopo.portal.model.Course;
import com.kopo.portal.model.Enrollment;
import com.kopo.portal.repository.CourseRepository;
import com.kopo.portal.repository.EnrollmentRepository;
import com.kopo.portal.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 수강신청(학생-과목 관계) 처리를 담당하는 서비스 (JPA 사용).
 * 정원 초과, 중복 신청 같은 비즈니스 규칙을 검증하고,
 * 신청 내역 저장과 강좌 신청 인원 증감을 하나의 트랜잭션으로 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 수강신청을 시도한다.
     * @return 성공 시 null, 실패 시 사용자에게 보여줄 에러 메시지
     */
    @Transactional
    public String enroll(Long studentId, Long courseId) {
        // 행 잠금으로 강좌를 읽어, 동시에 신청해도 정원을 넘지 않게 한다.
        Course course = courseRepository.findByIdForUpdate(courseId).orElse(null);
        if (course == null) {
            return "존재하지 않는 강좌입니다.";
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return "이미 신청한 강좌입니다.";
        }
        Course conflict = findTimeConflict(studentId, course);
        if (conflict != null) {
            return "이미 신청한 '" + conflict.getName() + "'(" + conflict.getDayOfWeek() + " "
                    + conflict.getPeriod() + ")과 수업 시간이 겹칩니다.";
        }
        if (course.isFull()) {
            return "정원이 초과된 강좌입니다.";
        }
        enrollmentRepository.save(new Enrollment(
                studentRepository.getReferenceById(studentId), course, LocalDateTime.now()));
        course.increaseEnrolled();   // 변경 감지(dirty checking)로 UPDATE 실행
        return null;
    }

    /** 같은 요일에 교시 범위가 겹치는, 이미 신청한 강좌를 찾는다. 없으면 null. */
    private Course findTimeConflict(Long studentId, Course target) {
        for (Course c : findEnrolledCourses(studentId)) {
            boolean sameDay = c.getDayOfWeek().equals(target.getDayOfWeek());
            boolean overlap = c.getStartPeriod() <= target.getEndPeriod()
                    && target.getStartPeriod() <= c.getEndPeriod();
            if (sameDay && overlap) {
                return c;
            }
        }
        return null;
    }

    @Transactional
    public void cancel(Long studentId, Long courseId) {
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).ifPresent(e -> {
            enrollmentRepository.delete(e);
            courseRepository.findByIdForUpdate(courseId).ifPresent(Course::decreaseEnrolled);
        });
    }

    public List<Enrollment> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdOrderByAppliedAtAsc(studentId);
    }

    /** 학사관리자용: 강좌별 수강신청 현황 */
    public List<Enrollment> findByCourse(Long courseId) {
        return enrollmentRepository.findByCourseIdOrderByAppliedAtAsc(courseId);
    }

    /** 로그인한 학생이 신청한 과목 목록(Course)을 반환한다. */
    public List<Course> findEnrolledCourses(Long studentId) {
        return findByStudent(studentId).stream().map(Enrollment::getCourse).toList();
    }

    /** 로그인한 학생이 신청한 강좌 id 목록 (수강신청 화면의 신청/취소 버튼 표시용). */
    public List<Long> findEnrolledCourseIds(Long studentId) {
        return findByStudent(studentId).stream().map(e -> e.getCourse().getId()).toList();
    }
}
