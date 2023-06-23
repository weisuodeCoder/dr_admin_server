package com.darhan.entity;

import lombok.Data;

@Data
public class SQLEntry {
    private String sql;
    private Object[] params;
}
