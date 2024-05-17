# dawdler-boot-web-undertow

## 模块介绍

嵌入式undertow容器的实现.

### 1. 使用方式

pom.xml中加入

```xml
	<dependencies>
		<dependency>
			<groupId>dawdler-boot</groupId>
			<artifactId>dawdler-boot-web-undertow</artifactId>
		</dependency>
	</dependencies>
```

### 2. 配置信息

undertow.yml 目前支持的容器undertow. (未来支持tonmcat,jetty 则为tomcat.yml、jetty.yml)

```yaml

undertow:
 #io-threads: #工作线程创建的I/O线程数,默认值源自可用处理器的数量.
 #worker-threads: #工作线程数,默认值是I/O线程数的8倍.
 #buffer-size: #每个缓冲区的大小,默认值源自JVM可用的最大内存量.单位为byte.
 #direct-buffers: #是否开启堆外内存缓冲区, 默认值当jvm大小超过64M时则开启.
 #virtual-thread: #是否开启虚拟线程 默认未开启
 #undertow-options: #undertow下的配置,一般不需要配置,如需自定义配置请具体参考UndertowOptions.java
  #max_headers: 200 #最大header请求个数 
 #socket-options: #socket配置项 一般不需要配置采用默认即可,如需自定义配置请具体参考org.xnio.Options.java

access-log: #访问日志
 enabled: false #是否开启accesslog
 pattern: common #日志格式 common=%h %l %u %t %r %s %b  combined=%h %l %u %t \"%r\" %s %b %{i,Referer} %{i,User-Agent}  commonobf=%o %l %u %t %r %s %b  combinedobf=%o %l %u %t %r %s %b %{i,Referer} %{i,User-Agent}
 prefix: access_log. #日志前缀
 suffix: log #日志后缀
 dir: logs #日志目录
 rotate: true #日志轮替

 ```