package com.hanu.registration.model;

public class Course {

    private Long id;
    private String courseCode;
    private String groupName;
    private String subGroup;
    private Integer credits;

    public Course() {
    }

    public Course(Long id, String courseCode, String groupName, String subGroup, Integer credits) {
        this.id = id;
        this.courseCode = courseCode;
        this.groupName = groupName;
        this.subGroup = subGroup;
        this.credits = credits;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(String subGroup) {
        this.subGroup = subGroup;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }
}