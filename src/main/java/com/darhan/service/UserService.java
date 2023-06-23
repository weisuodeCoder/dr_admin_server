package com.darhan.service;

import com.darhan.entity.Result;
import com.darhan.entity.ResultCode;
import com.darhan.entity.SQLEntry;
import com.darhan.utils.EncryptUtil;
import com.darhan.utils.JwtUtil;
import com.darhan.utils.RedisUtil;
import com.darhan.utils.SQLTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class UserService {
    private final EncryptUtil encryptUtil;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    JwtUtil jwtUtil;
    @Value("${params.slat}")
    String slat;
    @Autowired
    RedisUtil redisUtil;


    public UserService() {
        encryptUtil = EncryptUtil.getInstance(); // 加密工具
    }

    // 创建用户
    public Result create(Map params) {
        // 校验账号
        String account = (String) params.get("account");
        if (account == null || account.equals("")) return new Result("账号不能为空！");
        String name = (String) params.get("name");
        if (name == null || name.equals("")) return new Result("昵称不能为空！");

        List rows = jdbcTemplate.queryForList("select account,name from sys_user where account=? or name=? ;", account, name);
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = (Map) rows.get(i);
            if (account.equals(row.get("account"))) {
                return new Result("账号已存在！");
            } else if (name.equals(row.get("name"))) {
                return new Result("昵称已存在！");
            }
        }
        if (rows.size() > 0) return new Result("账号或用户名已存在！");

        // 密码加密
        String password = (String) params.get("password");
        password = encryptUtil.SHA1(password, slat); // 不可逆加密
        params.replace("password", password);
        SQLEntry sqlEntry = SQLTool.getInsertParams("sys_user", params);
        int count = jdbcTemplate.update(sqlEntry.getSql(), sqlEntry.getParams());
        return new Result(count);
    }

    // 登陆
    public Result login(Map params, HttpServletResponse response) {
        // 校验账号
        Object[] userName = {params.get("account")};
        List rows = jdbcTemplate.queryForList("select id,account,name,level,isUsing,orgId,password from sys_user where account=? ;", userName);
        String inputPass = (String) params.get("password");

        // 用户鉴权
        ResultCode resultCode = identify(rows, inputPass);
        if (resultCode.isSuccess()) { // 鉴权通过构造token
            Map<String, Object> userMap = (Map) rows.get(0);
            userMap.remove("password");
            userMap.remove("isUsing");

            String id =  String.valueOf(userMap.get("id"));
            long currentTime = System.currentTimeMillis();
            String token = jwtUtil.createJwt(id, currentTime);

            // 获取api路径
            String sql1 = "SELECT a.roleId AS rid FROM `user_org_and_role` a " +
                    "LEFT JOIN `user_and_org` b ON b.id=a.tableId " +
                    "WHERE b.userId = ? ;";
            List apis = new ArrayList();

            int level = Integer.parseInt(String.valueOf(userMap.get("level")));
            if(level==1) {
                apis = jdbcTemplate.queryForList("SELECT api FROM sys_resource WHERE api IS NOT NULL;");
            }else if (level==2) {
                apis = jdbcTemplate.queryForList("SELECT api FROM sys_resource WHERE api IS NOT NULL AND LEVEL>2;");
            }else if(level==3) {
                List<Map<String,Object>> roleIds = jdbcTemplate.queryForList(sql1,id);
                Iterator<Map<String,Object>> iterator1=roleIds.iterator();
                List<String> rids = new ArrayList<>();
                while (iterator1.hasNext()) {
                    Map rmap = iterator1.next();
                    rids.add(String.valueOf(rmap.get("rid")));
                }
                Iterator<String> iterator2=rids.iterator();
                String condition = "";
                while (iterator2.hasNext()) {
                    condition += iterator2.next() + ",";
                }
                if(condition.length()==0) {
                    return new Result(ResultCode.UNDISTRIBUTED_ERROR);
                }
                condition = condition.substring(0, condition.length()-1);
                String sql2 = "SELECT api FROM sys_resource WHERE id IN (SELECT resourceId FROM `role_and_resource` " +
                        "WHERE roleId IN (" +condition +"))AND api IS NOT NULL AND LEVEL = 3;";
                apis = jdbcTemplate.queryForList(sql2);
            }
            Set<String> apiParams = new HashSet<>();
            Iterator iterator = apis.iterator();
            while (iterator.hasNext()) {
                Map<String,String> next = (Map<String,String>) iterator.next();
                apiParams.add(next.get("api"));
            }
            HashMap map = new HashMap<String,Object>();
            map.put("currentTime", currentTime);
            map.put("user",userMap);
            map.put("apis",apiParams);
            redisUtil.hmset(String.valueOf(id), map, JwtUtil.REFRESH_EXPIRE_TIME);
            response.setHeader("Authorization", token);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            return new Result(ResultCode.SUCCESS);
        }
        return new Result(resultCode);
    }

    /**
     * 用户鉴权
     */
    private ResultCode identify(List rows, String inputPass) {
        // 如果该用户不存在
        if (rows.size() == 0) {
            return ResultCode.USER_NOT_FOUND;
        }
        Map<String, Object> userMap = (Map) rows.get(0);

        // 如果账号被冻结，禁止登录
        if ((int) userMap.get("isUsing") != 1) {
            return ResultCode.USER_FREEZE;
        }

        // 比对密码
        String resultPass = (String) userMap.get("password");
        inputPass = encryptUtil.SHA1(inputPass, slat);
        if (inputPass.equals(resultPass)) {
            return ResultCode.SUCCESS;
        }
        return ResultCode.USER_PASSWOED_ERROR;
    }
}
