package com.hanu.registration.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegistrationApiRequest {

    private String studentId;
    private Long courseId;
    private String courseCode;
    private String idToHoc;
    private String accessToken;
    private String qldtSession;

    /**
     * Flexible bag for real API payload mapping later.
     */
    private Map<String, Object> extraFields = new LinkedHashMap<>();

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

    public String getIdToHoc() {
        return idToHoc;
    }

    public void setIdToHoc(String idToHoc) {
        this.idToHoc = idToHoc;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getQldtSession() {
        return qldtSession;
    }

    public void setQldtSession(String qldtSession) {
        this.qldtSession = qldtSession;
    }

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }
}