package com.hanu.registration.controller;

import com.hanu.registration.model.LoginResult;
import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;
import com.hanu.registration.service.HanuAuthService;
import com.hanu.registration.service.RuntimeUserStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final HanuAuthService hanuAuthService;
    private final RuntimeUserStore runtimeUserStore;
    public LoginController(HanuAuthService hanuAuthService,
                           RuntimeUserStore runtimeUserStore) {
        this.hanuAuthService = hanuAuthService;
        this.runtimeUserStore = runtimeUserStore;
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
            RuleConfig rules = (RuleConfig) session.getAttribute("rules");
            if (rules == null) {
                rules = new RuleConfig();
                rules.setEnabled(true);
                rules.setRetryInterval(300);
                rules.setMaxRetries(3);
                rules.setQueuePriority(true);
                rules.setStopWhenSuccess(true);
                rules.setAvoidConflicts(true);
                rules.setOnlyWhenOpen(true);
                rules.setMinSlots(1);
                rules.setTargetCredits(18);
                rules.setPreferredSession("ANY");
                rules.setNotifications(true);
                session.setAttribute("rules", rules);
            }

            UserRuntimeContext context = new UserRuntimeContext();
            context.setStudentId(result.getStudentId());
            context.setFullName(result.getFullName());
            context.setAccessToken((String) result.getTokens().get("access_token"));
            context.setQldtSession((String) result.getTokens().get("session"));
            context.setRuleConfig(rules);

            runtimeUserStore.registerOrUpdateUser(context);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", result.getMessage());
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String studentId = (String) session.getAttribute("studentId");
        if (studentId != null) {
            runtimeUserStore.remove(studentId);
        }
        session.invalidate();
        return "redirect:/login?logout";
    }
}