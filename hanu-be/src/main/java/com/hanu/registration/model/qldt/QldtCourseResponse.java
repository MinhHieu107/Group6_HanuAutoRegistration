package com.hanu.registration.model.qldt;

public class QldtCourseResponse {
    private Integer code;
    private QldtCourseData data;

    public QldtCourseResponse() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public QldtCourseData getData() {
        return data;
    }

    public void setData(QldtCourseData data) {
        this.data = data;
    }
}