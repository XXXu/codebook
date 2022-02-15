# 让你彻底搞明白YARN资源分配

## 本篇要解决的问题：
* Container是以什么形式运行的？是单独的JVM进程吗？
* YARN的vcore和本机的CPU核数关系？
* 每个Container能够使用的物理内存和虚拟内存是多少？
* 一个NodeManager可以分配多少个Container？
* 一个Container可以分配的最小内存是多少？最大内存内存是多少？以及最小、最大的VCore是多少？
* 为什么use vcore会超过total vcore？

## YARN资源管理简述

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220215115843.png)

任务提交流程：
* 要将应用程序（MapReduce/Spark/Flink）程序运行在YARN集群上，先得有一个用于将任务提交到作业的客户端，也就是client。它向Resource Manager（RM）发起请求，RM会为提交的作业生成一个JOB ID。此时，JOB的状态是：NEW
* 客户端继续将JOB的详细信息提交给RM，RM将作业的详细信息保存。此时，JOB的状态是：SUBMIT
* RM继续将作业信息提交给scheduler（调度器），调度器会检查client的权限，并检查要运行Application Master（AM）对应的queue（默认：default queue）是否有足够的资源。此时，JOB的状态是ACCEPT。
* 接下来RM开始为要运行AM的Container资源，并在Container上启动AM。此时，JOB的状态是RUNNING
* AM启动成功后，开始与RM协调，并向RM申请要运行程序的资源，并定期检查状态。
* 如果JOB按照预期完成。此时，JOB的状态为FINISHED。如果运行过程中出现故障，此时，JOB的状态为FAILED。如果客户端主动kill掉作业，此时，JOB的状态为KILLED。

## YARN集群资源管理
### 集群总计资源
要想知道YARN集群上一共有多少资源很容易，我们通过YARN的web ui就可以直接查看到。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220215115952.png)

通过查看Cluster Metrics，可以看到总共的内存为24GB、虚拟CPU核为24个。我们也可以看到每个NodeManager的资源。很明显，YARN集群中总共能使用的内存就是每个NodeManager的可用内存加载一起，VCORE也是一样。

### NodeManager总计资源
NodeManager的可用内存、可用CPU分别是8G、和8Core。这个资源和Linux系统是不一致的，这个是虚拟的。NodeManager的可用内存和操作系统总计内存是没有直接关系的！
参数：
* yarn.nodemanager.resource.memory-mb 默认值8G
* yarn.nodemanager.vmem-pmem-ratio 默认值2.1，这个配置是针对NodeManager上的container，如果说某个Container的物理内存不足时，可以使用虚拟内存，能够使用的虚拟内存默认为物理内存的2.1倍。
* yarn.nodemanager.resource.cpu-vcores 默认值8

### scheduler调度资源
通过YARN的webui，点击scheduler，我们可以看到的调度策略、最小和最大资源分配。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220215120029.png)

通过web ui，我们可以看到当前YARN的调度策略为容量调度。调度资源的单位是基于MB的内存、和Vcore（虚拟CPU核）。最小的一次资源分配是：1024M（1G）和1个VCORE。最大的一次分配是：4096M（4G）和4个VCORE。注意：内存资源和VCORE都是以Container承载的。

* yarn.scheduler.minimum-allocation-mb 1024 该配置表示每个容器的最小分配。因为RM是使用scheduler来进行资源调度的，如果请求的资源小于1G，也会设置为1G。这表示，如果我们请求一个256M的container，也会分配1G。
* yarn.scheduler.maximum-allocation-mb 8192 最大分配的内存，如果比这个内存高，就会抛出InvalidResourceRequestException异常。这里也就意味着，最大请求的内存不要超过8G。上述截图显示是4G，是因为我在yarn-site.xml中配置了最大分配4G。
* yarn.scheduler.minimum-allocation-vcores 1 同内存的最小分配
* yarn.scheduler.maximum-allocation-vcores 4 同内存的最大分配 

### Container总计资源
在YARN中，资源都是通过Container来进行调度的，程序也是运行在Container中。Container能够使用的最大资源，是由scheduler决定的。如果按照Hadoop默认配置，一个container最多能够申请8G的内存、4个虚拟核。例如：我们请求一个Container，内存为3G、VCORE为2，是OK的。考虑一个问题：如果当前NM机器上剩余可用内存不到3G，怎么办？此时，就会使用虚拟内存。不过，虚拟内存，最多为内存的2.1倍，如果物理内存 + 虚拟内存仍然不足3G，将会给container分配资源失败。
根据上述分析，如果我们申请的container内存为1G、1个VCORE。那么NodeManager最多可以运行8个Container。如果我们申请的container内存为4G、4个vcore，那么NodeManager最多可以运行2个Container。

### Container是一个JVM进程吗
当向RM请求资源后，会在NodeManager上创建Container。问题是：Container是不是有自己独立运行的JVM进程呢？还是说，NodeManager上可以运行多个Container？Container和JVM的关系是什么？
明确一下，每一个Container就是一个独立的JVM实例。可以到nodemanager节点上打jps可以看到YarnChild进程。

## 总结
* Container是以什么形式运行的？是单独的JVM进程吗？
> 是的，每一个Container就是一个单独的JVM进程。

* YARN的vcore和本机的CPU核数关系？
> 没关系。默认都是手动在yarn-default.xml中配置的，默认每个NodeManager是8个vcore，所有的NodeManager上的vcore加在一起就是整个YARN所有的vcore。

* 每个Container能够使用的物理内存和虚拟内存是多少？
> scheduler分配给container多少内存就是最大能够使用的物理内存，但如果超出该物理内存，可以使用虚拟内存。虚拟内存默认是物理内存的2.1倍。

* 一个Container可以分配的最小内存是多少？最大内存内存是多少？以及最小、最大的VCore是多少？
> 根据scheduler分配的最小/最大内存、最小/最大vcore来定。

* 一个NodeManager可以分配多少个Container？
> 这个问题得和最后一个问题连起来看，想当然的看Container的内存大小和vcore数量，用NM上最大的可用Mem和Vcore相除就知道了。

* 为什么use vcore会超过total vcore？
> 参数:yarn.scheduler.capacity.resource-calculator 默认值为 org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator 这个默认计算container的时候只考虑memory，不考虑vcore。如果nodemanager节点内存很多，那么就会一直分配，加上vcore和实际cpu之间没有关系，所以就会导致use vcore会超过total vcore。需要把默认值修改为DominantResourceCalculator，计算container的时候才会把memory和vcore都考虑进去。
