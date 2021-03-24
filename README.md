# JWTUtils
快速生成token工具包
# JWTUtils2.2使用详解

**引言**

这是一个我自己写的一个工具类，用于快速生成Token的一个工具类，GitHub地址：https://github.com/Hanzoy/JWTUtils

该工具包提供了JWTUtils核心类和@Token注解，通过在实体类属性上使用@Token注解，可以快速根据注解标记的属性创建Token，也可以快速从token中解析出我们所需要的实体类

### 依赖引入

在pom.xml中加入依赖

```xml
<!-- https://mvnrepository.com/artifact/com.hanzoy/utils -->
<dependency>
    <groupId>com.hanzoy</groupId>
    <artifactId>utils</artifactId>
    <version>2.2</version>
</dependency>
```

### 基本使用

#### springboot配置

在springboot配置文件中添加**token签名**与**token有效期**

![image-20210311001631524](http://picture.hanzoy.com/img/image-20210311001631524.png)

其中time属性单位为**秒**，支持如图所示的表达式

#### @Token

注解在**属性**上

该注解用于标记需要生成token的属性，该注解可重复注解，多个注解时，后面加上对应的值可做到分组效果

```java
import com.hanzoy.utils.Token;

public class People {
    private Long id;

    @Token
    @Token("group2")
    private String name;

    @Token
    @Token("group2")
    private int age;
    
    @Token
    private String userid;
    private String password;
}
```

上述示例中通过对token添加不同的值，将其分类为默认组和**"group2"**组

**该注解是支持复杂类型的字段**

#### JWTUtils类

该类是该工具包的核心类，该类通过springboot配置文件传入**sign**（签名）和**time**（有效期）

通过spring自动注入的方式获取该类

![image-20210311001918456](http://picture.hanzoy.com/img/image-20210311001918456.png)

##### createToken方法

![image-20210311002043578](http://picture.hanzoy.com/img/image-20210311002043578.png)

该方法传入一个对象，工具类会自动扫描获取到拥有@Token注解的字段，并将其写入token中，传入group可以扫描指定分组的字段

实例方法只需传入需要生成token的实例 **t** 就行了，如果需要特定组的在后面填上value即可，如：若使用上面的**group2**组，则在value上传入**"group2"**字符串即可

```java
People people = new People();
people.setName("hanzoy");
people.setAge(19);
people.setUserid("hanzoy");
people.setPassword("123456");

String token1 = jwtUtils.createToken(people);
String token2 = jwtUtils.createToken(people, "group2");
```

当然该方法也支持传入一个map集合，但是map必须为**Map<String, ?>**的子类



##### createTokenFromMap方法

![image-20210311002605091](http://picture.hanzoy.com/img/image-20210311002605091.png)

该方法传入一个**Map<String, ?>**极其子类参数，上面的**createToken方法**接受到map类型参数时会自动调用该方法。

```java
HashMap<String, Object> hashMap = new HashMap<>();
hashMap.put("name", "hanzoy");
hashMap.put("age", 19);
ArrayList<String> books = new ArrayList<>();
books.add("Chinese");
books.add("English");
hashMap.put("Books", books);

String token1 = jwtUtils.createTokenFromMap(hashMap);
String token2 = jwtUtils.createToken(hashMap);
```



##### createTokenCustomFields方法

![image-20210311002923616](http://picture.hanzoy.com/img/image-20210311002923616.png)

该方法传入一个对象以及需要写入token的字段名称，例如：

```java
String token = jwtUtils.createTokenCustomFields(people, "name", "age", "userid");
```



##### checkToken方法

![image-20210122173354028](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122173354028.png)

该方法提供检验token签名和有效期的功能



##### getBean方法

![image-20210311004153754](http://picture.hanzoy.com/img/image-20210311004153754.png)

该方法提供了将token转化为对应的java对象的方法。

例如：

```java
People people = jwtUtils.getBean(token, People.class);
```



##### getBeanAsMap方法

![image-20210311004348019](http://picture.hanzoy.com/img/image-20210311004348019.png)

该方法提供了将token转化为Map集合的方法，默认将其转化为**Map<String, Object>**类型，同样也支持自定义返回类型

例如：

```java
Map<String, Object> map1 = jwtUtils.getBeanAsMap(token);
Map<String, String> map2 = jwtUtils.getBeanAsMap(token, String.class);
```

##### getValueFromToken方法

![image-20210324193254379](http://picture.hanzoy.com/img/image-20210324193254379.png)

该方法提供了从token中通过key值直接获取value值的方法。

例如：

```java
String username = jwtUtils.getValueFromToken(token, "username");
```

