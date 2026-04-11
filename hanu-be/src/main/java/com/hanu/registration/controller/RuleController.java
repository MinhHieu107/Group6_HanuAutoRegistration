package com.hanu.registration.controller;

import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.service.RuntimeUserStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rules")
public class RuleController {
    private final RuntimeUserStore runtimeUserStore;

    public RuleController(RuntimeUserStore runtimeUserStore) {
        this.runtimeUserStore = runtimeUserStore;
    }
    @PostMapping("/save")
    public RuleConfig saveRules(@RequestBody RuleConfig rules,
                                HttpSession session) {
        session.setAttribute("rules", rules);
        String studentId = (String) session.getAttribute("studentId");
        if (studentId != null) {
            runtimeUserStore.updateRules(studentId, rules);
        }

        return rules;
    }
    @GetMapping("/get")
    public RuleConfig getRules(HttpSession session) {
        RuleConfig rules = (RuleConfig) session.getAttribute("rules");
        if (rules == null) {
            rules = defaultRules();
            session.setAttribute("rules", rules);
        }
        return rules;
    }
    private RuleConfig defaultRules() {
        RuleConfig r = new RuleConfig();
        r.setEnabled(true);
        r.setRetryInterval(300);
        r.setMaxRetries(3);
        r.setQueuePriority(true);
        r.setStopWhenSuccess(true);
        r.setAvoidConflicts(true);
        r.setOnlyWhenOpen(true);
        r.setMinSlots(1);
        r.setTargetCredits(18);
        r.setPreferredSession("ANY");
        r.setNotifications(true);
        return r;
    }
}