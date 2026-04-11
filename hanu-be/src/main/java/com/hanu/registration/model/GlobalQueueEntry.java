package com.hanu.registration.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GlobalQueueEntry implements Serializable {
    private static final long serialVersionUID = 2L;

    private Long globalSequence;
    private String studentId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer localPriority;
    private Integer credits;
    private Integer availableSlots;
    private String scheduleTime;
    private LocalDateTime createdAt;
    private boolean active;
    private String idToHoc;
    public GlobalQueueEntry() {
    }

    public Long getGlobalSequence() {
        return globalSequence;
    }

    public void setGlobalSequence(Long globalSequence) {
        this.globalSequence = globalSequence;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getLocalPriority() {
        return localPriority;
    }

    public void setLocalPriority(Integer localPriority) {
        this.localPriority = localPriority;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIdToHoc() {
        return idToHoc;
    }

    public void setIdToHoc(String idToHoc) {
        this.idToHoc = idToHoc;
    }
}