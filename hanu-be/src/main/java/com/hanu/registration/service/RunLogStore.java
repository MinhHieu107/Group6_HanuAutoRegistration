package com.hanu.registration.service;

import com.hanu.registration.model.RunCourseResult;
import com.hanu.registration.model.RunLogEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RunLogStore {

    private final ConcurrentHashMap<String, List<RunLogEntry>> logsByStudent = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    public RunLogEntry createRun(String studentId) {
        RunLogEntry entry = new RunLogEntry();
        entry.setRunNumber(counter.incrementAndGet());
        entry.setStudentId(studentId);
        entry.setStartedAt(java.time.LocalDateTime.now());
        entry.setStatus("STARTED");
        entry.setMessage("Run created");

        logsByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(entry);
        return entry;
    }

    public void finishRun(String studentId,
                          Long runNumber,
                          String status,
                          String message,
                          List<RunCourseResult> results) {
        List<RunLogEntry> logs = logsByStudent.get(studentId);
        if (logs == null) return;

        for (RunLogEntry log : logs) {
            if (log.getRunNumber() != null && log.getRunNumber().equals(runNumber)) {
                log.setFinishedAt(java.time.LocalDateTime.now());
                log.setStatus(status);
                log.setMessage(message);
                log.setCourseResults(results != null ? results : new ArrayList<>());

                int success = 0;
                int failed = 0;
                for (RunCourseResult r : log.getCourseResults()) {
                    if (r.isSuccess()) {
                        success++;
                    } else {
                        String code = r.getStatusCode();
                        if (!"WAIT_START_TIME".equals(code)) {
                            failed++;
                        }
                    }
                }
                log.setSuccessCount(success);
                log.setFailedCount(failed);
                return;
            }
        }
    }

    public List<RunLogEntry> getLogs(String studentId) {
        return logsByStudent.getOrDefault(studentId, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(RunLogEntry::getRunNumber).reversed())
                .toList();
    }

    public void clearLogs(String studentId) {
        logsByStudent.remove(studentId);
    }
}