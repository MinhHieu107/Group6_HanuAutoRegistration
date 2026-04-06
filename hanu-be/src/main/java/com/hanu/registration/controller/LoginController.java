package com.hanu.registration.controller;

import com.hanu.registration.model.LoginResult;
import com.hanu.registration.service.HanuAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class LoginController {

    private final HanuAuthService hanuAuthService;

    public LoginController(HanuAuthService hanuAuthService) {
        this.hanuAuthService = hanuAuthService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("studentId") String studentId,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {

        LoginResult result = hanuAuthService.loginToQldt(studentId, password);

        if (result.isSuccess()) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("studentId", result.getStudentId());
            session.setAttribute("fullName", result.getFullName());
            session.setAttribute("roles", result.getRoles());

            session.setAttribute("qldtTokens", result.getTokens());

            session.setAttribute("accessToken", result.getTokens().get("access_token"));
            session.setAttribute("refreshToken", result.getTokens().get("refresh_token"));
            session.setAttribute("tokenType", result.getTokens().get("token_type"));
            session.setAttribute("expiresIn", result.getTokens().get("expires_in"));

            session.setAttribute("qldtSession", result.getTokens().get("session"));
            session.setAttribute("qldtUserId", result.getTokens().get("id_user"));
            session.setAttribute("qldtUsername", result.getTokens().get("qldt_username"));
            session.setAttribute("principal", result.getTokens().get("principal"));
            session.setAttribute("userLevel", result.getTokens().get("user_level"));

            return "redirect:/dashboard";
        }

        model.addAttribute("error", result.getMessage());
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/login";
        }

        // Tạm thời gán dữ liệu rỗng để dashboard không lỗi
        model.addAttribute("myRecords", new ArrayList<>());
        model.addAttribute("pendingCount", 0);

        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}