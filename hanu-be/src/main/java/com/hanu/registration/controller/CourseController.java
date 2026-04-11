package com.hanu.registration.controller;

import com.hanu.registration.model.Course;
import com.hanu.registration.service.QldtCourseService;
import com.hanu.registration.service.QldtStudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.hanu.registration.model.RegistrationRecord;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CourseController {

    private final QldtCourseService qldtCourseService;
    private final QldtStudentService qldtStudentService;

    public CourseController(QldtCourseService qldtCourseService,
                            QldtStudentService qldtStudentService) {
        this.qldtCourseService = qldtCourseService;
        this.qldtStudentService = qldtStudentService;
    }

    @GetMapping("/courses/search")
    public String searchCourses(
            @RequestParam(required = false) String creditsStr,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/login";
        }

        String accessToken = (String) session.getAttribute("accessToken");
        String qldtSession = (String) session.getAttribute("qldtSession");

        List<Course> courses = new ArrayList<>();
        String studentMajorCode = null;

        if (accessToken != null && !accessToken.isBlank()) {
            //studentMajorCode = "VIP";
            studentMajorCode = "VNS";
            //studentMajorCode = qldtStudentService.getStudentMajorCode(accessToken, qldtSession);
            courses = qldtCourseService.fetchAllCourses(accessToken, qldtSession, studentMajorCode);
        }


        if (creditsStr != null && !creditsStr.isBlank()) {
            try {
                int credits = Integer.parseInt(creditsStr.replaceAll("[^0-9]", ""));
                courses = courses.stream()
                        .filter(c -> c.getCredits() != null && c.getCredits() == credits)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        if ("Open".equalsIgnoreCase(status)) {
            courses = courses.stream()
                    .filter(Course::hasSlots)
                    .collect(Collectors.toList());
        } else if ("Full".equalsIgnoreCase(status)) {
            courses = courses.stream()
                    .filter(c -> !c.hasSlots())
                    .collect(Collectors.toList());
        }


        model.addAttribute("creditsStr", creditsStr);
        model.addAttribute("status", status);
        model.addAttribute("courses", courses);
        model.addAttribute("studentMajorCode", studentMajorCode);
        @SuppressWarnings("unchecked")
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (myRecords == null) {
            myRecords = new ArrayList<>();
            session.setAttribute("myRecords", myRecords);
        }

        List<Long> queuedCourseIds = myRecords.stream()
                .map(r -> r.getCourse() != null ? r.getCourse().getId() : null)
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("myRecords", myRecords);
        model.addAttribute("queuedCourseIds", queuedCourseIds);

        session.setAttribute("currentSearchCourses", courses);
        return "course-filter";
    }

}