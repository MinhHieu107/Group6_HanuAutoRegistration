package com.hanu.registration.model;

public class AvailabilityApiResponse {

    private boolean available;
    private int remainingSlots;
    private String statusCode;
    private String message;

    public boolean isAvailable() {
        return available;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}