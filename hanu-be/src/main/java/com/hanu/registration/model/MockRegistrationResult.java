package com.hanu.registration.model;

public class MockRegistrationResult {
    private boolean success;
    private String message;
    private String statusCode;

    public MockRegistrationResult() {
    }

    public MockRegistrationResult(boolean success, String message, String statusCode) {
        this.success = success;
        this.message = message;
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}