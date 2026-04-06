package com.hanu.registration.controller;

import com.hanu.registration.model.Course;
import com.hanu.registration.model.RegistrationRecord;
import com.hanu.registration.model.RegistrationStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/login";
        }

        String studentId = (String) session.getAttribute("studentId");
        String fullName = (String) session.getAttribute("fullName");
        String roles = (String) session.getAttribute("roles");

        model.addAttribute("studentId", studentId);
        model.addAttribute("fullName", fullName);
        model.addAttribute("roles", roles);

        // 👉 Lấy từ session
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        // 👉 Nếu chưa có thì tạo mock
        if (myRecords == null) {
            myRecords = createMockData();
            session.setAttribute("myRecords", myRecords);
        }

        long pendingCount = myRecords.stream()
                .filter(r -> RegistrationStatus.PENDING.equals(r.getStatus()))
                .count();

        model.addAttribute("myRecords", myRecords);
        model.addAttribute("pendingCount", pendingCount);

        return "dashboard";
    }

    private List<RegistrationRecord> createMockData() {
        List<RegistrationRecord> list = new ArrayList<>();

        Course c1 = new Course(1L, "IT101", "Group 1", "Sub A", 3);
        Course c2 = new Course(2L, "MA202", "Group 2", "Sub B", 3);
        Course c3 = new Course(3L, "EN303", "Group 3", "Sub C", 2);

        list.add(new RegistrationRecord(
                1L,
                "SV001",
                c1,
                1,
                RegistrationStatus.PENDING,
                null,
                null
        ));

        list.add(new RegistrationRecord(
                2L,
                "SV001",
                c2,
                2,
                RegistrationStatus.PENDING,
                null,
                null
        ));

        list.add(new RegistrationRecord(
                3L,
                "SV001",
                c3,
                3,
                RegistrationStatus.SUCCESS,
                null,
                null
        ));

        return list;
    }


}