# dawdler-boot-web

## 模块介绍

嵌入式dawdler-web容器的根模块, 提供注解、配置、组件提供者、启动器等功能. 一般开发者不需要使用该模块, 除非有扩展其他容器的需求比如 jetty、tomcat.

### 1. 配置信息

1、 undertow.yml 目前支持的容器undertow. (未来支持tomcat, jetty 则为tomcat.yml、jetty.yml)

```yaml

server:
 host: 192.168.1.188 #web容器的web地址 默认为空 绑定0.0.0.0
 port: 8080 #web容器提供http请求的端口号 默认8080
 http2: false #是否支持http2 默认为false
 graceful-shutdown: true #是否优雅停机 默认为true
 graceful-shutdown-timeout: 30000 # 优雅停机的等待时间 单位为毫秒 默认为30000毫秒

context-path: user-api #web的context-path 默认为空 如果设置为user-api 访问http://localhost:8080/user-api/xxx 如果不设 http://localhost:8080/xxx
deploy-name: user-api # web的deploy-name 默认为空
static-location: static #web的静态资源文件位置 默认不绑定 用于存放 html jsp 图片 js脚本等 
component-package-paths: #扫描servlet组件的包路径支持antpath 支持 filter、servlet、listener、ServletContainerInitializer中的@HandlesTypes,支持注入其他组件到全局变量 如 service、mq、redis schedule 等
 - com.xxx.listener
 - com.xxx.servlet
error-pages: # 错误页面定义 错误状态码 错误页面的文件
 500: /500.html
 404: /404.html

compression:
 enabled: false #是否开启压缩,只有前端代理服务器或游览器支持压缩时才有效,请求头中包含Accept-Encoding: gzip, deflate
 mime-types: #默认支持的类型.可不填,需要扩展可以填写
 - text/html
 - text/xml
 - text/plain
 - text/css
 - text/javascript
 - application/javascript
 - application/json
 - application/xml
 min-response-size: 65536 #压缩的阈值,单位为kb 默认为64kb

```

2、logback.xml 日志配置文件

3、dawdler.cer 公钥证书 可以采用默认的. 如果需要重新制作, 请参考[采用keytool制作证书](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-server/README.md#22-采用keytool制作证书)

4、dawdler-config.yml 统一配置中心文件 如果采用统一配置中心则需要此配置 [dawdler-config.yml](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-config-center/dawdler-config-center-consul/dawdler-config-center-consul-core/README.md#1-dawdler-configyml配置文件)

5、client-conf.xml [client-conf.xml配置](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-client/README.md#2-client-confxml配置文件说明)、[扫描组件包配置](https://github.com/srchen1987/dawdler-series/blob/master/dawdler/dawdler-client-plug-web/README.md#10-扫描组件包配置)、[远程加载组件](https://github.com/srchen1987/dawdler-series/tree/master/dawdler/dawdler-load-plug/dawdler-client-plug-load/README.md#2-配置需要加载的组件)

### 3. 配置启动服务类

```java

package com.dawdler.user.application;

import club.dawdler.boot.web.annotation.DawdlerBootApplication;
import club.dawdler.boot.web.starter.DawdlerWebApplication;

@DawdlerBootApplication
public class UserWebApplication {
 public static void main(String[] args) throws Throwable {
  DawdlerWebApplication.run(UserWebApplication.class, args);
 } 
}

```

### 4. 通过dawdler-boot-maven-plugin打包运行方式

```sh
java  -jar xxx-all.jar
```

### 5. 运行时指定端口号

启动参数指定优先于虚拟机系统属性指定.

#### 5.1 启动参数指定

```shell
--server.port=8081
```

#### 5.2 虚拟机系统属性指定

```shell
-Dserver.port=8081
```