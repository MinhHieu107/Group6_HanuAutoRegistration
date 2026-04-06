package com.hanu.registration.controller;

import com.hanu.registration.model.RegistrationRecord;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class QueueController {

    @PostMapping("/queue/remove")
    public String removeCourse(@RequestParam("courseId") Long courseId,
                               HttpSession session) {

        List<RegistrationRecord> myRecords =
                (List<RegistrationRecord>) session.getAttribute("myRecords");

        if (myRecords != null) {
            myRecords.removeIf(record ->
                    record.getCourse() != null &&
                            record.getCourse().getId().equals(courseId)
            );

            session.setAttribute("myRecords", myRecords);
        }

        return "redirect:/dashboard";
    }
}