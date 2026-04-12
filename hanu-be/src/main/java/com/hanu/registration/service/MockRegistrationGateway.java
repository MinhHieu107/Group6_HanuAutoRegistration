package com.hanu.registration.service;

import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.MockRegistrationResult;
import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;
import org.springframework.stereotype.Service;

@Service("mockRegistrationGateway")
public class MockRegistrationGateway implements RegistrationGateway {

    @Override
    public MockRegistrationResult register(UserRuntimeContext context,
                                           RuleConfig rules,
                                           GlobalQueueEntry queueEntry) {

        if (context == null || queueEntry == null) {
            return new MockRegistrationResult(false, "Invalid registration context", "INVALID");
        }

        if (rules.isOnlyWhenOpen()) {
            int minSlots = Math.max(1, rules.getMinSlots());
            int available = queueEntry.getAvailableSlots() != null ? queueEntry.getAvailableSlots() : 0;
            if (available < minSlots) {
                return new MockRegistrationResult(false,
                        "Not enough available slots",
                        "NOT_ENOUGH_SLOTS");
            }
        }

        if (!matchesPreferredSession(queueEntry, rules)) {
            return new MockRegistrationResult(false,
                    "Course not in preferred session",
                    "WRONG_SESSION");
        }

        String courseCode = queueEntry.getCourseCode() != null
                ? queueEntry.getCourseCode().toUpperCase()
                : "";

        if (courseCode.contains("FULL")) {
            return new MockRegistrationResult(false, "Course is full", "CLASS_FULL");
        }

        if (courseCode.contains("CONFLICT")) {
            return new MockRegistrationResult(false, "Schedule conflict", "TIME_CONFLICT");
        }

        return new MockRegistrationResult(true, "Mock registered successfully", "SUCCESS");
    }

    private boolean matchesPreferredSession(GlobalQueueEntry queueEntry, RuleConfig rules) {
        String preferred = rules.getPreferredSession();
        if (preferred == null || preferred.isBlank() || "ANY".equalsIgnoreCase(preferred)) {
            return true;
        }

        String schedule = queueEntry.getScheduleTime() != null ? queueEntry.getScheduleTime() : "";
        Integer firstPeriod = extractFirstPeriod(schedule);
        if (firstPeriod == null) {
            return true;
        }

        String actualSession;
        if (firstPeriod <= 4) {
            actualSession = "MORNING";
        } else if (firstPeriod <= 8) {
            actualSession = "AFTERNOON";
        } else {
            actualSession = "EVENING";
        }

        return actualSession.equalsIgnoreCase(preferred);
    }

    private Integer extractFirstPeriod(String schedule) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("tiết\\s*(\\d+)->", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(schedule);

        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}