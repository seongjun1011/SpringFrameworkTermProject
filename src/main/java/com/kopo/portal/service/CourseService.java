package com.kopo.portal.service;

import com.kopo.portal.model.Course;
import com.kopo.portal.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** 개설 강좌 조회 (JPA 사용). */
@Service
@Transactional(readOnly = true)
public class CourseService {

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
}
