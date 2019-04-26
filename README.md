# spring-cloud-demo
该demo演示基本的微服务组成体系。

## 微服务
Loosely coupled service oriented architecture with bounded contexts. 

微服务的优点：

     1. 易于维护
     2. 弹性（可以很好的处理服务不可用和降级）
     3. 可伸缩（只需对存在性能瓶颈的微服务进行扩展）
     4. 易于开发，简化部署（单体服务只改了一行代码就要重新部署，风险大；导致部署频率变低，这又导致不同版本差异大，风险变大）
     5. 与团队组织结构相匹配（康威定律）
     6. 可组合（针对不同平台，web，原生应用，移动web，穿戴设备）
     7. 可替代性强（可以轻易的重写或删除应用）
     8. 技术异构性（用合适的，新的）
     
微服务的六大原则     
![微服务六大原则](https://upload-images.jianshu.io/upload_images/12636540-d6f955bb676cc20e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000 "微服务六大原则")     

实现他们需要有完善的基础设施，对于小公司而言成本是比较高的，幸运的是Spring Cloud为我们提供了这样的一个生态，帮助我们低成本的搭建微服务系统。

## 组成
组件包括：Eureka、Zuul、Hystrix、Ribbon、Feign、Spring Cloud Config、Sleuth。
首先明确每种组件的作用，再来学习如何使用它们。
     
     Eureka：
          负责服务的注册与发现；
     Zuul：
          微服务网关；
     Hystrix：
          用于服务调用的隔离、熔断、降级；
     Ribbon：
          负载均衡；
     Feign：
          负责Rest接口的调用；
     Spring Cloud Config：
          统一的配置；
     Sleuth：
          分布式服务跟踪；
          
![微服务组件](http://www.uml.org.cn/wfw/images/2018050842.png "微服务组件") 
