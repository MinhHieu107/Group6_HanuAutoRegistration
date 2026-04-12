package com.hanu.registration.controller;

import com.hanu.registration.model.Course;
import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.QueueActionResponse;
import com.hanu.registration.model.RegistrationRecord;
import com.hanu.registration.model.RegistrationStatus;
import com.hanu.registration.service.FileBasedGlobalQueueStore;
import com.hanu.registration.service.GlobalQueueStore;
import com.hanu.registration.service.QueueRecordStateService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/queue")
public class QueueController {

    private final GlobalQueueStore globalQueueStore;
    private final QueueRecordStateService queueRecordStateService;

    public QueueController(GlobalQueueStore globalQueueStore,
                           QueueRecordStateService queueRecordStateService) {
        this.globalQueueStore = globalQueueStore;
        this.queueRecordStateService = queueRecordStateService;
    }

    @PostMapping("/add-ajax")
    @ResponseBody
    public QueueActionResponse addCourseAjax(@RequestParam("courseId") Long courseId,
                                             HttpSession session) {

        List<Course> availableCourses = getSessionCourses(session);
        List<RegistrationRecord> myRecords = getSessionRecords(session);
        String studentId = (String) session.getAttribute("studentId");

        Course selectedCourse = availableCourses.stream()
                .filter(c -> c.getId() != null && c.getId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (selectedCourse == null) {
            return new QueueActionResponse(false, "Không tìm thấy môn học.", false, myRecords);
        }

        if (!selectedCourse.hasSlots()) {
            return new QueueActionResponse(false, "Lớp này đã đầy, không thể thêm vào hàng đợi.", false, myRecords);
        }

        boolean alreadyExists = myRecords.stream()
                .anyMatch(r -> r.getCourse() != null
                        && r.getCourse().getId() != null
                        && r.getCourse().getId().equals(courseId));

        if (alreadyExists) {
            return new QueueActionResponse(false, "Môn học đã có trong hàng đợi.", true, myRecords);
        }

        int nextPriority = myRecords.stream()
                .map(RegistrationRecord::getPriority)
                .filter(p -> p != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        RegistrationRecord record = new RegistrationRecord();
        record.setId(System.currentTimeMillis());
        record.setStudentId(studentId);
        record.setCourse(selectedCourse);
        record.setPriority(nextPriority);
        record.setStatus(RegistrationStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        myRecords.add(record);
        myRecords = sortRecords(myRecords);

        session.setAttribute("myRecords", myRecords);
        queueRecordStateService.registerStudentRecords(studentId, myRecords);

        globalQueueStore.registerAdd(studentId, record);

        return new QueueActionResponse(true, "Đã thêm môn vào hàng đợi.", true, myRecords);
    }

    @PostMapping("/remove-ajax")
    @ResponseBody
    public QueueActionResponse removeCourseAjax(@RequestParam("courseId") Long courseId,
                                                HttpSession session) {

        List<RegistrationRecord> myRecords = getSessionRecords(session);
        String studentId = (String) session.getAttribute("studentId");

        boolean removed = myRecords.removeIf(r ->
                r.getCourse() != null
                        && r.getCourse().getId() != null
                        && r.getCourse().getId().equals(courseId)
        );

        myRecords = sortRecords(myRecords);
        for (int i = 0; i < myRecords.size(); i++) {
            myRecords.get(i).setPriority(i + 1);
            myRecords.get(i).setUpdatedAt(LocalDateTime.now());
        }

        session.setAttribute("myRecords", myRecords);
        queueRecordStateService.registerStudentRecords(studentId, myRecords);

        if (removed) {
            globalQueueStore.markRemoved(studentId, courseId);
            syncGlobalQueuePriorities(studentId, myRecords);
        }

        if (!removed) {
            return new QueueActionResponse(false, "Không tìm thấy môn trong hàng đợi.", false, myRecords);
        }

        return new QueueActionResponse(true, "Đã xóa môn khỏi hàng đợi.", false, myRecords);
    }

    @PostMapping("/reorder")
    @ResponseBody
    public Map<String, Object> reorderQueue(@RequestBody List<Map<String, Object>> newOrder,
                                            HttpSession session) {

        @SuppressWarnings("unchecked")
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        String studentId = (String) session.getAttribute("studentId");

        if (myRecords == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Queue is empty.");
            return response;
        }

        Map<Long, Integer> priorityMap = new HashMap<>();

        for (Map<String, Object> item : newOrder) {
            Object courseIdObj = item.get("courseId");
            Object priorityObj = item.get("priority");

            if (courseIdObj == null || priorityObj == null) {
                continue;
            }

            Long courseId = ((Number) courseIdObj).longValue();
            Integer priority = ((Number) priorityObj).intValue();

            priorityMap.put(courseId, priority);
        }

        for (RegistrationRecord record : myRecords) {
            if (record.getCourse() != null && record.getCourse().getId() != null) {
                Integer newPriority = priorityMap.get(record.getCourse().getId());
                if (newPriority != null) {
                    record.setPriority(newPriority);
                    record.setUpdatedAt(LocalDateTime.now());
                }
            }
        }

        myRecords = sortRecords(myRecords);
        session.setAttribute("myRecords", myRecords);
        queueRecordStateService.registerStudentRecords(studentId, myRecords);

        syncGlobalQueuePriorities(studentId, myRecords);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Queue reordered successfully.");
        return response;
    }

    @GetMapping("/records")
    @ResponseBody
    public List<RegistrationRecord> getQueueRecords(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        return myRecords != null ? myRecords : new ArrayList<>();
    }

    private void syncGlobalQueuePriorities(String studentId, List<RegistrationRecord> myRecords) {
        List<GlobalQueueEntry> allEntries = globalQueueStore.getAllEntries();

        Map<Long, Integer> priorityMap = new HashMap<>();
        for (RegistrationRecord record : myRecords) {
            if (record.getCourse() != null && record.getCourse().getId() != null) {
                priorityMap.put(record.getCourse().getId(), record.getPriority());
            }
        }

        boolean changed = false;

        for (GlobalQueueEntry entry : allEntries) {
            if (entry != null
                    && entry.isActive()
                    && studentId.equals(entry.getStudentId())
                    && entry.getCourseId() != null) {

                Integer newPriority = priorityMap.get(entry.getCourseId());
                if (newPriority != null) {
                    entry.setLocalPriority(newPriority);
                    changed = true;
                }
            }
        }

        if (changed && globalQueueStore instanceof FileBasedGlobalQueueStore fileStore) {
            fileStore.overwriteAllEntries(allEntries);
        }
    }

    private List<Course> getSessionCourses(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Course> courses = (List<Course>) session.getAttribute("currentSearchCourses");
        return courses != null ? new ArrayList<>(courses) : new ArrayList<>();
    }

    private List<RegistrationRecord> getSessionRecords(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<RegistrationRecord> records = (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (records == null) {
            records = new ArrayList<>();
            session.setAttribute("myRecords", records);
        }

        return records;
    }

    private List<RegistrationRecord> sortRecords(List<RegistrationRecord> records) {
        records.sort(Comparator.comparing(
                RegistrationRecord::getPriority,
                Comparator.nullsLast(Integer::compareTo)
        ));
        return records;
    }
}