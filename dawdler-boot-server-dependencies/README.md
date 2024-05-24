# dawdler-boot-server-dependencies

## 模块介绍

用于导入dawdler-boot-server的依赖.

### 1. 使用方式

pom.xml中加入

```xml
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.github.dawdler-series</groupId>
				<artifactId>dawdler-boot-server-dependencies</artifactId>
				<version>21.2-jdk21-RELEASES</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

经过以上配置即可使用dawdler统一管理的组件及其三方组件的jar.
