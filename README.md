# 1.项目简介

在线markdown格式转换服务, 对外提供http接口, 实现word/ppt/pdf等在线转为markdown


支持：

    1) doc/docx/ppt/pptx转换为markdown;
    2) pdf(扫描/非扫描)转换为markdown;
    3) html转换为markdown.


# 2.项目打包
    
打成war包：

    1) mvn compile;
    2) mvn package.

    
# 3.项目部署

部署在格式转换docker容器下的tomcat/webapp下即可