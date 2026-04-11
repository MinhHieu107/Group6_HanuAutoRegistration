package com.hanu.registration.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AutoRegistrationEngine {

    private final AutoRegistrationService autoRegistrationService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile String status = "IDLE";
    private volatile String lastMessage = "Engine chưa chạy";
    private volatile LocalDateTime lastRunTime;

    public AutoRegistrationEngine(AutoRegistrationService autoRegistrationService) {
        this.autoRegistrationService = autoRegistrationService;
    }

    public synchronized boolean start() {
        if (running.get()) {
            return false;
        }

        running.set(true);
        status = "WAITING";
        lastMessage = "Engine đã khởi động và đang chờ theo rules";
        lastRunTime = LocalDateTime.now();

        Thread worker = new Thread(() -> {
            try {
                while (running.get()) {
                    Map<String, Object> result = autoRegistrationService.executeCycle();

                    lastRunTime = LocalDateTime.now();

                    int remaining = getInt(result.get("remainingEntries"));
                    int processed = getInt(result.get("processedEntries"));
                    int waiting = getInt(result.get("waitingEntries"));

                    if (remaining == 0) {
                        status = "COMPLETED";
                        lastMessage = "Queue đã xử lý xong";
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
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = "STOPPED";
                lastMessage = "Engine bị interrupt";
                running.set(false);
            } catch (Exception e) {
                status = "ERROR";
                lastMessage = "Lỗi engine: " + e.getMessage();
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