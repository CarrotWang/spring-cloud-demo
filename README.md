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
   
![Hystrix执行逻辑图](https://github.com/CarrotWang/spring-cloud-demo/blob/master/imgs/Hystrix_Process.png?raw=true "Hystrix执行逻辑图") 

（1）线程池隔离：
   
   调用依赖服务接口的时候，不使用容器的线程，而是使用Hystrix配置的线程池，当某依赖对应的线程池中线程全被占用，且线程池中队列满的时候，再请求该依赖就会执行降级逻辑。
   
   请求线程和执行服务调用的线程分别是Tomcat容器线程和Hystrix设置的线程，这样请求线程能够自动设置超时或者直接异步调用，控制服务线程的占用时间。防止因某个服务调用阻塞，导致大量请求因为容器线程池被占满而得不到响应。
     
（2）信号量隔离：
   
   具体实现在 com.netflix.hystrix.AbstractCommand.TryableSemaphoreActual#tryAcquire ，原理就是通过一个AtomicInteger计数，当小于等于该计数时，继续使用原线程访问依赖，否则执行降级操作。
     
![Hystrix调用流程图](https://raw.githubusercontent.com/CarrotWang/spring-cloud-demo/master/imgs/Hystrix.png "Hystrix调用流程图")

##### “信号量隔离”和“线程池隔离”的比较：

###### 信号量隔离
信号量隔离的优点：
     
请求线程和服务调用线程使用同一线程，避免了线程切换带来的性能损耗。

信号量隔离的缺点：

不可以控制服务调用的等待时间，

如果客户端是可信的且可以快速返回，可以使用信号隔离替换线程隔离,降低开销（一般是系统内部访问，比如访问本地缓存）.

###### 线程池隔离
线程隔离的优点:

（1）将请求处理和服务调用做到线程隔离，请求线程可以控制请求时间，不受服务调用影响；

（2）可以完全模拟异步调用，方便异步编程；

（3）当一个失败的依赖再次变成可用时，线程池将清理，并立即恢复可用，而不是一个长时间的恢复。

线程隔离的缺点:

（1）线程池的主要缺点是它增加了cpu，因为每个命令的执行涉及到排队(默认使用SynchronousQueue避免排队)，调度和上下文切换。

（2）对使用ThreadLocal等依赖线程状态的代码增加复杂性，需要手动传递和清理线程状态。

2. 熔断：

熔断器有三个状态：closed、open、half-open。与家用电路熔断器类似，closed表示关闭熔断，可以发起远程服务调用，open表示打开熔断，执行降级方法。

当熔断器进入open状态时，会开始计时，当时间超过时间窗口，就会变为half-open状态，再调用该服务时，会调用远程接口，若成功则变为closed状态，否则变为open状态重新计时。

每个熔断器默认维护10个bucket,每秒一个bucket,每个blucket记录成功,失败,超时,拒绝的状态，默认错误超过50%且10秒内超过20个请求进行中断拦截。

![Hystrix断路器](https://github.com/CarrotWang/spring-cloud-demo/blob/master/imgs/hystrix-circuit-breaker.png?raw=true  "Hystrix断路器")


3. 降级：

整体资源快不够用了，忍痛将某些服务先关掉，待度过难关，在开启回来。

所谓降级，就是一般是从整体符合考虑，就是当某个服务熔断之后，服务器将不再被调用，此刻客户端可以自己准备一个本地的fallback回调，返回一个缺省值，这样做，虽然服务水平下降，但好歹可用，比直接挂掉要强。

Hystrix实现原理：

https://www.jianshu.com/p/c8a998c9a571 “命令模式”与Hystrix。

命令模式：命令对象包含执行动作的所有信息。

四个实体：调用者、命令对象、接收者、客户端。

（1）调用者调用命令的指定方法；
（2）命令的执行方法被调用后，会真正执行接受者上的逻辑；
（3）客户端决定什么时候执行命令，传递命令给调用者。

命令模式解耦了请求者和实现者，请求者不需要关心实现者如何实现（面向扩展开发，面向修改封闭）。
命令模式的缺点是，每多一个命令，都要新建一个类。

对于Hystrix具体而言，调用某一服务时，我们实现HystrixCommand这一接口，即命令对象，将服务调用逻辑封装在其中，我们只需要关心调用命令即可，不需要关心Hystrix内部实现。

更多Hystrix原理：

https://blog.csdn.net/zl1zl2zl3/article/details/78840364 （Command 四种调用方式）

http://www.iocoder.cn/categories/Hystrix/ （全面）

http://www.imooc.com/article/76515 （讲了配置）

### Zuul
Zuul是Spring Cloud全家桶中的微服务API网关。网关负责对外聚合内部各微服务的API，屏蔽内部系统的变动，保持系统的稳定性。

另外，网关还承担负载均衡、统一鉴权、协议转换、监控检测等功能。

Zuul中有四类Filter，“pre”、“routing”、“post”、“error”，Zuul内部默认实现了一些filter，用户也可以自定义filter，比如鉴权。filter可以使用Groovy编写，达到动态加载的目的。（Zuul的1.0版本基于Servlet实现，2.0版本基于Netty实现）

https://blog.csdn.net/u011820505/article/details/79373594 （Zuul默认实现的filter）

![Zuul](https://github.com/CarrotWang/spring-cloud-demo/blob/master/imgs/zuul.png?raw=true "Zuul")

##### Zuul的高可用：
部署多台zuul服务，前端使用nginx等负载均衡组件。


### Feign
Feign封装了Http调用流程，使服务调用开发成本降低。

与Dubbo等RPC框架类似，Feign使用“动态代理”模式完成对服务接口的调用，并且封装了http报文到Request、Response对象的编解码逻辑。

![Feign](https://github.com/CarrotWang/spring-cloud-demo/blob/master/imgs/feign.png?raw=true "Feign")

### Sleuth



