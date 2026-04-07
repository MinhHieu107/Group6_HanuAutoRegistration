package com.hanu.registration.model;

import java.util.List;

public class QueueActionResponse {
    private boolean success;
    private String message;
    private boolean inQueue;
    private List<RegistrationRecord> myRecords;

    public QueueActionResponse() {
    }

    public QueueActionResponse(boolean success, String message, boolean inQueue, List<RegistrationRecord> myRecords) {
        this.success = success;
        this.message = message;
        this.inQueue = inQueue;
        this.myRecords = myRecords;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public List<RegistrationRecord> getMyRecords() {
        return myRecords;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public void setMyRecords(List<RegistrationRecord> myRecords) {
        this.myRecords = myRecords;
    }
}