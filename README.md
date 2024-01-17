# dawdler-boot

![version](https://img.shields.io/badge/dawdler--boot-0.0.2--RELEASES-brightgreen)&nbsp;
[![License](https://img.shields.io/badge/license-apache2.0-green)](LICENSE)&nbsp;
![jdk](https://img.shields.io/badge/jdk-1.8%2B-green)

## 项目介绍

dawdler-boot 是基于dawdler-series开发的一站式分布式应用,支持ide、fastjar运行.

###  dawdler模块介绍(具体文档可以点击标题连接进入子模块查看详细说明)

#### 1. [dawdler-boot-server](dawdler-boot-server/README.md)

嵌入式dawdler-server容器,支持ide中main方法运行或dawdler-boot-maven-plugin插件构建的fatjar方式运行.

#### 2. [dawdler-boot-server-dependencies](dawdler-boot-server-dependencies/README.md)

用于导入dawdler-boot版的service层依赖.

#### 3. [dawdler-boot-web-dependencies](dawdler-boot-web-dependencies/README.md)

用于导入dawdler-boot版的web层依赖.

#### 4. [dawdler-boot-web](dawdler-boot-web/README.md) 

嵌入式dawdler-web容器的根模块,提供注解、配置、组件提供者、启动器等功能.支持ide中main方法运行或dawdler-boot-maven-plugin插件构建的fatjar方式运行.

#### 5. [dawdler-boot-web-undertow](dawdler-boot-web-undertow/README.md) 

嵌入式undertow容器的实现.

#### 6. [dawdler-boot-web-undertow-jsp](dawdler-boot-web-undertow-jsp/README.md) 

嵌入式undertow容器的jsp模块支持.

#### 7. [dawdler-boot-maven-plugin](dawdler-boot-maven-plugin/README.md) 

用于构建dawdlerboot的fat-jar的插件,构建完成的jar包可以通过java -jar直接运行.

#### 8. [dawdler-boot-classloader](dawdler-boot-classloader/README.md) 

支持嵌套的类加载器.
