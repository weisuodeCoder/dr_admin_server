package com.darhan.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtUtil {
    public static final long EXPIRE_TIME = 5 * 60 * 1000;//token到期时间5分钟，毫秒为单位
    public static final long REFRESH_EXPIRE_TIME = 30 * 60;//RefreshToken到期时间为30分钟，秒为单位
    private static final String TOKEN_SECRET = "ljdyaishijin**3nkjnj??";  //密钥盐

    /**
     * @param : [user]
     * @return : java.lang.String
     * @throws :
     * @Description ：生成token
     * @author : lj
     * @date : 2020-1-31 22:49
     */
    public static String createJwt(String id, Long currentTime) {

        String token = null;
        try {
            Date expireAt = new Date(currentTime + EXPIRE_TIME);
            token = JWT.create().withIssuer("auth0") // 发行人
                    .withClaim("id", id) // 存放数据
                    .withClaim("currentTime", currentTime).withExpiresAt(expireAt) // 过期时间
                    .sign(Algorithm.HMAC256(TOKEN_SECRET));
        } catch (IllegalArgumentException | JWTCreationException je) {
            je.printStackTrace();
        }
        return token;
    }

    /**
     * @param : [token]
     * @return : java.lang.Boolean
     * @throws :
     * @Description ：token验证
     * @author : lj
     * @date : 2020-1-31 22:59
     */
    public static Boolean verify(String token) throws Exception {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).withIssuer("auth0").build();//创建token验证器
        DecodedJWT decodedJWT = jwtVerifier.verify(token);
        return true;
    }

    public static String getId(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("id").asString();
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Long getCurrentTime(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("currentTime").asLong();
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
