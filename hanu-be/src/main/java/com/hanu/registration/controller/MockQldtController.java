package com.hanu.registration.controller;

import com.hanu.registration.model.Course;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MockQldtController {

    @GetMapping("/mock-qldt")
    public String mockQldtPage(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<Course> currentSearchCourses =
                (List<Course>) session.getAttribute("currentSearchCourses");

        if (currentSearchCourses == null) {
            currentSearchCourses = new ArrayList<>();
        }

        model.addAttribute("courses", currentSearchCourses);
        return "mock-qldt";
    }
}