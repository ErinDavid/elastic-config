# Elastic-Config  Distribute Configuration Center Based On ZK

Elastic-Config是基于ZooKeeper的分布式配置中心,目前主要特性：  
1.配置简单，与Spring整合以及命名空间提供，在项目中引入一个标签，即可使用    
2.支持Spring @Value注解注入，兼容Spring @Value注解，项目无需作大的改动   
3.支持动态更新（使用@Value注解注入暂时不支持，后续有支持的计划，无需重启服务器）   
4.提供管理后台，对配置进行统一管理，支持 properties文件上传以及批量上传

###后续RoadMap:
1.支持本地配置和修改  
2.提供扩展，在配置发生改变时，用户支定义操作  
3.支持@Value注解注入配置热更新  
4.管理后台支持创建配置概结点  
5.提供dump命令，导出配置

###Elastic-Config环境需求：
Java
使用JDK1.7及其以上版本。  
Zookeeper
使用Zookeeper 3.4.6及其以上版本。  
Maven
使用Maven 3.0.4及其以上版本。  
Spring
使用Spring 3.1及其以上版本

###Elastic-Config源码编绎：
Elastic-Config依赖lombok。导入源码会遇lombok中的注解导致不能编译，请安装lombok到你的IDE。
