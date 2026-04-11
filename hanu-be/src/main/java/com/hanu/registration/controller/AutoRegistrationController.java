package com.hanu.registration.controller;

import com.hanu.registration.service.AutoRegistrationEngine;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auto-registration")
public class AutoRegistrationController {

    private final AutoRegistrationEngine autoRegistrationEngine;

    public AutoRegistrationController(AutoRegistrationEngine autoRegistrationEngine) {
        this.autoRegistrationEngine = autoRegistrationEngine;
    }

    @PostMapping("/start")
    public Map<String, Object> start() {
        boolean started = autoRegistrationEngine.start();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", started);
        response.put("message", started ? "Engine started" : "Engine is already running");
        response.put("engineStatus", autoRegistrationEngine.getStatus());
        return response;
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        autoRegistrationEngine.stop();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Engine stopped");
        response.put("engineStatus", autoRegistrationEngine.getStatus());
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return autoRegistrationEngine.getStatus();
    }
}