# Elastic-Config  Distributed Configuration Center Based On ZK

Elastic-Config是基于ZooKeeper的分布式配置中心,简化在分布式下环境中的配置，目前主要特性：  
1.配置简单，与Spring整合以及命名空间提供，在项目中引入配置标签即可
2.同时支持本地properties文件配置和Elastic-Config配置，优先使用本地properties文件配置
2.支持Spring @Value注解注入，兼容Spring @Value注解，项目无需作大的改动   
3.支持动态更新（本地properties文件配置动态更新修改暂不支持，后续有支持的计划，无需重启服务器）   
4.提供管理后台，对配置进行统一管理，支持properties文件上传以及批量上传

###后续RoadMap:
1.支持本地配置和修改  
2.提供扩展，在配置发生改变时，用户支定义操作  
3.支持@Value注解注入和本地文件配置热更新  
4.管理后台支持创建配置概结点  
5.提供dump命令，导出配置

###Elastic-Config环境需求：

1.Java 使用JDK1.7及其以上版本。  
2.Zookeeper 使用Zookeeper 3.4.6及其以上版本。  
3.Maven 使用Maven 3.0.4及其以上版本。  
4.Spring 使用Spring 3.1及其以上版本

###Elastic-Config源码编绎：
Elastic-Config依赖lombok。导入源码会遇lombok中的注解导致不能编译，请安装lombok到你的IDE.  
lombok 的官方网址：[http://projectlombok.org](http://projectlombok.org)

#####lombok 安装  
使用 lombok 是需要安装的，如果不安装，IDE 则无法解析 lombok 注解。

eclipse / myeclipse 手动安装 lombok   
 
    1. 将 lombok.jar 复制到 myeclipse.ini / eclipse.ini 所在的文件夹目录下  
    2. 打开 eclipse.ini / myeclipse.ini，在最后面插入以下两行并保存：
        -Xbootclasspath/a:lombok.jar
        -javaagent:lombok.jar
    3.重启 eclipse / myeclipse

idea 打开IDEA的Settings面板，并选择Plugins选项,点击 “Browse repositories..”,输入框输入”lombok”，搜索结果,点击安装即可！

#####开发指南

1.引用依赖：   

Elastic-Config 已推送到Maven中央仓库，在pom文件引入Maven依赖:
		
	<dependency>
	    <groupId> com.github.erindavid</groupId>
	    <artifactId>elastic-config-core</artifactId>
	    <version>1.0.0-release</version>
	</dependency>
	
	<dependency>
	    <groupId> com.github.erindavid</groupId>
	    <artifactId>elastic-config-spring</artifactId>
	    <version>1.0.0-release</version>
	</dependency>

2.引入命名空间和定义配置节点：

	<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
	xmlns:register="http://www.github.com/schema/config/register"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
             http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
             http://www.github.com/schema/config/register http://www.github.com/schema/config/reg/reg.xsd">


	<context:component-scan base-package="com.github.config" />
	<context:property-placeholder location="classpath:conf/*.properties" />
	
	<register:config id="elasticconfig0" serverlist="${zk.address}"
		namespace="${zk.namespace}" project="${config.project}" version="${config.version}"
		node="${config.node0}"/>
    </beans>

备注：  
serverlist：zookeeper服务器列表，包括IP地址和端口号. 多个地址用逗号分隔. 如: host1:4181,host2:4181    
namespace：配置的命名空间  
project：项目名称  
version：项目版本  
node：配置节点

注：对于需要执更新的配置，在项目注入ElasticConfig实例，从ElasticConfig实例获取配置的值，如果有多个ElasticConfig实例，注入时需要指定实例名称,对于只需要配置用Spring @Value注入，对于像数据库连接这类需要初始化的配置，ElasticConfig提供了refresh属性，refresh为true，更新会刷新容器--刷新容过程中，不能有业务处理！

3.创在ZK上创建根节点，下载elastic-config-console运行，导入配置即可。

#####项目结构
elastic-config-core：ElasticConfig核心模块  
elastic-config-spring：命名空间提供模块  
config-toolkit-demo：ElasticConfig Demo  
elastic-config-console:ElasticConfig后台管理  

