# 1.项目简介

在线markdown格式转换服务, 对外提供http接口, 实现word/ppt/pdf等在线转为markdown


支持：

    1) doc/docx/ppt/pptx转换为markdown;
    2) pdf(扫描/非扫描)转换为markdown;
    3) html转换为markdown.


# 2.项目打包
    
打成war包：

    在项目根目录下执行：mvn clean install
    在项目中会生成target/ConverterServer.war

    
# 3.项目部署及运行

## 3.1 jetty运行

    1) 把ConverterServer.war拷贝到格式转换docker容器环境下任意路径
    2) 执行：java -jar ConverterServer.war

## 3.2 tomcat运行

    1) 把ConverterServer.war拷贝到格式转换docker容器环境下的tomcat/webapp路径下
    2) tomcat根目录下执行：bin/startup.sh