package com.hanu.registration.service;

import com.hanu.registration.model.LoginResult;

public interface HanuAuthService {
    LoginResult loginToQldt(String studentId, String password);
}