# dawdler-boot-server

## 模块介绍

嵌入式dawdler-server容器,支持ide中main方法运行或dawdler-boot-maven-plugin插件构建的fatjar方式运行.

### 1. 使用方式

pom.xml中加入

```xml
	<dependencies>
		<dependency>
			<groupId>dawdler-boot</groupId>
			<artifactId>dawdler-boot-server</artifactId>
		</dependency>
	</dependencies>
```

### 2. 配置文件

以下配置文件存放在 classpath下,一般为源码的resources下.

1、dawdler.keystore 私钥证书 可以采用默认的.如果需要重新制作,请参考[采用keytool制作证书](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server/README.md#22-采用keytool制作证书) 

2、dawdler-config.yml 统一配置中心文件 如果采用统一配置中心则需要此配置 [dawdler-config.yml](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-config-center/dawdler-config-center-consul/dawdler-config-center-consul-core/README.md#1-dawdler-configyml配置文件)

3、logback.xml 日志配置文件

4、server-conf.xml dawdler服务器配置. 如果需要自定义配置,请参考[dawdler-conf.xml](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server/README.md#21-server-confxml说明)

5、services-config.xml 服务基本配置包括 [服务扫描路径](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server-plug/README.md#3-配置需要扫描的包)、[未使用统一配置中心的情况下的数据源](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server-plug-db/README.md#3-数据源配置)、 [数据源规则配置](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server-plug-db/README.md#4-数据源规则配置)、[数据源绑定服务配置](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server-plug-db/README.md#5-数据源绑定服务配置)

### 3. 配置启动服务类

```java

package com.dawdler.user.application;

import com.anywide.dawdler.boot.server.annotation.DawdlerBootApplication;
import com.anywide.dawdler.boot.server.starter.DawdlerServerApplication;
@DawdlerBootApplication(serviceName = "user-service") // 服务名
public class UserServiceApplication {
	public static void main(String[] args) throws Exception {
			DawdlerServerApplication.run(UserServiceApplication.class, args);
	}
}

```

### 4. ide启动参数配置(JPMS jdk17+以上需要)

```shell

--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/jdk.internal.perf=dawdler.boot.server --add-opens=java.base/jdk.internal.loader=dawdler.boot.server --add-opens=java.base/java.lang=org.aspectj.weaver --add-opens=java.base/jdk.internal.loader=dawdler.server --add-opens=java.base/jdk.internal.perf=dawdler.server --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED --add-opens=java.base/jdk.internal.perf=ALL-UNNAMED --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=dawdler.core --add-opens=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED

```

### 5. 通过dawdler-boot-maven-plugin打包运行方式(JPMS jdk17+以上需要)

```shell
java --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED --add-opens=java.base/jdk.internal.perf=ALL-UNNAMED --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/jdk.internal.loader=dawdler.boot.classloader --add-opens=java.xml/com.sun.org.apache.xerces.internal.dom=ALL-UNNAMED --add-opens=java.base/java.lang=org.aspectj.weaver-p xxx-all.jar -m dawdler.boot.classloader

```