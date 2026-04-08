package com.hanu.registration.controller;

import com.hanu.registration.model.RegistrationRecord;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TimetableController {

    @GetMapping("/timetable")
    public String timetable(HttpSession session, Model model) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/login";
        }

        @SuppressWarnings("unchecked")
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (myRecords == null) {
            myRecords = new ArrayList<>();
            session.setAttribute("myRecords", myRecords);
        }

        model.addAttribute("myRecords", myRecords);
        return "timetable";
    }
}