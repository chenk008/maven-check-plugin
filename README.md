插件介绍
每次发布的时候，遇到jar依赖冲突很苦恼，本插件就是为了帮助你扫描冲突的。

Mojo作为插件执行的处理器，在注释中写标注，设置Mojo的属性、可以接受的参数、IOC依赖
Maven默认使用的IoC容器：plexus
依赖配置在：META-INF/plexus/components.xml
install到本地之后，使用  mvn ck.maven:pv-plugin:check


插件特点
常规分析依赖冲突，是通过分析pom.xml定义的maven依赖坐标之间的区别（gid、aid、version），这样的排查有以下几个不足：
1、两个依赖，如果坐标完全不一样，无法识别出是否冲突。
2、一个依赖包含另一个依赖，无法识别两者是否存在部分冲突。
3、依赖树非常庞大，开发人员难免会漏改一个冲突。
4、pom定义未变，依赖树的变化，未知会开发人员，导致产生新的冲突。
为了弥补上述不足，本插件采用直接仲裁分析包内容的方式，分析每个类从而更精确的找出潜在的威胁。
标准的maven插件，可以很方便的使用，以及集成其它系统。比如：aone

 

插件介绍
mvn pv:check 
支持任何打包形式的maven工程。jar、war、ear等。

mvn pv:check-war 
专门为war工程设计，分析war包的lib目录中的jar冲突。 注：check插件扫描maven依赖树，check-war插件扫描war包lib目录。

插件使用
手动运行
直接在命令行运行。

例如：mvn pv:check 或者 mvn com.alibaba.maven.plugins:maven-pv-plugin:1.1-SNAPSHOT:check
mvn pv:check -DcheckPackaging=war,car -DcheckWithoutScope=test,provided,system

或者 mavn pv:check-war 或者 mvn com.alibaba.maven.plugins:maven-pv-plugin:1.1-SNAPSHOT:check-war

自动运行，绑定声明周期。在pom中定义（可以直接定义在当前工程，也可以定义在当前工程的父POM上）
pv:check （默认绑定 generate-sources 声明周期）。绑定后，运行插件不需要什么特殊的操作，编译项目插件自动进行扫描。

<plugin>
  <groupId>com.alibaba.maven.plugins</groupId>
  <artifactId>maven-pv-plugin</artifactId>
  <version>${pv.version}</version>
  <executions>
    <execution>
      <id>check</id>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
</plugin>
 pv:check-war （默认绑定 package 声明周期）。绑定后，运行插件不需要什么特殊的操作，war打包时插件自动进行扫描。

<plugin>
  <groupId>com.alibaba.maven.plugins</groupId>
  <artifactId>maven-pv-plugin</artifactId>
  <version>${pv.version}</version>
  <executions>
    <execution>
      <id>check-war</id>
      <goals>
        <goal>check-war</goal>
      </goals>
    </execution>
  </executions>
</plugin>

插件扫描会生成一个仲裁报告，开发人员根据这个仲裁报告可以人工进行冲突修复了。




仲裁报告各个部分说明
Id：冲突编号，一个唯一标识，在忽略扫描中会用到。
Level：冲突级别。有3个值，一个是fatal、一个是error、一个是warn。
          fatal：这个冲突必须排除，有可能会产生严重后果，出现这种级别的冲突。
          error：这个冲突可以不排除，冲突包扫描发现MD5码是一样的，打包后只会增加体积，但不影响程序稳定，建议排除一下。
          warn：这个冲突，不一定会产生严重后果，多个依赖之间只是部分冲突。打包不失败，只是提示而已。（*开发人员自行判断是否要处理）
Count：扫描到这个冲突项一共有多少个类冲突。
Detail：每个依赖的名字，（）里面是这个jar一共包含有少个类

小提示：如何看懂仲裁报告，判断到底哪个依赖应该被踢掉？

举例：
1、a.jar和b.jar。a.jar里有10个类，b.jar里有10个类，扫描到冲突count有10个。
很显然2者是100%冲突，冲突级别fatal，必须排查。2者随便排除掉一个
2、a1.jar和b1.jar。a1.jar里有10个类，b1.jar里有10个类，扫描到冲突count有10个。
很显然2者是100%冲突，但是扫描发现二者MD5一致，冲突级别error，冲突的包不影响程序稳定，只是打包后体积增大，建议排除一个。
3、c.jar和d.jar。c.jar里有10个类，d.jar里有6个类，扫描到冲突count有6个。
很显然c.jar一定包含了d.jar的所有类，冲突级别error，必须排查。应该把d.jar排除。
4、e.jar和f.jar。e.jar里有20个类，f.jar里有15个类，扫描到冲突count有10个。
这个就不好说了，2者只是部分冲突，有一个交集，冲突级别warn，是否处理具体问题具体分析，开发人员自行判断。



插件日志结果说明
B2B跟淘宝的mvn仓库合并后，同名类冲突的问题一直困扰我们，目前已经在顶级pom上配置了一个同名类冲突的检测插件，在每次编译的时候默认执行，并在编译日志中输出冲突详情。

一、以下是冲突日志的详细解读

输出日志	注解
[INFO] [pv:check{execution:check}] 
 [INFO]Documentation:http://docs.alibaba-inc.com/pages/viewpage.actionpageId=52111636 
[INFO] Check project packaging type : war,ali-war 
[INFO] Check dependency without scope type : test,provided 
…… 
[INFO] Scan to 484 dependencies 
[INFO] +- activation-1.1.jar:compile 
[INFO] +- ajax.json.java-0.0.0.jar:compile 
[INFO] +- ajax.jsonlib-0.0.0.jar:compile 
…… 
conflict output begin 
--------------------------------------------------------------------------------------------- 
The jars conflict. 
Id : jetty-util-6.1.26.jar,server.jetty.jetty-util-6.1.21.jar 
Level : fatal 
Count : 105 
Detail : 
org.mortbay.jetty:jetty-util:jar:6.1.26:compile >> jetty-util-6.1.26.jar (105) 
com.alibaba.external:server.jetty.jetty-util:jar:6.1.21:compile >> server.jetty.jetty-util-6.1.21.jar (105) 
org.mortbay.jetty:jetty-util:jar:6.1.26:compile >> jetty-util-6.1.26.jar (105) 
com.alibaba.external:server.jetty.jetty-util:jar:6.1.21:compile >> server.jetty.jetty-util-6.1.21.jar (105) 
----------------------------------------------------------------------------------------------------------------  
The jars may be the inclusion relationship conflict. 
Id : commons-beanutils-1.8.0.jar,commons-beanutils-core-1.8.0.jar 
Level : fatal 
Count : 118 
Detail : 
commons-beanutils:commons-beanutils:jar:1.8.0:compile >> commons-beanutils-1.8.0.jar (137)  
commons-beanutils:commons-beanutils-core:jar:1.8.0:compile >> commons-beanutils-core-1.8.0.jar (118) 
---------------------------------------------------------------------------------------------------------------- 
The conflict jars has the same MD5 value. 
Id : xml.dom4j-1.6.1.jar,dom4j-1.6.1.jar 
Level : error 
Count : 190 
Detail : 
dom4j:dom4j:jar:1.6.1:compile >> dom4j-1.6.1.jar (190) 
com.alibaba.external:xml.dom4j:jar:1.6.1:compile >> xml.dom4j-1.6.1.jar (190) 
----------------------------------------------------------------------------------------------------------------- 
The jars cannot determine the logic of conflict. 
Id : jsp-2.1-6.1.14.jar,jasper-compiler-5.5.23.jar 
Level : warn 
Count : 144 
Detail : 
org.mortbay.jetty:jsp-2.1:jar:6.1.14:compile >> jsp-2.1-6.1.14.jar (585) 
tomcat:jasper-compiler:jar:5.5.23:runtime >> jasper-compiler-5.5.23.jar (180) 
---------------------------------------------------------------------------------------------- 
conflict output end 
****************************************************** 
[INFO] [pv*-fatal] : Scan the dependencies found conflict. ( 138 ) 
[INFO] fatal level : 67 
[INFO] error level : 12 
[INFO] warn  level : 59 

插件名称及开始标记 
插件的详细说明文档，开发也可手工本地命令方式运行 
顶级pom上默认指定插件检测的packaging类型 
顶级pom上默认指定插件检测的jar生效范围 
  
打到war包里的jar总数，下面输出列出打到war中的jar 
  
  
  
  
  
冲突的jar输出开始 
------------------------------------------------------------------------------ 
两个jar的class数量相等，但是md5不一致，这种冲突可能是同一个库引入不同坐标不同版本的jar，这种风险往往是致命的，必须排除。 
  
  
  
  

  


------------------------------------------------------------------------------------------------- 
冲突的class数量刚好等于其中class数量小的jar,这种冲突可能是父包和子包的关系，如果完全包含那风险不大，如果版本不一致那也可能是致命的。需要分析后排除，webx2等框架的jar这种情况很多。 
  
  
  

  

------------------------------------------------------------------------------------------------ 
这种冲突是同一个库引入了不同坐标的jar，因为其md5是一致的，因此理论上不会造成风险，但是会导致war里包的冗余，建议排除，配管这边也正在和架构师一起在搞统一解决方案。 
  
  
  
  
  -------------------------------------------------------------------------------------------------- 
冲突的class数量不等于任何一个jar，这种冲突无法判断冲突的逻辑关系，一般很难简单的排除其中一个。 
  
  
  
  
  
------------------------------------------------------------------------------------------------- 
冲突的jar输出结束 
********************************************** 
检测出冲突的总数，以及各种级别冲突的数量
 

二、冲突解决建议

1、目前这个插件检测出的冲突不会导致aone上编译审计失败，只作为开发解决冲突的一个参考信息

 

2、从中文站的扫描结果来看，大部分应用的冲突数量都在100以上，但是为什么这些冲突平时很少引发编译或启动异常呢？ 这是因为jvm的class加载机制相对来说是稳定的，比如总是按照字母排序的方式加载class。 但是这种稳定可能因为环境的变动而破坏，我们还是强烈建议开发分析后排除一些冲突逻辑明确的冲突。

3、根据实际项目中遇到因为同名类冲突导致异常一般都是因为项目本身引入了新的依赖，或者因为引用的依赖为SNAPSHOT版本，导致间接引入了新的依赖而导致的冲突。当前，如果是开发自己引入新的jar，可以在编译审计日志的冲突结果里搜索下这个jar，看有没有冲突。

Aone考虑实现的方法：我们假设主干的冲突是稳定的，aone保存每次发布成功后主干包的jar列表，以及冲突列表；分支每次编译同样打出一份jar列表和冲突列表，进行比对，并把差异显示给用户。（这个实现要跟aone开发这边沟通）

4、整个集团mvn库的融合，冲突的解决注定是一个艰巨而复杂的工作，配管会owner并协同各个BU的架构师，开发以及一些热心的maven专家一起系统的推进这个工作。




高级技巧 (pv:check独有)
默认check插件会扫描所有种类的工程，想限制只扫描war、car？ *1.1版本新增
配置参数checkPackaging

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check</id>
        <goals>
          <goal>check</goal>
        </goals>
        <configuration>
          <!-- 默认为空，或不配置此参数表示不限制工程类型-->
          <checkPackaging>war,car</checkPackaging>
        </configuration>
      </execution>
    </executions>
</plugin>
默认check插件会扫描所以类型的依赖，不像扫描test、provided类型的scope？ *1.1版本新增
配置参数checkWithoutScope

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check</id>
        <goals>
          <goal>check</goal>
        </goals>
        <configuration>
          <!-- 默认为空，或不配置此参数表示全部类型scope都扫描-->
          <checkWithoutScope>test,provided,system</checkWithoutScope>
        </configuration>
      </execution>
    </executions>
</plugin>
高级技巧 (pv:check、pv:check-war通用)
扫描出fatal级别的冲突，希望编译失败、打包失败如何操作？
配置参数isTerminatePackaging

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check-war</id>
        <goals>
          <goal>check-war</goal>
        </goals>
        <configuration>
          <!-- 默认 false -->
          <isTerminatePackaging>true</isTerminatePackaging>
        </configuration>
      </execution>
    </executions>
</plugin>
扫描出fatal级别的冲突，希望删除打好的包？
同时配置参数isDeleteFailurePackage、isTerminatePackaging

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check-war</id>
        <goals>
          <goal>check-war</goal>
        </goals>
        <configuration>
          <!-- 下面2个参数需要同时配置为true -->
          <!-- 默认 false -->
          <isTerminatePackaging>true</isTerminatePackaging>
          <!-- 默认 false -->
          <isDeleteFailurePackage>true</isDeleteFailurePackage>
        </configuration>
      </execution>
    </executions>
</plugin>
扫描出error、warn级别的冲突，也希望受到上二项配置的影响（提示编译失败、删除编译失败的包）？
多配置一个插件参数level，指定级别为warn或者error

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check-war</id>
        <goals>
          <goal>check-war</goal>
        </goals>
        <configuration>
          <!-- 同时配置下面3个参数 -->
          <!-- 默认 fatal -->
          <level>error</level>
          <!-- 默认 false -->
          <isTerminatePackaging>true</isTerminatePackaging>
          <!-- 默认 false -->
          <isDeleteFailurePackage>true</isDeleteFailurePackage>
        </configuration>
      </execution>
    </executions>
</plugin>
设置严格到warn级别后，扫描到的一个冲突项，分析后认为是合理的，想排除这个冲突的提示怎么办？
配置一个warn忽略列表，要忽略哪个冲突项，写哪个冲突项的ID。

<plugin>
    <groupId>com.alibaba.maven.plugins</groupId>
    <artifactId>maven-pv-plugin</artifactId>
    <version>${pv.version}</version>
    <executions>
      <execution>
        <id>check-war</id>
        <goals>
          <goal>check-war</goal>
        </goals>
        <configuration>
          <!-- 同时配置下面4个参数 -->
          <!-- 默认 fatal -->
          <level>warn</level>
          <!-- 默认 false -->
          <isTerminatePackaging>true</isTerminatePackaging>
          <!-- 默认 false -->
          <isDeleteFailurePackage>true</isDeleteFailurePackage>
          <!-- warn级别冲突忽略列表，填写冲突的ID -->
          <excludes>
            <exclude>xxxxxxxx</exclude>
          </excludes>
        </configuration>
      </execution>
    </executions>
</plugin>
高级技巧 (pv:check-war独有)
*（通常不需要考虑）编译路径target采用自定义路径？
配置target参数。

*（通常不需要考虑）我的web工程pom配置很特殊，编译打包生成不采用默认路径。
配置war路径参数warDirPath、warPath