package com.hanu.registration.model;

public class UserRuntimeContext {

    private String studentId;
    private String fullName;
    private String accessToken;
    private String qldtSession;
    private RuleConfig ruleConfig;

    public UserRuntimeContext() {
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public RuleConfig getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(RuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
    }
}