# JWTUtils
快速生成token工具包
# JWTUtils1.0使用详解

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
    <version>1.0</version>
</dependency>
```

### 基本使用

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

该类是该工具包的核心类，该类传入**sing**（签名）和**time**（有效期）

##### createToken方法

![image-20210122155116920](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122155116920.png)

![image-20210122155347248](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122155347248.png)

该方法提供了使用JWTUtils实例方法和静态方法两种使用方式

实例方法只需传入需要生成token的实例 **t** 就行了，如果需要特定组的在后面填上value即可，如：若使用上面的**group2**组，则在value上传入**"group2"**字符串即可

当然也支持传入一个map集合

##### checkToken方法

![image-20210122173354028](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122173354028.png)

![image-20210122173321060](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122173321060.png)

该方法提供检验token签名和有效期的功能，同样也提供了静态版本

##### getBean方法

![image-20210122173716068](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122173716068.png)

![image-20210122173752763](https://hanzoy-picture.oss-cn-chengdu.aliyuncs.com/img/image-20210122173752763.png)

该方法提供了将token转化为对应的java对象的方法，同样也提供了静态版本。
