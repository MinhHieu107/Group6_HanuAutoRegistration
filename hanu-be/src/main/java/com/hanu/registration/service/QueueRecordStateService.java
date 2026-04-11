package com.hanu.registration.service;

import com.hanu.registration.model.RegistrationRecord;
import com.hanu.registration.model.RegistrationStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueueRecordStateService {

    private final Map<String, List<RegistrationRecord>> recordsByStudent = new ConcurrentHashMap<>();

    public void registerStudentRecords(String studentId, List<RegistrationRecord> records) {
        if (studentId != null && records != null) {
            recordsByStudent.put(studentId, records);
        }
    }

    public List<RegistrationRecord> getRecords(String studentId) {
        return recordsByStudent.get(studentId);
    }

    public void updateCourseStatus(String studentId, Long courseId, RegistrationStatus status) {
        List<RegistrationRecord> records = recordsByStudent.get(studentId);
        if (records == null || courseId == null || status == null) {
            return;
        }

        for (RegistrationRecord record : records) {
            if (record.getCourse() != null
                    && record.getCourse().getId() != null
                    && record.getCourse().getId().equals(courseId)) {
                record.setStatus(status);
            }
        }
    }
}