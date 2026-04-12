package com.hanu.registration.service;

import com.hanu.registration.config.RegistrationIntegrationProperties;
import com.hanu.registration.model.AvailabilityApiResponse;
import com.hanu.registration.model.RegistrationApiRequest;
import com.hanu.registration.model.RegistrationApiResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RegistrationApiClient {

    private final RegistrationIntegrationProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public RegistrationApiClient(RegistrationIntegrationProperties properties) {
        this.properties = properties;
    }

    public AvailabilityApiResponse checkAvailability(RegistrationApiRequest request) {
        if (properties.getAvailabilityPath() == null || properties.getAvailabilityPath().isBlank()) {
            AvailabilityApiResponse fallback = new AvailabilityApiResponse();
            fallback.setAvailable(true);
            fallback.setRemainingSlots(Integer.MAX_VALUE);
            fallback.setStatusCode("NO_AVAILABILITY_ENDPOINT");
            fallback.setMessage("Availability endpoint not configured; skipping remote availability check.");
            return fallback;
        }

        try {
            HttpHeaders headers = buildHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = buildUrl(properties.getAvailabilityPath(), request);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return mapAvailabilityResponse(response);

        } catch (RestClientException e) {
            AvailabilityApiResponse fail = new AvailabilityApiResponse();
            fail.setAvailable(false);
            fail.setRemainingSlots(0);
            fail.setStatusCode("REMOTE_AVAILABILITY_ERROR");
            fail.setMessage(e.getMessage());
            return fail;
        }
    }

    public RegistrationApiResponse registerCourse(RegistrationApiRequest request) {
        if (properties.getRegisterPath() == null || properties.getRegisterPath().isBlank()) {
            return new RegistrationApiResponse(
                    false,
                    "REGISTER_ENDPOINT_NOT_CONFIGURED",
                    "Real register endpoint is not configured yet.",
                    500
            );
        }

        try {
            HttpHeaders headers = buildHeaders(request);
            Map<String, Object> payload = buildRegisterPayload(request);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = buildUrl(properties.getRegisterPath(), request);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return mapRegisterResponse(response);

        } catch (RestClientException e) {
            return new RegistrationApiResponse(
                    false,
                    "REMOTE_REGISTER_ERROR",
                    e.getMessage(),
                    500
            );
        }
    }

    private HttpHeaders buildHeaders(RegistrationApiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (Map.Entry<String, String> entry : properties.getDefaultHeaders().entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }

        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            headers.setBearerAuth(request.getAccessToken());
        }

        if (request.getQldtSession() != null && !request.getQldtSession().isBlank()) {
            headers.add(HttpHeaders.COOKIE, "session=" + request.getQldtSession());
        }

        return headers;
    }

    private Map<String, Object> buildRegisterPayload(RegistrationApiRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();

        /*

          need to adjust once you know the real API payload.
         */
        payload.put("studentId", request.getStudentId());
        payload.put("courseId", request.getCourseId());
        payload.put("courseCode", request.getCourseCode());
        payload.put("idToHoc", request.getIdToHoc());

        if (request.getExtraFields() != null) {
            payload.putAll(request.getExtraFields());
        }

        return payload;
    }

    private String buildUrl(String path, RegistrationApiRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(properties.getBaseUrl() != null ? properties.getBaseUrl() : "");
        sb.append(path);

        boolean hasQuery = path.contains("?");

        for (Map.Entry<String, String> entry : properties.getDefaultQueryParams().entrySet()) {
            sb.append(hasQuery ? "&" : "?");
            hasQuery = true;
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return sb.toString();
    }

    private AvailabilityApiResponse mapAvailabilityResponse(ResponseEntity<String> response) {
        AvailabilityApiResponse result = new AvailabilityApiResponse();
        result.setStatusCode("HTTP_" + response.getStatusCode().value());
        result.setMessage(response.getBody());

        /*

         replace this once you know the real response schema.
         */
        if (response.getStatusCode().is2xxSuccessful()) {
            result.setAvailable(true);
            result.setRemainingSlots(Integer.MAX_VALUE);
        } else {
            result.setAvailable(false);
            result.setRemainingSlots(0);
        }

        return result;
    }

    private RegistrationApiResponse mapRegisterResponse(ResponseEntity<String> response) {
        /*

          replace this once  know the real response schema.
         */
        if (response.getStatusCode().is2xxSuccessful()) {
            return new RegistrationApiResponse(
                    true,
                    "SUCCESS",
                    response.getBody() != null ? response.getBody() : "Remote registration success.",
                    response.getStatusCode().value()
            );
        }

        return new RegistrationApiResponse(
                false,
                "REMOTE_REJECTED",
                response.getBody() != null ? response.getBody() : "Remote registration failed.",
                response.getStatusCode().value()
        );
    }
}