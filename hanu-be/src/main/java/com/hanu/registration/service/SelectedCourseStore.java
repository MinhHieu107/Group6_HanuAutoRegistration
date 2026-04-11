package com.hanu.registration.service;

import com.hanu.registration.model.GlobalQueueEntry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SelectedCourseStore {

    private final Map<String, List<GlobalQueueEntry>> selectedCourses = new ConcurrentHashMap<>();
    private final Map<String, List<String>> notifications = new ConcurrentHashMap<>();

    public void addSelectedCourse(String studentId, GlobalQueueEntry entry) {
        selectedCourses.computeIfAbsent(studentId, k -> new ArrayList<>()).add(entry);
    }

    public List<String> getSelectedIdToHocList(String studentId) {
        return selectedCourses.getOrDefault(studentId, new ArrayList<>())
                .stream()
                .map(GlobalQueueEntry::getIdToHoc)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<GlobalQueueEntry> getSelectedEntries(String studentId) {
        return selectedCourses.getOrDefault(studentId, new ArrayList<>());
    }

    public int getTotalCredits(String studentId) {
        return selectedCourses.getOrDefault(studentId, new ArrayList<>())
                .stream()
                .map(GlobalQueueEntry::getCredits)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void addNotification(String studentId, String message) {
        notifications.computeIfAbsent(studentId, k -> new ArrayList<>()).add(message);
    }

    public List<String> getNotifications(String studentId) {
        return notifications.getOrDefault(studentId, new ArrayList<>());
    }

    public void clearStudentState(String studentId) {
        selectedCourses.remove(studentId);
        notifications.remove(studentId);
    }

    public void clear(String studentId) {
        clearStudentState(studentId);
    }
}