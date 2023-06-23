package com.darhan.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页
 * {
 *     “success”: true,
 *     "code": 10000,
 *     "message": "操作成功",
 *     "data": {
 *         "total": "总条数",
 *         "list": []
 *     }
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> list;
}
