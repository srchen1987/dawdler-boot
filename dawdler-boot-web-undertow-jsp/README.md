# dawdler-boot-web-undertow-jsp

## 模块介绍

嵌入式undertow容器的jsp模块支持.

### 1. 使用方式

pom.xml中加入

```xml
	<dependencies>
		<dependency>
			<groupId>dawdler-boot</groupId>
			<artifactId>dawdler-boot-web-undertow-jsp</artifactId>
		</dependency>
	</dependencies>
```

### 2. 配置信息

undertow.yml 目前支持的容器undertow. (未来支持tonmcat,jetty 则为tomcat.yml、jetty.yml)

在undertow.yml中需要配置static-location.具体参考 [配置信息](../dawdler-boot-web/README.md#1-配置信息).jsp文件放在此目录下.