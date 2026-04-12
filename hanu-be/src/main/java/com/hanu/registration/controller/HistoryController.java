package com.hanu.registration.controller;

import com.hanu.registration.service.RunLogStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

    private final RunLogStore runLogStore;

    public HistoryController(RunLogStore runLogStore) {
        this.runLogStore = runLogStore;
    }

    @GetMapping("/history")
    public String historyPage(HttpSession session, Model model) {
        String studentId = (String) session.getAttribute("studentId");
        model.addAttribute("runLogs", runLogStore.getLogs(studentId));
        model.addAttribute("studentId", studentId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "history";
    }
}