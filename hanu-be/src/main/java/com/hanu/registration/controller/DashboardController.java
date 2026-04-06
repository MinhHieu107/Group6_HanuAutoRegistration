package com.hanu.registration.controller;

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

        @SuppressWarnings("unchecked")
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (myRecords == null) {
            myRecords = new ArrayList<>();
            session.setAttribute("myRecords", myRecords);
        }

        long pendingCount = myRecords.stream()
                .filter(r -> RegistrationStatus.PENDING.equals(r.getStatus()))
                .count();

        model.addAttribute("myRecords", myRecords);
        model.addAttribute("pendingCount", pendingCount);

        return "dashboard";
    }
}