# dawdler-boot-web-tomcat-source

## 模块介绍

由于embedded-tomcat源码没有遵循JPMS规范,所以需要单独打包出来.

### 1. 使用方式

这个模块不需要单独使用,仅供内部使用.

### 2. embedded-tomcat版本

10.1.52

### 3. 更改记录

- MemorySegment类中的getString换成getUtf8String.

- Arena类中allocateFrom方法换成allocateUtf8String. 部分使用allocate的方法参数有数组需要换成allocateArray. localArena.allocate(ValueLayout.JAVA_BYTE, OPENSSL_ERROR_MESSAGE_BUFFER_SIZE);这种需要换成 localArena.allocate(ValueLayout.JAVA_INT, OPENSSL_ERROR_MESSAGE_BUFFER_SIZE);

- tomcat将jakarta的包打入了tomcat-embed-core.jar中,并加入了XMLSchema.dtd、datatypes.dtd、xml.xsd到jakarta.servlet.resouerces中并在org.apache.tomcat.util.descriptor.DigesterFactory中进行了加载,由于dawdler-series的jdk17以上版本遵循JPMS规范,所以需要将jakarta.servlet.resources中的文件从tomcat-embed-core.jar中移除. 采用标准的jakarta-servlet-api.jar包并将这些配置文件(XMLSchema.dtd、datatypes.dtd、xml.xsd)放在org.apache.tomcat.util.descriptor包下.同时修改DigesterFactory对应的代码进行加载(在DigesterFactory添加locationForLocal).

- 为兼容jar中jar需要改JarFactory中的getJarEntryURL方法. JarFileUrlJar更改构造方法(jarFileURL = url 这个变量赋值).

- 新增org/apache/tomcat/jakartaee 将 jakartaee-migration的源码打进来,这里使用1.0.10版本.
