# dawdler-boot-web-tomcat

## 模块介绍

嵌入式tomcat容器的实现.

### 1. 使用方式

pom.xml中加入

```xml
    <dependencies>
        <dependency>
            <groupId>club.dawdler</groupId>
            <artifactId>dawdler-boot-web-tomcat</artifactId>
        </dependency>
    </dependencies>
```

### 2. 配置信息

tomcat.yml 位于resources下.

```yaml
tomcat:
 #virtual-thread #是否使用虚拟线程 默认false
 #max-threads: #最大线程数 virtual-thread为true时不生效
 #min-spare-threads: #最小空闲线程数 virtual-thread为true时不生效
 #max-queue-size: #最大队列大小 virtual-thread为true时不生效
 #accept-count: #请求队列长度
 #max-connections: #最大连接数
 #max-header-count: #最大请求头数
 #connection-timeout: #连接超时时间(毫秒)
 #keep-alive-timeout: #Keep-Alive超时时间(毫秒)
 #graceful-shutdown-response-timeout: 3000 # 优雅停机后其他请求在此期间会返回503等待时间 单位为毫秒 默认为3000毫秒,如果设置为0 新请求将直接拒绝.
 #max-concurrent-requests: # 最大处理请求数，超过限制的请求将返回503状态码. 为空或为0时不启用, 默认为空.
 #tomcat-options: #tomcat下的配置,一般不需要配置,如需自定义配置请具体参考org.apache.coyote.http11.Http11NioProtocol.java
  #maxKeepAliveRequests: 100

access-log: #访问日志
 enabled: false #是否开启accesslog
 pattern: common #日志格式 common=%h %l %u %t %r %s %b  combined=%h %l %u %t \"%r\" %s %b %{i,Referer} %{i,User-Agent}
 prefix: access_log. #日志前缀
 suffix: log #日志后缀
 dir: logs #日志目录
 rotate: true #日志轮替
```

### 3. 与undertow的切换

dawdler-boot-web-tomcat 和 dawdler-boot-web-undertow 不能同时引入,需要哪个容器就引入对应的依赖即可.
