package com.hanu.registration.service;

import com.hanu.registration.model.RunCourseResult;
import com.hanu.registration.model.RunLogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoRegistrationEngine {

    private final AutoRegistrationService autoRegistrationService;
    private final RunLogStore runLogStore;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile String status = "IDLE";
    private volatile String lastMessage = "Engine chưa chạy";
    private volatile LocalDateTime lastRunTime;

    public AutoRegistrationEngine(AutoRegistrationService autoRegistrationService,
                                  RunLogStore runLogStore) {
        this.autoRegistrationService = autoRegistrationService;
        this.runLogStore = runLogStore;
    }

    public synchronized boolean start(String studentId) {
        if (running.get()) {
            return false;
        }

        RunLogEntry runLog = runLogStore.createRun(studentId);
        List<RunCourseResult> aggregatedResults = new ArrayList<>();

        running.set(true);
        status = "WAITING";
        lastMessage = "Engine đã khởi động và đang chờ theo rules";
        lastRunTime = LocalDateTime.now();

        Thread worker = new Thread(() -> {
            try {
                while (running.get()) {
                    Map<String, Object> result = autoRegistrationService.executeCycle();

                    lastRunTime = LocalDateTime.now();

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> cycleResults =
                            (List<Map<String, Object>>) result.get("results");

                    if (cycleResults != null) {
                        for (Map<String, Object> item : cycleResults) {
                            RunCourseResult r = new RunCourseResult();
                            r.setCourseCode((String) item.get("courseCode"));
                            Object courseIdObj = item.get("courseId");
                            if (courseIdObj instanceof Number n) {
                                r.setCourseId(n.longValue());
                            }
                            r.setStatusCode((String) item.get("statusCode"));
                            r.setMessage((String) item.get("message"));
                            Object successObj = item.get("success");
                            r.setSuccess(successObj instanceof Boolean b && b);
                            aggregatedResults.add(r);
                        }
                    }

                    int remaining = getInt(result.get("remainingEntries"));
                    int processed = getInt(result.get("processedEntries"));
                    int waiting = getInt(result.get("waitingEntries"));

                    if (remaining == 0) {
                        status = "COMPLETED";
                        lastMessage = "Queue đã xử lý xong";
                        runLogStore.finishRun(
                                studentId,
                                runLog.getRunNumber(),
                                "COMPLETED",
                                lastMessage,
                                aggregatedResults
                        );
                        running.set(false);
                        break;
                    }

                    if (waiting > 0 && processed == 0) {
                        status = "WAITING";
                        lastMessage = "Đang chờ đến start time";
                    } else {
                        status = "RUNNING";
                        lastMessage = "Auto registration đang xử lý queue";
                    }

                    Thread.sleep(500);
                }

                if (!running.get() && "RUNNING".equals(status)) {
                    status = "STOPPED";
                    lastMessage = "Engine đã dừng";
                    runLogStore.finishRun(
                            studentId,
                            runLog.getRunNumber(),
                            "STOPPED",
                            lastMessage,
                            aggregatedResults
                    );
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = "STOPPED";
                lastMessage = "Engine bị interrupt";
                runLogStore.finishRun(
                        studentId,
                        runLog.getRunNumber(),
                        "STOPPED",
                        lastMessage,
                        aggregatedResults
                );
                running.set(false);
            } catch (Exception e) {
                status = "ERROR";
                lastMessage = "Lỗi engine: " + e.getMessage();
                runLogStore.finishRun(
                        studentId,
                        runLog.getRunNumber(),
                        "ERROR",
                        lastMessage,
                        aggregatedResults
                );
                running.set(false);
            }
        });

        worker.setDaemon(true);
        worker.setName("auto-registration-engine");
        worker.start();

        return true;
    }

    public synchronized void stop() {
        running.set(false);
        status = "STOPPED";
        lastMessage = "Engine đã được dừng thủ công";
    }

    public synchronized void reset() {
        running.set(false);
        status = "IDLE";
        lastMessage = "Engine has been reset";
        lastRunTime = null;
    }

    public Map<String, Object> getStatus() {
        return Map.of(
                "running", running.get(),
                "status", status,
                "lastMessage", lastMessage,
                "lastRunTime", lastRunTime != null ? lastRunTime.toString() : ""
        );
    }

    private int getInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }
}