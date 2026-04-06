package com.hanu.registration.model.qldt;

import java.util.List;

public class QldtCourseData {
    private Integer total_items;
    private Integer total_pages;
    private List<QldtCourseItem> ds_mon_hoc;

    public QldtCourseData() {
    }

    public Integer getTotal_items() {
        return total_items;
    }

    public void setTotal_items(Integer total_items) {
        this.total_items = total_items;
    }

    public Integer getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(Integer total_pages) {
        this.total_pages = total_pages;
    }

    public List<QldtCourseItem> getDs_mon_hoc() {
        return ds_mon_hoc;
    }

    public void setDs_mon_hoc(List<QldtCourseItem> ds_mon_hoc) {
        this.ds_mon_hoc = ds_mon_hoc;
    }
}