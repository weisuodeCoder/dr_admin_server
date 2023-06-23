package com.darhan.entity;

import lombok.Data;

@Data
public class Pager {
    private Integer pageSize;
    private Integer pageNum;

    public void setPageSize(int pageSize) {
        this.pageSize = new Integer(pageSize);
    }
    public void setPageNum(int pageNum) {
        this.pageNum = new Integer(pageNum);
    }

    public void setPageSize(String pageSize) {
        pageSize = pageSize == null ? "10" : pageSize;
        this.pageSize = Integer.valueOf(pageSize);
    }
    public void setPageNum(String pageNum) {
        pageNum = pageNum == null ? "1" : pageNum;
        this.pageNum = Integer.valueOf(pageNum);
    }
}
