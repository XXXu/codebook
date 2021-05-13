## 基本架构
### 组成
YARN 主要由 ResourceManager、NodeManager、ApplicationMaster 和 Container 等组件构成
### ResourceManager作用
* 处理客户端请求
* 监控NodeManager
* 启动或监控ApplicationMaster
* 资源的分配与调度
### NodeManager作用
* 管理单个节点上的资源
* 处理来自ResourceManager的命令
* 处理来自ApplicationMaster的命令
### ApplicationMaster 作用
* 负责数据的切分
* 为应用程序申请资源并分配给内部的任务
* 任务的监控与容错
### Container 作用
Container是YARN中的资源抽象，它封装了某个节点上的多维度资源，如内存、CPU、磁盘、网络等

## 工作机制
* Mr 程序提交到客户端所在的节点（生成job.split、job.xml...等任务规划配置文件）
* Yarnrunner 根据job运行所需资源向Resourcemanager 申请Application，RM将该应用程序的资源路径返回给 yarnrunner，将运行所需资源提交到 HDFS 上之后申请开启App。
* RM 将用户的请求初始化成一个 task，其中一个 NodeManager 领取到 task 任务， 创建容器 Container，并产生 Appmaster。
* Container 从 HDFS 上拷贝任务规划配置文件以及资源到本地，Appmaster 向 RM 申请运行 maptask 资源，RM 将运行 maptask 任务分配给另外两个 NodeManager，另两个 NodeManager 分别领取任务并创建容器
* AppMaster 向两个接收到任务的 NodeManager 发送程序启动脚本，这两个 NodeManager分别启动 maptask 对数据分区排序
* AppMaster 等待所有 maptask 运行完毕后，向 RM 申请容器，运行 reduce task
* reduce task 向 maptask 获取相应分区的数据。程序运行完毕后，AppMaster 会向 RM 申请注销自己
