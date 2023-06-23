package com.darhan.utils;

import com.darhan.entity.Pager;
import com.darhan.entity.SQLEntry;

import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

public class SQLTool {
    // 获取插入数据参数
    public static SQLEntry getInsertParams(String name, Map map) {
        String sql = "insert into " + name + " ";
        int count = 0;
        String str1 = ""; // 此处是insert语句的values括号内的插入参数部分，根据提交的数据生成，所以需保证前端提交数据与数据库字段一致，否则报错
        String str2 = ""; // 有多少个values就需要多少个问号(？),此处生成问号
        String[] strArr = {"id", "createdTime", "updatedTime"};
        Object[] params = new Object[map.size()+strArr.length]; // 需要添加到表单的参数params，strArr.length分别是id，createdTime，updatedTime
        for(Object key : map.keySet()) {
            str1 += key + ",";
            str2 += "?,";
            params[count] = map.get(key);
            count ++;
        }
        for(Object key : strArr) {
            str1 += key + ",";
            str2 += "?,";
        }
        String time = System.currentTimeMillis()/1000+""; // 丢失三位数的精度
        params[count] = IdWorker.nextId(); // 添加id参数
        params[count+1] = time; // 添加createdTime
        params[count+2] = time; // 添加updatedTime
        str1 = str1.substring(0, str1.length()-1);
        str2 = str2.substring(0, str2.length()-1);
        sql = sql + "(" + str1 + ") values (" + str2 + ");";

        // 设置返回参数SQLEntry
        SQLEntry sqlEntry = new SQLEntry();
        sqlEntry.setSql(sql);
        sqlEntry.setParams(params);
        return sqlEntry;
    }

    // 获取更新数据参数
    public static SQLEntry getUpdateParams(String name, Map map, long id) {
        // update XXX set name=?,title=? where id=?
        map.remove("id");
        map.remove("createdTime");
        map.remove("updatedTime");
        String sql = "update " + name + " set ";
        int count = 0;
        String str1 = ""; // 此处是update语句的set入参数部分，根据提交的数据生成，所以需保证前端提交数据与数据库字段一致，否则报错
        Object[] params = new Object[map.size()+2]; // 需要添加到表单的参数params,+2分别是updatedTIme和id
        for(Object key : map.keySet()) {
            str1 += key + "=?,";
            params[count] = map.get(key);
            count ++;
        }
        str1 += "updatedTime=? where id =? ";
        String time = System.currentTimeMillis()/1000+""; // 丢失三位数的精度
        params[count] = time; // 获取时间戳
        params[count+1] = id; // 添加id

        // 设置返回参数SQLEntry
        SQLEntry sqlEntry = new SQLEntry();
        sqlEntry.setSql(sql+str1);
        sqlEntry.setParams(params);
        return sqlEntry;
    }

    // 获取分页数据参数
    public static SQLEntry getPageParams(String sql, ArrayList conditions, Pager pager) {
        Object[] params = new Object[conditions.size()+2];
        int count = 0;
        // 用迭代器给params添加conditions(条件参数)
        Iterator iterator = conditions.iterator();
        while (iterator.hasNext()) {
            params[count] = iterator.next();
            count ++;
        }
        // 给params添加分页参数
        // 转换pager数据类型为Integer，基本数据类型int无法与null进行比对，所以需要使用引用数据类型
        // 同时检查参数是否为空，并设置默认值，防止空指针异常
        int pageNum = pager.getPageNum()==null?1:pager.getPageNum();
        int pageSize = pager.getPageSize()==null?10:pager.getPageSize();
        params[conditions.size()] = (pageNum-1)*pageSize;
        params[conditions.size()+1] = pageSize;

        // 设置返回参数SQLEntry
        SQLEntry sqlEntry = new SQLEntry();
        sqlEntry.setSql(sql + " limit ?,?;");
        sqlEntry.setParams(params);
        return sqlEntry;
    }

    // 获取带条件的total params
    public static SQLEntry getTotalParams(String sql, ArrayList conditions) {
        Object[] params = new Object[conditions.size()];
        int count = 0;
        // 用迭代器给params添加conditions(条件参数)
        Iterator iterator = conditions.iterator();
        while (iterator.hasNext()) {
            params[count] = iterator.next();
            count ++;
        }

        // 设置返回参数SQLEntry
        SQLEntry sqlEntry = new SQLEntry();
        sqlEntry.setSql(sql);
        sqlEntry.setParams(params);
        return sqlEntry;
    }

    // 数组转字符串，主要用于ids转带有"，"号的string
    public static String arr2str(ArrayList ids) {
        // 此处适合使用StringBuffer，减少频繁创建string
        StringBuffer strBuffer = new StringBuffer();
        Iterator iterator = ids.iterator();
        while (iterator.hasNext()) {
            strBuffer.append(iterator.next()+",");
        }
        // 去掉最后一个逗号并转换为字符串
        String str = "";
        if(strBuffer.length()>0) {
            str = strBuffer.substring(0,strBuffer.length()-1);
        }else {
            str = "0";
        }
        return str;
    }
}
