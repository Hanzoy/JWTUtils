package com.hanzoy.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    private String sing;
    private int time;

    public JWTUtils() {
    }

    public JWTUtils(int time) {
        this.time = time;
    }

    public JWTUtils(String sing) {
        this.sing = sing;
    }

    public JWTUtils(String sing, int time) {
        this.sing = sing;
        this.time = time;
    }

    public String getSing() {
        return sing;
    }

    public void setSing(String sing) {
        this.sing = sing;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    /**
     * 获取传入对象上所有命名为value的成员字段并生成token
     *
     * @param t     传入对象
     * @param value 字段命名
     * @param SING  签名
     * @param time  有效期
     * @return token
     */
    public static <T> String createToken(T t, String value, String SING, int time) {
        JWTCreator.Builder builder = JWT.create();
        ObjectMapper objectMapper = new ObjectMapper();
        Class<?> aClass = t.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Tokens.class) || field.isAnnotationPresent(Token.class)) {
                field.setAccessible(true);
                boolean flag = false;
                for (Token token : field.getAnnotationsByType(Token.class)) {
                    if (value.equals(token.value())) {
                        flag = true;
                        break;
                    }
                }
                if (flag) try {
                    builder.withClaim(field.getName(), objectMapper.writeValueAsString(field.get(t)));
                } catch (IllegalAccessException | JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, time);
        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(SING));
    }

    public static <T> String createToken(T t, String SING, int time) {
        return createToken(t, "", SING, time);
    }

    public <T> String createToken(T t, String value) {
        return createToken(t, value, sing, time);
    }

    public <T> String createToken(T t) {
        return createToken(t, "", sing, time);
    }

    public static String createTokenFromMap(Map<String, ?> map, String SING, int time) {
        JWTCreator.Builder builder = JWT.create();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            try {
                builder.withClaim(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, time);
        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(SING));
    }

    public String createToken(Map<String, Object> map) {
        return createToken(map, sing, time);
    }

    /**
     * 验证token
     *
     * @param token token
     * @param SING  签名
     * @return 验证是否通过
     */
    public static boolean checkToken(String token, String SING) {
        try {
            JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean checkToken(String token) {
        return checkToken(token, sing);
    }

    /**
     * 传入token返回其解析类
     *
     * @param token  token
     * @param SING   签名
     * @param tClass 解析类
     * @return 返回对应解析类实体类
     */
    public static <T> T getBean(String token, String SING, Class<T> tClass) {
        Map<String, Claim> claims;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            claims = JWT.require(Algorithm.HMAC256(SING)).build().verify(token).getClaims();
            T t = tClass.newInstance();
            Class<?> aClass = t.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Token.class) || field.isAnnotationPresent(Tokens.class)) {
                    field.setAccessible(true);
                    try {
                        if (claims.containsKey(field.getName())) {
                            field.set(t, objectMapper.readValue(claims.get(field.getName()).asString(), field.getType()));
                        }
                    } catch (IllegalAccessException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T getBean(String token, Class<T> tClass) {
        return getBean(token, sing, tClass);
    }

    public static <T> Map<String, T> getBeanAsMap(String token, String SING, Class<T> tClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Claim> claims = JWT.require(Algorithm.HMAC256(SING)).build().verify(token).getClaims();
        Map<String, T> map = new HashMap<>();
        for (Map.Entry<String, Claim> entry : claims.entrySet()) {
            if (entry.getKey().equals("exp")) continue;
            try {
                map.put(entry.getKey(), objectMapper.readValue(entry.getValue().asString(), tClass));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public <T> Map<String, T> getBeanAsMap(String token, Class<T> tClass) {
        return getBeanAsMap(token, sing, tClass);
    }
}
