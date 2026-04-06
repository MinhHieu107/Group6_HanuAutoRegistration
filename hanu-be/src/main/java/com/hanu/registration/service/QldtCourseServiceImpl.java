package com.hanu.registration.service;

import com.hanu.registration.model.Course;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QldtCourseServiceImpl implements QldtCourseService {

    private static final String COURSE_API_URL =
            "https://qldt.hanu.edu.vn/api/dkmh/w-locdsnhomto";

    @Override
    @SuppressWarnings("unchecked")
    public List<Course> fetchAllCourses(String accessToken, String qldtSession, String majorCode) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = buildHeaders(accessToken, qldtSession);

            Map<String, Object> body = new HashMap<>();
            body.put("is_CVHT", false);

            Map<String, Object> additional = new HashMap<>();
            Map<String, Object> paging = new HashMap<>();
            paging.put("limit", 99999);
            paging.put("page", 1);

            Map<String, Object> ordering = new HashMap<>();
            ordering.put("name", "");
            ordering.put("order_type", "");

            additional.put("paging", paging);
            additional.put("ordering", List.of(ordering));
            body.put("additional", additional);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    COURSE_API_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> root = response.getBody();
            if (root == null) return new ArrayList<>();

            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null) return new ArrayList<>();

            List<Map<String, Object>> dsNhomTo =
                    (List<Map<String, Object>>) data.get("ds_nhom_to");

            if (dsNhomTo == null) return new ArrayList<>();

            List<Course> courses = new ArrayList<>();
            AtomicLong idGen = new AtomicLong(1);

            for (Map<String, Object> item : dsNhomTo) {
                Course course = mapToCourse(item, idGen.getAndIncrement(), majorCode);
                if (course != null) {
                    courses.add(course);
                }
            }

            Map<String, Course> uniqueMap = new LinkedHashMap<>();
            for (Course c : courses) {
                String key = (c.getCourseCode() == null ? "" : c.getCourseCode())
                        + "_"
                        + (c.getSubGroup() == null ? "" : c.getSubGroup());

                uniqueMap.putIfAbsent(key, c);
            }

            return new ArrayList<>(uniqueMap.values());

        } catch (Exception e) {
            System.out.println("FETCH COURSE ERROR = " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private HttpHeaders buildHeaders(String accessToken, String qldtSession) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Origin", "https://qldt.hanu.edu.vn");
        headers.set("Referer", "https://qldt.hanu.edu.vn/");
        headers.set("User-Agent", "Mozilla/5.0");

        if (qldtSession != null && !qldtSession.isBlank()) {
            headers.set("X-Session", qldtSession);
        }

        return headers;
    }

    private Course mapToCourse(Map<String, Object> item, Long id, String majorCode) {
        String courseCode = getString(item, "ma_mon");
        String courseName = getString(item, "ten_mon");
        String courseNameEn = getString(item, "ten_mon_eg");
        String subGroup = getString(item, "nhom_to");

        if (!matchesMajor(item, courseCode, courseName, courseNameEn, majorCode)) {
            return null;
        }

        Course course = new Course();
        course.setId(id);
        course.setCourseCode(courseCode);
        course.setGroupName(firstNonBlank(courseName, courseNameEn, courseCode));
        course.setCourseNameEn(courseNameEn);
        course.setSubGroup(subGroup);
        course.setCredits((Integer) extractCredits(item));
        course.setScheduleTime(extractSchedule(item));

        // Không dùng giảng viên nữa
        course.setLecturer("");

        course.setEnrolled(extractInt(item, "sl_dk"));
        course.setCapacity(extractInt(item, "sl_cp"));
        course.setDepartment(majorCode != null ? majorCode : "Unknown");

        return course;
    }

    private boolean matchesMajor(Map<String, Object> item,
                                 String courseCode,
                                 String courseName,
                                 String courseNameEn,
                                 String majorCode) {

        if (majorCode == null || majorCode.isBlank()) {
            return true;
        }

        if (courseCode == null) {
            return false;
        }

        String code = courseCode.toUpperCase();
        String major = majorCode.trim().toUpperCase();

        return code.contains(major);
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && target != null
                && source.toUpperCase().contains(target.toUpperCase());
    }

    private Integer extractCredits(Map<String, Object> item) {
        Object value = item.get("so_tc_so");
        if (value == null) {
            value = item.get("so_tc");
        }

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {

            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }

    }

    private String extractSchedule(Map<String, Object> item) {
        String tkb = getString(item, "tkb");
        if (tkb != null && !tkb.isBlank()) {
            return tkb;
        }

        String thu = getString(item, "thu");
        if (thu != null && !"0".equals(thu)) {
            return "Thứ " + thu;
        }

        return "";
    }

    private int extractInt(Map<String, Object> item, String key) {
        Object value = item.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private String getString(Map<String, Object> item, String key) {
        Object value = item.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}