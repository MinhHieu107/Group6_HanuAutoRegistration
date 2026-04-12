package com.hanu.registration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.registration.integration")
public class RegistrationIntegrationProperties {

    /**
     * mock | real
     */
    private String mode = "mock";

    private String baseUrl = "";
    private String registerPath = "";
    private String availabilityPath = "";
    private String courseDetailPath = "";

    /**
     * Static headers you may want to send on every request.
     * Example:
     * app.registration.integration.default-headers.X-Requested-With=XMLHttpRequest
     */
    private Map<String, String> defaultHeaders = new LinkedHashMap<>();

    /**
     * Optional static query params if the real API needs them.
     */
    private Map<String, String> defaultQueryParams = new LinkedHashMap<>();

    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 8000;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRegisterPath() {
        return registerPath;
    }

    public void setRegisterPath(String registerPath) {
        this.registerPath = registerPath;
    }

    public String getAvailabilityPath() {
        return availabilityPath;
    }

    public void setAvailabilityPath(String availabilityPath) {
        this.availabilityPath = availabilityPath;
    }

    public String getCourseDetailPath() {
        return courseDetailPath;
    }

    public void setCourseDetailPath(String courseDetailPath) {
        this.courseDetailPath = courseDetailPath;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public Map<String, String> getDefaultQueryParams() {
        return defaultQueryParams;
    }

    public void setDefaultQueryParams(Map<String, String> defaultQueryParams) {
        this.defaultQueryParams = defaultQueryParams;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}