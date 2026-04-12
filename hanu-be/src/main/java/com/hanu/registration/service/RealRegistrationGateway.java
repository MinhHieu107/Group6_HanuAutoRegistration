package com.hanu.registration.service;

import com.hanu.registration.model.*;
import org.springframework.stereotype.Service;

@Service("realRegistrationGateway")
public class RealRegistrationGateway implements RegistrationGateway {

    private final RegistrationApiClient registrationApiClient;

    public RealRegistrationGateway(RegistrationApiClient registrationApiClient) {
        this.registrationApiClient = registrationApiClient;
    }

    @Override
    public MockRegistrationResult register(UserRuntimeContext context,
                                           RuleConfig rules,
                                           GlobalQueueEntry queueEntry) {

        RegistrationApiRequest request = new RegistrationApiRequest();
        request.setStudentId(context.getStudentId());
        request.setCourseId(queueEntry.getCourseId());
        request.setCourseCode(queueEntry.getCourseCode());
        request.setIdToHoc(queueEntry.getIdToHoc());
        request.setAccessToken(context.getAccessToken());
        request.setQldtSession(context.getQldtSession());

        if (rules.isOnlyWhenOpen()) {
            AvailabilityApiResponse availability = registrationApiClient.checkAvailability(request);

            if (!availability.isAvailable()) {
                return new MockRegistrationResult(
                        false,
                        availability.getMessage() != null ? availability.getMessage() : "No available slots.",
                        "NOT_ENOUGH_SLOTS"
                );
            }

            if (availability.getRemainingSlots() < Math.max(1, rules.getMinSlots())) {
                return new MockRegistrationResult(
                        false,
                        "Remaining slots lower than configured minimum.",
                        "NOT_ENOUGH_SLOTS"
                );
            }
        }

        RegistrationApiResponse apiResponse = registrationApiClient.registerCourse(request);

        return new MockRegistrationResult(
                apiResponse.isSuccess(),
                apiResponse.getMessage(),
                apiResponse.getStatusCode()
        );
    }
}