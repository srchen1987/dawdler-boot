# dawdler-boot-web-tomcat-websocket

## 模块介绍

嵌入式tomcat容器的websocket模块支持.

### 1. 使用方式

pom.xml中加入

```xml
    <dependencies>
        <dependency>
            <groupId>club.dawdler</groupId>
            <artifactId>dawdler-boot-web-tomcat-websocket</artifactId>
        </dependency>
    </dependencies>
```

### 2. 使用示例

创建WebSocketServer类,并将此类的包配置到[dawdler-boot-web中component-package-paths的配置](../dawdler-boot-web/README.md#1-通用配置信息) component-package-paths中.

```java

@ServerEndpoint(value = "/websocket", configurator = HttpSessionConfigurator.class)
public class WebSocketServer {
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
         System.out.println("open");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("close");
    }

    @OnError
    public void onError(Throwable error) {
    System.out.println(error.getMessage());
    }

}

```
