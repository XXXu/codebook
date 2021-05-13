# 基于ZK的HA切换原理
hdfs的active namenode、Standby namenode节点与ZK有什么关联呢？
> 当一个Namenode被成功切换为active状态时，它会在ZK内部创建一个临时的znode，在znode中保留当前active namenode的一些信息，比如主机名等等，当active namenode
> 出现失败或者连接超时的情况下，监控程序会将ZK上对应的临时znode节点删除，znode的删除事件会主动触发下一次的active namenode选举。

# HDFS HA自动切换机制的核心：ZKFC
FC是要和NN一一对应的，两个NN就要部署两个FC。它负责监控NN的状态，并及时的把状态信息写入ZK。
在ZKFC的进程内部，运行着3个对象服务：
* HealthMonitor：监控NameNode是否不可用或是进入了一个不健康的状态。
* ActiveStandbyElector：控制和监控ZK上的节点的状态。
* ZKFailoverController：协调HealMonitor和ActiveStandbyElector对象，处理它们发来的event变化事件，完成自动切换的过程。
三者关系图如下：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701154152.png)

# 线程模型
ZKFC的线程模型总体上来讲比较简单的，它主要包括三类线程，一是主线程；一是HealthMonitor线程; 一是zookeeper客户端的线程。它们的主要工作方式是：
* 主线程在启动所有的服务后就开始循环等待
* HealthMonitor是一个单独的线程，它定期向NN发包，检查NN的健康状况
* 当NN的状态发生变化时，HealthMonitor线程会回调ZKFailoverController注册进来的回调函数，通知ZKFailoverController NN的状态发生了变化
* ZKFailoverController收到通知后，会调用ActiveStandbyElector的API，来管理在zookeeper上的结点的状态
* ActiveStandbyElector会调用zookeeper客户端API监控zookeeper上结点的状态，发生变化时，回调ZKFailoverController的回调函数，通知ZKFailoverController
，做出相应的变化