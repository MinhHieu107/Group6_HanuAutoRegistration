package com.hanu.registration.service;

import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.RegistrationRecord;

import java.util.List;

public interface GlobalQueueStore {
    GlobalQueueEntry registerAdd(String studentId, RegistrationRecord record);

    void markRemoved(String studentId, Long courseId);

    void markAllRemovedForStudent(String studentId);

    List<GlobalQueueEntry> getAllEntries();

    List<GlobalQueueEntry> getActiveEntries();
}