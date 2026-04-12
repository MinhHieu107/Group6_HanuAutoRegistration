package com.hanu.registration.model;

public class RegistrationApiResponse {

    private boolean success;
    private String statusCode;
    private String message;
    private int httpStatus;

    public RegistrationApiResponse() {
    }

    public RegistrationApiResponse(boolean success, String statusCode, String message, int httpStatus) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}