package com.hanu.registration.model;

import java.util.Map;

public class LoginResult {
    private boolean success;
    private String message;
    private String studentId;
    private String fullName;
    private String roles;
    private Map<String, String> tokens;

    public LoginResult() {
    }

    public LoginResult(boolean success, String message, String studentId,
                       String fullName, String roles, Map<String, String> tokens) {
        this.success = success;
        this.message = message;
        this.studentId = studentId;
        this.fullName = fullName;
        this.roles = roles;
        this.tokens = tokens;
    }

    public static LoginResult success(String studentId, String fullName,
                                      String roles, Map<String, String> tokens) {
        return new LoginResult(true, "Đăng nhập thành công", studentId, fullName, roles, tokens);
    }

    public static LoginResult fail(String message) {
        return new LoginResult(false, message, null, null, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRoles() {
        return roles;
    }

    public Map<String, String> getTokens() {
        return tokens;
    }
}