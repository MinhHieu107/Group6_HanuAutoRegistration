package com.hanu.registration.service;

import com.hanu.registration.model.Course;

import java.util.List;

public interface QldtCourseService {
    List<Course> fetchAllCourses(String accessToken, String qldtSession, String majorCode);
}