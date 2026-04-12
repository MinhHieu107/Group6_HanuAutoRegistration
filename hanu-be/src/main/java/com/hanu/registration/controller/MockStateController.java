package com.hanu.registration.controller;

import com.hanu.registration.model.RegistrationRecord;
import com.hanu.registration.service.AutoRegistrationEngine;
import com.hanu.registration.service.GlobalQueueStore;
import com.hanu.registration.service.QueueRecordStateService;
import com.hanu.registration.service.RunLogStore;
import com.hanu.registration.service.SelectedCourseStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock-state")
public class MockStateController {

    private final SelectedCourseStore selectedCourseStore;
    private final GlobalQueueStore globalQueueStore;
    private final QueueRecordStateService queueRecordStateService;
    private final AutoRegistrationEngine autoRegistrationEngine;
    private final RunLogStore runLogStore;

    public MockStateController(SelectedCourseStore selectedCourseStore,
                               GlobalQueueStore globalQueueStore,
                               QueueRecordStateService queueRecordStateService,
                               AutoRegistrationEngine autoRegistrationEngine,
                               RunLogStore runLogStore) {
        this.selectedCourseStore = selectedCourseStore;
        this.globalQueueStore = globalQueueStore;
        this.queueRecordStateService = queueRecordStateService;
        this.autoRegistrationEngine = autoRegistrationEngine;
        this.runLogStore = runLogStore;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset(HttpSession session) {
        String studentId = (String) session.getAttribute("studentId");

        if (studentId != null) {
            selectedCourseStore.clearStudentState(studentId);
            globalQueueStore.markAllRemovedForStudent(studentId);
            runLogStore.clearLogs(studentId);

            List<RegistrationRecord> records = queueRecordStateService.getRecords(studentId);
            if (records != null) {
                records.clear();
                queueRecordStateService.registerStudentRecords(studentId, records);
            }

            session.setAttribute("myRecords", records != null ? records : java.util.Collections.emptyList());
        }

        autoRegistrationEngine.reset();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Reset completed: queue, mock state, engine, and run logs cleared.");
        return result;
    }
}