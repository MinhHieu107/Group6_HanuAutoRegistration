package com.hanu.registration.service;

import com.hanu.registration.model.LoginResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HanuAuthServiceImpl implements HanuAuthService {

    private static final String LOGIN_API_URL = "https://qldt.hanu.edu.vn/api/auth/login";
    private static final String USER_INFO_URL = "https://qldt.hanu.edu.vn/api/auth/me";

    @Override
    public LoginResult loginToQldt(String studentId, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Bước 1: login thật vào QLĐT
            Map<String, Object> loginData = doLogin(restTemplate, studentId, password);

            String accessToken = getString(loginData, "access_token");
            String refreshToken = getString(loginData, "refresh_token");
            String tokenType = getString(loginData, "token_type");
            String expiresIn = getString(loginData, "expires_in");

            if (accessToken == null || accessToken.isBlank()) {
                return LoginResult.fail("Không lấy được access token từ QLĐT.");
            }

            // Bước 2: lấy thông tin user bằng token
            Map<String, Object> userInfo = getUserInfo(restTemplate, accessToken);
            System.out.println("USER INFO = " + userInfo);

            String roles = extractRoles(userInfo);
            String fullName = extractFullName(userInfo);
            String qldtSession = getDataValue(userInfo, "Session");
            String qldtUserId = getDataValue(userInfo, "IDUser");
            String qldtUsername = getDataValue(userInfo, "userName");
            String principal = getDataValue(userInfo, "principal");
            String userLevel = getDataValue(userInfo, "UserLevel");

            // Bước 3: check role sinh viên
            if (roles == null || !roles.toUpperCase().contains("SINHVIEN")) {
                return LoginResult.fail("Tài khoản đăng nhập thành công nhưng không phải sinh viên.");
            }

            // Bước 4: gom token + session để lưu về sau dùng tiếp API khác
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", accessToken);

            if (refreshToken != null && !refreshToken.isBlank()) {
                tokens.put("refresh_token", refreshToken);
            }
            if (tokenType != null && !tokenType.isBlank()) {
                tokens.put("token_type", tokenType);
            }
            if (expiresIn != null && !expiresIn.isBlank()) {
                tokens.put("expires_in", expiresIn);
            }

            if (qldtSession != null && !qldtSession.isBlank()) {
                tokens.put("session", qldtSession);
            }
            if (qldtUserId != null && !qldtUserId.isBlank()) {
                tokens.put("id_user", qldtUserId);
            }
            if (qldtUsername != null && !qldtUsername.isBlank()) {
                tokens.put("qldt_username", qldtUsername);
            }
            if (principal != null && !principal.isBlank()) {
                tokens.put("principal", principal);
            }
            if (userLevel != null && !userLevel.isBlank()) {
                tokens.put("user_level", userLevel);
            }

            return LoginResult.success(studentId, fullName, roles, tokens);

        } catch (HttpClientErrorException.Unauthorized e) {
            return LoginResult.fail("Sai mã sinh viên hoặc mật khẩu.");
        } catch (HttpClientErrorException.BadRequest e) {
            return LoginResult.fail("Dữ liệu gửi tới QLĐT không hợp lệ: " + e.getResponseBodyAsString());
        } catch (HttpClientErrorException e) {
            return LoginResult.fail("QLĐT từ chối đăng nhập: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return LoginResult.fail("Không thể kết nối tới hệ thống QLĐT: " + e.getMessage());
        }
    }

    private Map<String, Object> doLogin(RestTemplate restTemplate, String studentId, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Origin", "https://qldt.hanu.edu.vn");
        headers.set("Referer", "https://qldt.hanu.edu.vn/");
        headers.set("User-Agent", "Mozilla/5.0");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", studentId);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                LOGIN_API_URL,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Đăng nhập QLĐT thất bại.");
        }

        System.out.println("LOGIN RESPONSE = " + response.getBody());
        return response.getBody();
    }

    private Map<String, Object> getUserInfo(RestTemplate restTemplate, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Referer", "https://qldt.hanu.edu.vn/");
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Không lấy được thông tin người dùng từ QLĐT.");
        }

        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> userInfo) {
        Object dataObj = userInfo.get("data");
        if (dataObj instanceof Map<?, ?>) {
            return (Map<String, Object>) dataObj;
        }
        return Map.of();
    }

    private String extractRoles(Map<String, Object> userInfo) {
        Map<String, Object> data = extractData(userInfo);

        Object rolesObj = data.get("roles");
        if (rolesObj != null) {
            return String.valueOf(rolesObj);
        }

        rolesObj = userInfo.get("roles");
        return rolesObj != null ? String.valueOf(rolesObj) : null;
    }

    private String extractFullName(Map<String, Object> userInfo) {
        Map<String, Object> data = extractData(userInfo);

        String[] possibleKeys = {"FullName", "fullName", "fullname", "name", "hoTen"};
        for (String key : possibleKeys) {
            Object value = data.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }

        for (String key : possibleKeys) {
            Object value = userInfo.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }

        return null;
    }

    private String getDataValue(Map<String, Object> userInfo, String key) {
        Map<String, Object> data = extractData(userInfo);
        Object value = data.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}