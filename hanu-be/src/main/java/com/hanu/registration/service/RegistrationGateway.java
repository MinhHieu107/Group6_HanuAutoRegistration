package com.hanu.registration.service;

import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.model.MockRegistrationResult;
import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;

public interface RegistrationGateway {
    MockRegistrationResult register(UserRuntimeContext context,
                                    RuleConfig rules,
                                    GlobalQueueEntry queueEntry);
}