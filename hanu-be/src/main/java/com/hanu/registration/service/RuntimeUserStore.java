package com.hanu.registration.service;

import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;

public interface RuntimeUserStore {
    void registerOrUpdateUser(UserRuntimeContext context);

    UserRuntimeContext getByStudentId(String studentId);

    void updateRules(String studentId, RuleConfig ruleConfig);

    void remove(String studentId);
}