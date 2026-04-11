package com.hanu.registration.controller;

import com.hanu.registration.service.SelectedCourseStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/mock-state")
public class MockStateController {

    private final SelectedCourseStore selectedCourseStore;

    public MockStateController(SelectedCourseStore selectedCourseStore) {
        this.selectedCourseStore = selectedCourseStore;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset(HttpSession session) {
        String studentId = (String) session.getAttribute("studentId");

        if (studentId != null) {
            selectedCourseStore.clearStudentState(studentId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Mock selected state reset");
        return result;
    }
}