package com.hanu.registration.controller;

import com.hanu.registration.model.RegistrationRecord;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
<<<<<<< Updated upstream
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
=======
import org.springframework.web.bind.annotation.*;
>>>>>>> Stashed changes

import java.util.List;

@Controller
<<<<<<< Updated upstream
public class QueueController {

    @PostMapping("/queue/remove")
    public String removeCourse(@RequestParam("courseId") Long courseId,
                               HttpSession session) {
=======
@RequestMapping("/queue")
public class QueueController {

    @PostMapping("/add-ajax")
    @ResponseBody
    public QueueActionResponse addCourseAjax(@RequestParam("courseId") Long courseId,
                                             HttpSession session) {
>>>>>>> Stashed changes

        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (myRecords != null) {
            myRecords.removeIf(record ->
                    record.getCourse() != null &&
                            record.getCourse().getId().equals(courseId)
            );

            session.setAttribute("myRecords", myRecords);
        }

<<<<<<< Updated upstream
        return "redirect:/dashboard";
=======
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
        session.setAttribute("myRecords", myRecords);

        return new QueueActionResponse(true, "Đã thêm môn vào hàng đợi.", true, sortRecords(myRecords));
    }

    @PostMapping("/remove-ajax")
    @ResponseBody
    public QueueActionResponse removeCourseAjax(@RequestParam("courseId") Long courseId,
                                                HttpSession session) {

        List<RegistrationRecord> myRecords = getSessionRecords(session);

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

        if (!removed) {
            return new QueueActionResponse(false, "Không tìm thấy môn trong hàng đợi.", false, myRecords);
        }

        return new QueueActionResponse(true, "Đã xóa môn khỏi hàng đợi.", false, myRecords);
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
>>>>>>> Stashed changes
    }
}