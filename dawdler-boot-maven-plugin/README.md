# dawdler-boot-maven-plugin

## 模块介绍

用于构建dawdlerboot的fat-jar的插件,构建完成的jar包可以通过java -jar直接运行.

### 1. 使用方式

pom.xml中加入

```xml
			<plugin>
				<groupId>club.dawdler</groupId>
				<artifactId>dawdler-boot-maven-plugin</artifactId>
				<version>0.0.7-jdk21-RELEASES</version>
				<executions>
					<execution>
						<goals>
							<goal>repackage</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
```

mvn install 即可获取xxx-all.jar.