package com.hanu.registration.controller;

import com.hanu.registration.service.SelectedCourseStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mock-qldt")
public class MockSelectedController {

    private final SelectedCourseStore selectedCourseStore;

    public MockSelectedController(SelectedCourseStore selectedCourseStore) {
        this.selectedCourseStore = selectedCourseStore;
    }

    @GetMapping("/selected")
    public List<String> getSelectedCourses(HttpSession session) {
        String studentId = (String) session.getAttribute("studentId");
        return selectedCourseStore.getSelectedIdToHocList(studentId);
    }
}