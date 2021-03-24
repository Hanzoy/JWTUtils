package com.hanzoy.utils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Calendar;

@Component
@ConfigurationProperties(prefix = "token")
public class JWTUtils {
    private String sign;
    private String time;
    private int _time;
    public JWTUtils() {
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = String.valueOf(Math.round(new Operation().operationExpression(time)));
        _time = new Integer(this.time);
    }

    /**
     * 根据传入对象上group分组的成员字段生成token
     * @param t 传入对象
     * @param group token分组
     * @return token
     */
    public <T> String createToken(T t, String group){
        JWTCreator.Builder builder = JWT.create();
        ObjectMapper objectMapper = new ObjectMapper();
        Class<?> aClass = t.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Tokens.class) || field.isAnnotationPresent(Token.class)) {
                field.setAccessible(true);
                boolean flag = false;
                for (Token token : field.getAnnotationsByType(Token.class)) {
                    if (group.equals(token.value())) {
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
        instance.add(Calendar.SECOND, this._time);
        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(this.sign));
    }

    /**
     * 根据传入对象上所有默认分组的成员字段生成token
     * @param t 传入对象
     * @return token
     */
    public <T> String createToken(T t){
        if(t instanceof Map){
            return createTokenFromMap((Map<String, ?>) t);
        }
        return createToken(t, "");
    }

    /**
     * 根据传入的指定字段名称生成token
     * @param t 传入对象
     * @param fields 指定的需要生成token的字段名
     * @return token
     */
    public <T> String createTokenCustomFields(T t, String... fields){
        JWTCreator.Builder builder = JWT.create();
        ObjectMapper objectMapper = new ObjectMapper();
        Class<?> aClass = t.getClass();
        Field[] _fields = aClass.getDeclaredFields();
        for (Field field : _fields) {
            field.setAccessible(true);
            String name = field.getName();
            boolean flag = false;
            for (String _name : fields) {
                if(_name.equals(name)){
                    flag = true;
                    break;
                }
            }
            if(flag)try {
                builder.withClaim(name, objectMapper.writeValueAsString(field.get(t)));
            } catch (IllegalAccessException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, this._time);
        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(this.sign));
    }

    /**
     * 根据传入的Map生成token
     * @param map 传入的Map<String, ?>
     * @return token
     */
    public String createTokenFromMap(Map<String, ?> map) {
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
        instance.add(Calendar.SECOND, this._time);
        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(this.sign));
    }

    /**
     * 验证token
     * @param token token
     * @return 验证是否通过
     */
    public boolean checkToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(this.sign)).build().verify(token);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 传入token返回其解析类
     * @param token  token
     * @param tClass 解析类
     * @return 返回对应解析类实体类
     */
    public <T> T getBean(String token, Class<T> tClass) {
        Map<String, Claim> claims;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            claims = JWT.require(Algorithm.HMAC256(this.sign)).build().verify(token).getClaims();
            T t = tClass.newInstance();
            Class<?> aClass = t.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    if (claims.containsKey(field.getName())) {
                        field.set(t, objectMapper.readValue(claims.get(field.getName()).asString(), field.getType()));
                    }
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将传入的token解析为Map
     * @param token 需要解析的token
     * @param tClass Map中Value的类型
     * @return 解析后的map
     */
    public <T> Map<String, T> getBeanAsMap(String token, Class<T> tClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Claim> claims = JWT.require(Algorithm.HMAC256(this.sign)).build().verify(token).getClaims();
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

    /**
     * 将传入的token解析为Map
     * @param token 需要解析的token
     * @return 解析后的map
     */
    public Map<String, Object> getBeanAsMap(String token){
        return getBeanAsMap(token, Object.class);
    }

    public Object getValueFromToken(String token, String key){
        ObjectMapper objectMapper = new ObjectMapper();
        Claim claim = JWT.require(Algorithm.HMAC256(this.sign)).build().verify(token).getClaim(key);
        Object res = null;
        try {
            if(claim.asString() == null)
                return null;
            res = objectMapper.readValue(claim.asString(),Object.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return res;
    }
}


/**
 * 对表达式求值，支持加减乘除括号小数点，不支持负数
 */
class Operation {
    //优先级Map
    private static final Map<String, Integer> OP_PRIORITY_MAP = new HashMap<String, Integer>() {
        {
            put("(", 0);
            put("+", 3);
            put("-", 3);
            put("*", 4);
            put("/", 4);
            put(")", 10);
        }
    };

    public double operationExpression(String expression) {
        Stack<String> opStack = new Stack<String>();         //运算符栈
        Stack<BigDecimal> numStack = new Stack<BigDecimal>();       //操作数栈
        StringBuilder numBuilder = new StringBuilder();     //当前数值的追加器

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c >= '0' && c <= '9' || c == '.') {          //如果是数值则加入追加器
                numBuilder.append(c);
            } else {                                          //如果是运算符
                if (numBuilder.length() > 0) {               //如果numBuilder有值说明里面已经有一个数值
                    numStack.push(new BigDecimal(numBuilder.toString()));     //把数值入运算符栈
                    numBuilder.delete(0, numBuilder.length());  //清空数值
                }
                //读取到的字符是运算符
                String op = String.valueOf(c);
                if (opStack.empty()) {    //如果操作数栈没有运算符
                    opStack.push(op);
                } else {
                    //如果是"("则直接入运算栈
                    if ("(".equals(op)) {
                        opStack.push(op);
                    } else if (")".equals(op)) {
                        //如果是")"则进行括号匹配运算括号内的表达式
                        while (!"(".equals(opStack.peek())) {
                            stackOperation(opStack, numStack);
                        }
                        opStack.pop();
                    } else {
                        //如果是运算符，需要对比当前运算符op和栈顶的运算符优先级。
                        do {
                            //比较当前运算符和栈顶运算符的优先级,如果nowOp和opStack栈顶元素相同或者低级，
                            // 则进行运算，直到nowOp高于opStack栈顶
                            if (jubgmentPriority(op, opStack.peek())) {
                                stackOperation(opStack, numStack);
                                if (opStack.empty()) {
                                    opStack.push(op);
                                    break;
                                }
                            } else {
                                opStack.push(op);
                                break;
                            }
                        } while (!opStack.empty());
                    }
                }
            }
        }

        //表达式结束，追加器里面有值
        if (numBuilder.length() > 0) {
            numStack.push(new BigDecimal(numBuilder.toString()));
        }

        while (!opStack.empty()) {
            stackOperation(opStack, numStack);
        }
        return numStack.pop().doubleValue();
    }

    /**
     * 进行一次二元运算
     *
     * @param opStack
     * @param numStack
     */
    public void stackOperation(Stack<String> opStack, Stack<BigDecimal> numStack) {
        String opT = opStack.pop();              //栈顶运算符
        BigDecimal num2 = numStack.pop();       //第二个操作数
        BigDecimal num1 = numStack.pop();       //第一个操作数
        BigDecimal operationNum = oneOperation(opT, num1, num2);   //num1 op num2

        numStack.push(operationNum);            //把计算完的结果放入操作数栈
    }


//

    /**
     * 单次计算，计算为num1 op num2
     *
     * @param op   运算符
     * @param num1 第一个操作数
     * @param num2 第二个操作数
     * @return num1 op num2
     */
    public BigDecimal oneOperation(String op, BigDecimal num1, BigDecimal num2) {
        BigDecimal result = new BigDecimal(0);
        switch (op) {
            case "+":
                result = num1.add(num2);
                break;
            case "-":
                result = num1.subtract(num2);
                break;
            case "*":
                result = num1.multiply(num2);
                break;
            case "/":
                result = num1.divide(num2);
                break;
            default:
                break;
        }
        return result;
    }


    /**
     * 比较运算符优先级
     *
     * @param op1
     * @param op2
     * @return op1比op2相同或低级则返回true，op1比op2高级则返回false
     */
    private boolean jubgmentPriority(String op1, String op2) {
        return (OP_PRIORITY_MAP.get(op1) - OP_PRIORITY_MAP.get(op2)) <= 0;
    }
}