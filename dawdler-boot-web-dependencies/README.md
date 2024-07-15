# dawdler-boot-web-dependencies

## 模块介绍

用于导入dawdler-boot版的web层依赖.

### 1. 使用方式

pom.xml中加入

```xml
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.github.dawdler-series</groupId>
				<artifactId>dawdler-boot-web-dependencies</artifactId>
				<version>1.0.4-jdk1.8-RELEASES</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

经过以上配置即可使用dawdler统一管理的组件及其三方组件的jar.