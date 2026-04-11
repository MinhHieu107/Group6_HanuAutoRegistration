package com.hanu.registration.service;

import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.MockRegistrationResult;
import com.hanu.registration.model.RegistrationStatus;
import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutoRegistrationService {

    private final GlobalQueueStore globalQueueStore;
    private final RuntimeUserStore runtimeUserStore;
    private final RegistrationGateway registrationGateway;
    private final SelectedCourseStore selectedCourseStore;
    private final QueueRecordStateService queueRecordStateService;

    public AutoRegistrationService(GlobalQueueStore globalQueueStore,
                                   RuntimeUserStore runtimeUserStore,
                                   RegistrationGateway registrationGateway,
                                   SelectedCourseStore selectedCourseStore,
                                   QueueRecordStateService queueRecordStateService) {
        this.globalQueueStore = globalQueueStore;
        this.runtimeUserStore = runtimeUserStore;
        this.registrationGateway = registrationGateway;
        this.selectedCourseStore = selectedCourseStore;
        this.queueRecordStateService = queueRecordStateService;
    }

    public Map<String, Object> executeCycle() {
        List<GlobalQueueEntry> activeEntries = new ArrayList<>(globalQueueStore.getActiveEntries());
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, List<GlobalQueueEntry>> groupedByStudent = activeEntries.stream()
                .collect(Collectors.groupingBy(
                        GlobalQueueEntry::getStudentId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        int processed = 0;
        int waiting = 0;

        for (Map.Entry<String, List<GlobalQueueEntry>> studentEntry : groupedByStudent.entrySet()) {
            String studentId = studentEntry.getKey();
            UserRuntimeContext context = runtimeUserStore.getByStudentId(studentId);

            if (context == null) {
                for (GlobalQueueEntry queueEntry : studentEntry.getValue()) {
                    queueRecordStateService.updateCourseStatus(
                            studentId,
                            queueEntry.getCourseId(),
                            RegistrationStatus.FAILED
                    );
                    removeCourseFromQueue(queueEntry);

                    results.add(buildSimpleResult(
                            queueEntry,
                            false,
                            "NO_RUNTIME_CONTEXT",
                            "User runtime context not found"
                    ));
                }
                continue;
            }

            RuleConfig rules = context.getRuleConfig() != null ? context.getRuleConfig() : defaultRules();

            if (!rules.isEnabled()) {
                for (GlobalQueueEntry queueEntry : studentEntry.getValue()) {
                    results.add(buildSimpleResult(
                            queueEntry,
                            false,
                            "AUTO_DISABLED",
                            "Auto registration is disabled"
                    ));
                }
                continue;
            }

            if (!isAllowedByStartTime(rules)) {
                waiting += studentEntry.getValue().size();
                for (GlobalQueueEntry queueEntry : studentEntry.getValue()) {
                    results.add(buildSimpleResult(
                            queueEntry,
                            false,
                            "WAIT_START_TIME",
                            "Current time has not reached configured start time"
                    ));
                }
                continue;
            }

            List<GlobalQueueEntry> studentQueue = new ArrayList<>(studentEntry.getValue());

            if (rules.isQueuePriority()) {
                studentQueue.sort(Comparator.comparing(
                        GlobalQueueEntry::getLocalPriority,
                        Comparator.nullsLast(Integer::compareTo)
                ));
            } else {
                studentQueue.sort(Comparator.comparing(
                        GlobalQueueEntry::getGlobalSequence,
                        Comparator.nullsLast(Long::compareTo)
                ));
            }

            int currentCredits = selectedCourseStore.getTotalCredits(studentId);

            for (int i = 0; i < studentQueue.size(); i++) {
                GlobalQueueEntry queueEntry = studentQueue.get(i);

                if (rules.getTargetCredits() > 0 && currentCredits >= rules.getTargetCredits()) {
                    removeRemainingForStudent(studentId, studentQueue, i);

                    results.add(buildSimpleResult(
                            queueEntry,
                            true,
                            "TARGET_REACHED",
                            "Target total credits reached, remaining courses removed from queue"
                    ));
                    break;
                }

                if (rules.isAvoidConflicts() && hasConflictWithSelected(studentId, queueEntry)) {
                    queueRecordStateService.updateCourseStatus(
                            studentId,
                            queueEntry.getCourseId(),
                            RegistrationStatus.FAILED
                    );
                    removeCourseFromQueue(queueEntry);

                    results.add(buildSimpleResult(
                            queueEntry,
                            false,
                            "TIME_CONFLICT",
                            "Conflict with already selected course, removed from queue"
                    ));
                    continue;
                }

                int nextCredits = queueEntry.getCredits() != null ? queueEntry.getCredits() : 0;

                if (rules.getTargetCredits() > 0
                        && currentCredits + nextCredits > rules.getTargetCredits()) {
                    queueRecordStateService.updateCourseStatus(
                            studentId,
                            queueEntry.getCourseId(),
                            RegistrationStatus.FAILED
                    );
                    removeCourseFromQueue(queueEntry);

                    results.add(buildSimpleResult(
                            queueEntry,
                            false,
                            "TARGET_EXCEEDED",
                            "Skipping course because adding it would exceed target credits"
                    ));
                    continue;
                }

                Map<String, Object> result = tryRegisterSingleCourse(context, rules, queueEntry);
                results.add(result);
                processed++;

                if (Boolean.TRUE.equals(result.get("success"))) {
                    currentCredits += nextCredits;

                    if (rules.isNotifications()) {
                        selectedCourseStore.addNotification(studentId,
                                "Registered successfully: " + queueEntry.getCourseCode());
                    }

                    if (rules.getTargetCredits() > 0 && currentCredits >= rules.getTargetCredits()) {
                        removeRemainingForStudent(studentId, studentQueue, i + 1);

                        results.add(buildSimpleResult(
                                queueEntry,
                                true,
                                "TARGET_REACHED",
                                "Target total credits reached, remaining courses removed from queue"
                        ));
                        break;
                    }

                    if (rules.isStopWhenSuccess()) {
                        removeRemainingForStudent(studentId, studentQueue, i + 1);

                        results.add(buildSimpleResult(
                                queueEntry,
                                true,
                                "STOP_WHEN_SUCCESS",
                                "Stop when success enabled, remaining courses removed from queue"
                        ));
                        break;
                    }
                } else {
                    if (rules.isNotifications()) {
                        selectedCourseStore.addNotification(studentId,
                                "Registration failed: " + queueEntry.getCourseCode() + " - " + result.get("message"));
                    }
                }
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalEntries", activeEntries.size());
        response.put("remainingEntries", globalQueueStore.getActiveEntries().size());
        response.put("processedEntries", processed);
        response.put("waitingEntries", waiting);
        response.put("results", results);
        return response;
    }

    private Map<String, Object> tryRegisterSingleCourse(UserRuntimeContext context,
                                                        RuleConfig rules,
                                                        GlobalQueueEntry queueEntry) {
        int maxRetries = Math.max(1, rules.getMaxRetries());
        int retryInterval = Math.max(100, rules.getRetryInterval());

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            MockRegistrationResult gatewayResult = registrationGateway.register(context, rules, queueEntry);

            if (gatewayResult.isSuccess()) {
                globalQueueStore.markRemoved(context.getStudentId(), queueEntry.getCourseId());
                selectedCourseStore.addSelectedCourse(context.getStudentId(), queueEntry);

                queueRecordStateService.updateCourseStatus(
                        context.getStudentId(),
                        queueEntry.getCourseId(),
                        RegistrationStatus.SUCCESS
                );

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("studentId", context.getStudentId());
                result.put("courseId", queueEntry.getCourseId());
                result.put("courseCode", queueEntry.getCourseCode());
                result.put("attempt", attempt);
                result.put("success", true);
                result.put("statusCode", gatewayResult.getStatusCode());
                result.put("message", gatewayResult.getMessage());
                return result;
            }

            String statusCode = gatewayResult.getStatusCode();

            if ("TIME_CONFLICT".equals(statusCode)
                    || "WRONG_SESSION".equals(statusCode)
                    || "NOT_ENOUGH_SLOTS".equals(statusCode)
                    || "CLASS_FULL".equals(statusCode)) {

                globalQueueStore.markRemoved(context.getStudentId(), queueEntry.getCourseId());

                queueRecordStateService.updateCourseStatus(
                        context.getStudentId(),
                        queueEntry.getCourseId(),
                        RegistrationStatus.FAILED
                );

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("studentId", context.getStudentId());
                result.put("courseId", queueEntry.getCourseId());
                result.put("courseCode", queueEntry.getCourseCode());
                result.put("attempt", attempt);
                result.put("success", false);
                result.put("statusCode", statusCode);
                result.put("message", gatewayResult.getMessage() + " - removed from queue");
                return result;
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        globalQueueStore.markRemoved(context.getStudentId(), queueEntry.getCourseId());

        queueRecordStateService.updateCourseStatus(
                context.getStudentId(),
                queueEntry.getCourseId(),
                RegistrationStatus.FAILED
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", context.getStudentId());
        result.put("courseId", queueEntry.getCourseId());
        result.put("courseCode", queueEntry.getCourseCode());
        result.put("success", false);
        result.put("statusCode", "FAILED");
        result.put("message", "Failed after max retries - removed from queue");
        return result;
    }

    private void removeRemainingForStudent(String studentId,
                                           List<GlobalQueueEntry> studentQueue,
                                           int startIndex) {
        for (int j = startIndex; j < studentQueue.size(); j++) {
            GlobalQueueEntry remain = studentQueue.get(j);
            globalQueueStore.markRemoved(studentId, remain.getCourseId());
            queueRecordStateService.updateCourseStatus(
                    studentId,
                    remain.getCourseId(),
                    RegistrationStatus.FAILED
            );
        }
    }

    private void removeCourseFromQueue(GlobalQueueEntry queueEntry) {
        globalQueueStore.markRemoved(queueEntry.getStudentId(), queueEntry.getCourseId());
    }

    private boolean isAllowedByStartTime(RuleConfig rules) {
        if (rules.getStartTime() == null || rules.getStartTime().isBlank()) {
            return true;
        }

        try {
            LocalDateTime configured = LocalDateTime.parse(rules.getStartTime());
            return !LocalDateTime.now().isBefore(configured);
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    private boolean hasConflictWithSelected(String studentId, GlobalQueueEntry newEntry) {
        List<GlobalQueueEntry> selected = selectedCourseStore.getSelectedEntries(studentId);
        for (GlobalQueueEntry oldEntry : selected) {
            if (scheduleConflict(oldEntry.getScheduleTime(), newEntry.getScheduleTime())) {
                return true;
            }
        }
        return false;
    }

    private boolean scheduleConflict(String s1, String s2) {
        List<Slot> a = parseSlots(s1);
        List<Slot> b = parseSlots(s2);

        for (Slot x : a) {
            for (Slot y : b) {
                boolean sameDay = x.day == y.day;
                boolean samePeriodOverlap = x.start <= y.end && y.start <= x.end;
                boolean dateOverlap = datesOverlap(x.startDate, x.endDate, y.startDate, y.endDate);

                if (sameDay && samePeriodOverlap && dateOverlap) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean datesOverlap(java.time.LocalDate aStart,
                                 java.time.LocalDate aEnd,
                                 java.time.LocalDate bStart,
                                 java.time.LocalDate bEnd) {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return true;
        }
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    private List<Slot> parseSlots(String schedule) {
        List<Slot> slots = new ArrayList<>();
        if (schedule == null || schedule.isBlank()) {
            return slots;
        }

        String[] parts = schedule.split("<hr>");
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy");

        for (String part : parts) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "Thứ\\s*(\\d).*?tiết\\s*(\\d+)->(\\d+).*?(\\d{2}/\\d{2}/\\d{2}).*?(\\d{2}/\\d{2}/\\d{2})",
                    java.util.regex.Pattern.CASE_INSENSITIVE
            ).matcher(part);

            if (m.find()) {
                try {
                    int day = Integer.parseInt(m.group(1));
                    int start = Integer.parseInt(m.group(2));
                    int end = Integer.parseInt(m.group(3));

                    java.time.LocalDate startDate = java.time.LocalDate.parse(m.group(4), formatter);
                    java.time.LocalDate endDate = java.time.LocalDate.parse(m.group(5), formatter);

                    slots.add(new Slot(day, start, end, startDate, endDate));
                } catch (Exception ignored) {
                }
            }
        }

        return slots;
    }

    private Map<String, Object> buildSimpleResult(GlobalQueueEntry queueEntry,
                                                  boolean success,
                                                  String statusCode,
                                                  String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", queueEntry.getStudentId());
        result.put("courseId", queueEntry.getCourseId());
        result.put("courseCode", queueEntry.getCourseCode());
        result.put("success", success);
        result.put("statusCode", statusCode);
        result.put("message", message);
        return result;
    }

    private RuleConfig defaultRules() {
        RuleConfig r = new RuleConfig();
        r.setEnabled(true);
        r.setRetryInterval(300);
        r.setMaxRetries(3);
        r.setQueuePriority(true);
        r.setStopWhenSuccess(false);
        r.setAvoidConflicts(true);
        r.setOnlyWhenOpen(true);
        r.setMinSlots(1);
        r.setTargetCredits(18);
        r.setPreferredSession("ANY");
        r.setNotifications(true);
        return r;
    }

    private static class Slot {
        int day;
        int start;
        int end;
        java.time.LocalDate startDate;
        java.time.LocalDate endDate;

        Slot(int day, int start, int end, java.time.LocalDate startDate, java.time.LocalDate endDate) {
            this.day = day;
            this.start = start;
            this.end = end;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}