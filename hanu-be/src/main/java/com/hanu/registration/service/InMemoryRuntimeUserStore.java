package com.hanu.registration.service;

import com.hanu.registration.model.RuleConfig;
import com.hanu.registration.model.UserRuntimeContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRuntimeUserStore implements RuntimeUserStore {

    private final Map<String, UserRuntimeContext> users = new ConcurrentHashMap<>();

    @Override
    public void registerOrUpdateUser(UserRuntimeContext context) {
        if (context == null || context.getStudentId() == null) {
            return;
        }
        users.put(context.getStudentId(), context);
    }

    @Override
    public UserRuntimeContext getByStudentId(String studentId) {
        return users.get(studentId);
    }

    @Override
    public void updateRules(String studentId, RuleConfig ruleConfig) {
        UserRuntimeContext ctx = users.get(studentId);
        if (ctx != null) {
            ctx.setRuleConfig(ruleConfig);
        }
    }

    @Override
    public void remove(String studentId) {
        users.remove(studentId);
    }
}