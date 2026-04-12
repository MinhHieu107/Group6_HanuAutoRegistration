package com.hanu.registration.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RunLogEntry {
    private Long runNumber;
    private String studentId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status;
    private String message;

    private int successCount;
    private int failedCount;
    private List<RunCourseResult> courseResults = new ArrayList<>();

    public Long getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(Long runNumber) {
        this.runNumber = runNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<RunCourseResult> getCourseResults() {
        return courseResults;
    }

    public void setCourseResults(List<RunCourseResult> courseResults) {
        this.courseResults = courseResults;
    }
}