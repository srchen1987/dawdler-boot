# dawdler-boot-web-tomcat-jsp

## 模块介绍

嵌入式tomcat容器的jsp模块支持.

### 1. 使用方式

pom.xml中加入

```xml
    <dependencies>
        <dependency>
            <groupId>club.dawdler</groupId>
            <artifactId>dawdler-boot-web-tomcat-jsp</artifactId>
        </dependency>
    </dependencies>
```

### 2. 配置信息

在tomcat.yml中需要配置static-location.具体参考 [配置信息](../dawdler-boot-web/README.md#1-配置信息).jsp文件放在此目录下.

### 3. JSTL支持

本模块现在支持JSTL标签库，可以在JSP文件中使用以下标签库：

```jsp
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sql" uri="jakarta.tags.sql" %>
<%@ taglib prefix="x" uri="jakarta.tags.xml" %>
```

示例JSP文件：

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<html>
<head>
    <title>JSTL Test Page</title>
</head>
<body>
    <h1>JSTL Test Page</h1>
    
    <h2>Core Tags Test</h2>
    <c:set var="testVar" value="Hello JSTL!" />
    <p>Value of testVar: <c:out value="${testVar}" /></p>
    
    <h2>Functions Test</h2>
    <c:set var="testString" value="Hello World" />
    <p>Length of testString: ${fn:length(testString)}</p>
    <p>Uppercase testString: ${fn:toUpperCase(testString)}</p>
</body>
</html>
```

### 4. 自定义jsp标签库

在 ```resources/META-INF/``` 下创建tld文件.

示例:

custom.tld

```tld
<?xml version="1.0" encoding="UTF-8"?>
<taglib xmlns="https://jakarta.ee/xml/ns/jakartaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_3_0.xsd" version="3.0">
    
    <description>Custom tags library</description>
    <display-name>reverse</display-name>
    <tlib-version>1.0</tlib-version>
    <short-name>cm</short-name>
    <uri>custom</uri>
    <tag>
        <name>reverse</name>
        <tag-class>com.example.demo.tag.ReverseTag</tag-class>
        <body-content>empty</body-content>
        <attribute>
            <name>value</name>
            <required>true</required>
            <rtexprvalue>true</rtexprvalue>
        </attribute>
    </tag>
</taglib>
```

ReverseTag.java

```java
package com.example.demo.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ReverseTag extends SimpleTagSupport {
    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        if (value != null) {
            String reversedValue = new StringBuilder(value).reverse().toString();
            out.print(reversedValue);
        }
    }
}
```

demo.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="cm" uri="custom"%> 
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Simple JSP Example with JSTL</title>
</head>
<body>
    <h1>Hello from JSP!</h1>
    <p>Current time: <%= new java.util.Date() %></p>
    <p>Server info: <%= application.getServerInfo() %></p>
    <cm:reverse value="Hello World!" />
    <% pageContext.setAttribute("name", "Dawdler"); %>
    ${name}
    
    <!-- 创建一个List并使用JSTL循环遍历 -->
    <%
        java.util.List<String> fruits = new java.util.ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Orange");
        fruits.add("Grape");
        fruits.add("Strawberry");
        request.setAttribute("fruits", fruits);
    %>
    
    <h2>Fruits List (using JSTL forEach)</h2>
    <ul>
        <c:forEach var="fruit" items="${fruits}">
            <li>${fruit}</li>
        </c:forEach>
    </ul>
    
    <!-- 使用JSTL循环输出数字1-5 -->
    <h2>Numbers 1-5 (using JSTL forTokens)</h2>
    <%
        pageContext.setAttribute("numbers", "1,2,3,4,5");
    %>
    <ul>
        <c:forEach var="number" items="${fn:split(numbers, ',')}">
            <li>Number: ${number}</li>
        </c:forEach>
    </ul>
    
    <!-- 带索引的循环 -->
    <h2>Fruits with Index</h2>
    <table border="1">
        <tr>
            <th>Index</th>
            <th>Fruit</th>
        </tr>
        <c:forEach var="fruit" items="${fruits}" varStatus="status">
            <tr>
                <td>${status.index + 1}</td>
                <td>${fruit}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
```
