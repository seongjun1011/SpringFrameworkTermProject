package com.kopo.portal.service;

import com.kopo.portal.model.Course;
import com.kopo.portal.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** 개설 강좌 조회, 학사관리자의 강좌 개설·수정 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class CourseService {

    public static final List<String> DAYS = List.of("월", "화", "수", "목", "금");
    public static final int MAX_PERIOD = 6;   // 시간표가 1~6교시로 구성됨

    /** 강좌 개설·수정 화면의 입력값 */
    public record CourseForm(String name, String professor, String dayOfWeek, Integer startPeriod,
                             Integer endPeriod, String location, Integer capacity) {
        String trimmedName() { return name == null ? "" : name.trim(); }
        String trimmedProfessor() { return professor == null ? "" : professor.trim(); }
        String trimmedLocation() { return location == null ? "" : location.trim(); }
    }

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAllByOrderByIdAsc();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    /** @return 성공 시 null, 실패 시 에러 메시지 */
    @Transactional
    public String create(CourseForm f) {
        String error = validate(f, null);
        if (error != null) return error;
        courseRepository.save(new Course(f.trimmedName(), f.trimmedProfessor(), f.dayOfWeek(),
                f.startPeriod(), f.endPeriod(), f.trimmedLocation(), f.capacity()));
        return null;
    }

    /**
     * 강좌 수정. 정원은 현재 신청 인원보다 작게 줄일 수 없고,
     * 이미 신청한 학생이 있으면 시간표가 꼬이지 않도록 요일·교시를 바꿀 수 없다.
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    @Transactional
    public String update(Long id, CourseForm f) {
        // 신청과 동시에 수정되지 않도록 행 잠금으로 읽는다.
        Course c = courseRepository.findByIdForUpdate(id).orElse(null);
        if (c == null) return "존재하지 않는 강좌입니다.";
        String error = validate(f, c);
        if (error != null) return error;
        c.update(f.trimmedName(), f.trimmedProfessor(), f.dayOfWeek(),
                f.startPeriod(), f.endPeriod(), f.trimmedLocation(), f.capacity());   // 변경 감지로 UPDATE
        return null;
    }

    private String validate(CourseForm f, Course current) {
        if (f.trimmedName().isEmpty() || f.trimmedName().length() > 50) return "과목명을 1~50자로 입력하세요.";
        if (f.trimmedProfessor().isEmpty() || f.trimmedProfessor().length() > 30) return "담당교수를 1~30자로 입력하세요.";
        if (f.trimmedLocation().isEmpty() || f.trimmedLocation().length() > 20) return "강의실을 1~20자로 입력하세요.";
        if (!DAYS.contains(f.dayOfWeek())) return "요일을 선택하세요.";
        if (f.startPeriod() == null || f.endPeriod() == null
                || f.startPeriod() < 1 || f.endPeriod() > MAX_PERIOD || f.startPeriod() > f.endPeriod()) {
            return "교시는 1~" + MAX_PERIOD + "교시 사이에서 시작 ≤ 종료가 되도록 선택하세요.";
        }
        if (f.capacity() == null || f.capacity() < 1 || f.capacity() > 300) return "정원은 1~300명으로 입력하세요.";

        if (current != null) {
            if (f.capacity() < current.getEnrolledCount()) {
                return "정원을 현재 신청 인원(" + current.getEnrolledCount() + "명)보다 작게 줄일 수 없습니다.";
            }
            boolean timeChanged = !current.getDayOfWeek().equals(f.dayOfWeek())
                    || current.getStartPeriod() != f.startPeriod() || current.getEndPeriod() != f.endPeriod();
            if (timeChanged && current.getEnrolledCount() > 0) {
                return "이미 신청한 학생이 있어 요일·교시를 바꿀 수 없습니다.";
            }
        }
        // 같은 강의실을 같은 시간에 두 강좌가 쓰지 않도록 검사
        for (Course other : courseRepository.findAll()) {
            if (current != null && other.getId().equals(current.getId())) continue;
            if (other.getLocation().equals(f.trimmedLocation())
                    && other.overlaps(f.dayOfWeek(), f.startPeriod(), f.endPeriod())) {
                return f.trimmedLocation() + "은(는) 같은 시간에 '" + other.getName() + "'("
                        + other.getDayOfWeek() + " " + other.getPeriod() + ") 강의가 있습니다.";
            }
        }
        return null;
    }
}
