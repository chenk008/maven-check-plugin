插件介绍

每次发布的时候，遇到jar依赖冲突很苦恼，本插件就是为了帮助你扫描冲突的。

Mojo作为插件执行的处理器，在注释中写标注，设置Mojo的属性、可以接受的参数、IOC依赖

Maven默认使用的IoC容器：plexus

依赖配置在：META-INF/plexus/components.xml

install到本地之后，使用  

mvn ck.maven:pv-plugin:check -DcheckPackaging=war,car -DcheckWithoutScope=test,provided,system

mvn ck.maven:pv-plugin:check-war

mvn ck.maven:pv-plugin:sa

mvn ck.maven:pv-plugin:update-snapshots


