package com.hanu.registration.service;

import com.hanu.registration.config.RegistrationIntegrationProperties;
import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.MockRegistrationResult;
import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DelegatingRegistrationGateway implements RegistrationGateway {

    private final RegistrationIntegrationProperties properties;
    private final RegistrationGateway mockGateway;
    private final RegistrationGateway realGateway;

    public DelegatingRegistrationGateway(RegistrationIntegrationProperties properties,
                                         @Qualifier("mockRegistrationGateway") RegistrationGateway mockGateway,
                                         @Qualifier("realRegistrationGateway") RegistrationGateway realGateway) {
        this.properties = properties;
        this.mockGateway = mockGateway;
        this.realGateway = realGateway;
    }

    @Override
    public MockRegistrationResult register(UserRuntimeContext context,
                                           RuleConfig rules,
                                           GlobalQueueEntry queueEntry) {
        if ("real".equalsIgnoreCase(properties.getMode())) {
            return realGateway.register(context, rules, queueEntry);
        }
        return mockGateway.register(context, rules, queueEntry);
    }
}