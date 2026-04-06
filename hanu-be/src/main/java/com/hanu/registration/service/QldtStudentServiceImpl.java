package com.hanu.registration.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QldtStudentServiceImpl implements QldtStudentService {

    private static final String STUDENT_PROGRAM_API =
            "https://qldt.hanu.edu.vn/api/sch/w-locdsctdtsinhvien";

    @Override
    @SuppressWarnings("unchecked")
    public String getStudentMajorCode(String accessToken, String qldtSession) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = buildHeaders(accessToken, qldtSession);

            Map<String, Object> body = buildBody();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    STUDENT_PROGRAM_API,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> root = response.getBody();
            System.out.println("STUDENT PROGRAM RESPONSE = " + root);

            if (root == null) {
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null) {
                return null;
            }

            List<Map<String, Object>> dsNganhSinhVien =
                    (List<Map<String, Object>>) data.get("ds_nganh_sinh_vien");

            if (dsNganhSinhVien == null || dsNganhSinhVien.isEmpty()) {
                return null;
            }

            Object maNganh = dsNganhSinhVien.get(0).get("ma_nganh");
            return maNganh != null ? String.valueOf(maNganh) : null;

        } catch (Exception e) {
            System.out.println("GET STUDENT MAJOR ERROR = " + e.getMessage());
            return null;
        }
    }

    private HttpHeaders buildHeaders(String accessToken, String qldtSession) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Origin", "https://qldt.hanu.edu.vn");
        headers.set("Referer", "https://qldt.hanu.edu.vn/");
        headers.set("User-Agent", "Mozilla/5.0");

        if (qldtSession != null && !qldtSession.isBlank()) {
            headers.set("X-Session", qldtSession);
        }

        return headers;
    }

    private Map<String, Object> buildBody() {
        Map<String, Object> body = new HashMap<>();

        Map<String, Object> filter = new HashMap<>();
        filter.put("loai_chuong_trinh_dao_tao", 1);

        Map<String, Object> additional = new HashMap<>();

        Map<String, Object> paging = new HashMap<>();
        paging.put("limit", 500);
        paging.put("page", 1);

        Map<String, Object> orderingItem = new HashMap<>();
        orderingItem.put("name", null);
        orderingItem.put("order_type", null);

        additional.put("paging", paging);
        additional.put("ordering", List.of(orderingItem));

        body.put("filter", filter);
        body.put("additional", additional);

        return body;
    }
}