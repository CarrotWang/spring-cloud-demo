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

最后对于每个组件，我们要清楚它的实现原理。

## Spring Cloud组件
### Eureka
![Eureka架构图](https://raw.githubusercontent.com/CarrotWang/spring-cloud-demo/master/imgs/Eureka.png "Eureka架构图") 

Eureka的架构图如上，Eureka分为Eureka Server、Eureka Client。单独部署Eureka Server，各Java微服务集成Eureka Client，与Eureka Server通信。

服务注册：
     微服务启动时，Eureka Client向Eureka Server注册自己的信息（服务信息、网络信息）；
     
服务发现：
     服务消费者调用服务时，Eureka Client会查看本地是否有服务缓存信息，没有的情况下会去Eureka Server拉取信息；

（1）信息同步     
Eureka Server间的信息同步：每个Eureka Server同时也是Eureka Client，互相之间注册，Eureka Server之间点对点以复制的方式同步“服务注册表”（由其他Eureka Server同步的信息不会同步）。

（2）客户端缓存
服务消费者要调用服务时，会通过Eureka Client向Eureka Server获取服务提供者地址列表，并缓存在本地，下次调用，则直接从本地缓存获取。

（3）服务列表维护
服务提供者在启动后，周期性（默认30秒）向Eureka Server发送心跳，以证明当前服务是可用状态。Eureka Server在一定的时间（默认90秒）未收到客户端的心跳，则认为服务宕机，注销该实例，并把当前服务提供者状态向订阅者发布，订阅过的服务消费者更新本地缓存。

（4）Eureka保护机制
当Eureka Server集群和服务集群发生分区时，会导致大部分服务被错误的判断为不可用，为了防止此情况发生，Eureka提供了保护机制，当Eureka Server节点在短时间内丢失过多的客户端时（可能发送了网络故障），那么这个节点将进入自我保护模式，不再注销任何微服务，当网络故障回复后，该节点会自动退出自我保护模式。（为了提高可用性）

与Zookeeper实现的注册发现中心不同，Eureka保证的是AP，而不是CP，更适合服务注册发现的场景。（Zookeeper有节点宕机时，整个集群需要重新进行领导选举，从而达到一致状态，而Eureka允许不一致状态的存在，对于注册发现服务是可以接受的）。

https://www.cnblogs.com/snowjeblog/p/8821325.html
https://www.jianshu.com/p/2fa691d4a00a

### Ribbon
实现客户端软负载均衡，具体的负载均衡算法：随机负载均衡、轮询、加权轮询、加权响应时间负载均衡、区域感知负载均衡。
（负载均衡方式：DNS、硬件负载均衡、软件负载均衡）

### Hystrix
Hystrix的作用有“依赖隔离”、“熔断”、“降级”，目的在于提高服务的鲁棒性和可用性。

1. 依赖隔离：

   Hystrix隔离方式采用线程/信号的方式,通过隔离限制依赖的并发量和阻塞扩散（某一请求因为依赖阻塞大量占用容器线程，导致其他请求得不到响应）。
     
![Hystrix执行逻辑图](https://raw.githubusercontent.com/CarrotWang/spring-cloud-demo/master/imgs/Hystrix_Process.png "Hystrix执行逻辑图")     
     
（1）线程池隔离：
   
   调用依赖服务接口的时候，不使用容器的线程，而是使用Hystrix配置的线程池，当某依赖对应的线程池中线程全被占用，且线程池中队列满的时候，再请求该依赖就会执行降级逻辑。
     
（2）信号量隔离：
   
   具体实现在 com.netflix.hystrix.AbstractCommand.TryableSemaphoreActual#tryAcquire ，原理就是通过一个AtomicInteger计数，当小于等于该计数时，继续使用原线程访问依赖，否则执行降级操作。
     
![Hystrix调用流程图](https://raw.githubusercontent.com/CarrotWang/spring-cloud-demo/master/imgs/Hystrix.png "Hystrix调用流程图") 
     
2. 熔断：

3. 降级：

Hystrix实现原理：
命令模式

### Zuul

### Feign

### Sleuth

### Spring Cloud Config


