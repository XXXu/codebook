# Hlog WALs和oldWALs
先介绍一下Hlog失效和Hlog删除的规则：
Hlog失效：写入数据一旦从MemStore中刷新到磁盘，Hlog（默认存储目录在/hbase/WALs下）就会自动把数据移动到/hbase/oldWALs目录下，此时并不会删除。
Hlog删除：Master启动时会启动一个线程，定期去检查oldWALs目录下的可删除文件进行删除，定期检查时间为hbase.master.cleaner.interval,默认时10分钟，删除条件有两个：
* Hlog文件在参与主从复制，否的话删除，是的话不删除。
> 所有在参与peer的数据都在zookeeper中/hbase/replication/rs目录下存储
> 比如在zk目录下有这么个节点： /hbase/replication/rs/jast.zh,16020,1576397142865/Indexer_account_indexer_prd/jast.zh%2C16020%2C1576397142865.jast.zh%2C16020%2C1576397142865.regiongroup-0.1579283025645
> 那么我们在oldWALs目录下是不会删除掉这个数据的：hdfs dfs -du -h /hbase/oldWALs/jast015.zh%2C16020%2C1576397142865.jast015.zh%2C16020%2C1576397142865.regiongroup-0.1579283025645
* Hlog文件是否在目录中存在hbase.master.logcleaner.ttl时间，如果是则删除。
# 整体流程
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200721110423.png)

# WALs split
Master启动时会启动SplitLogManager, 并进行log分割。那么什么是log split, 为什么要进行log split呢？

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201009170039.png)

HLog是每个Region server只有一份，一个region server上的所有region共用一个Hlog, hlog用来在系统异常down掉，MemStore中大量更新丢失时，
对数据进行恢复。从上图中可以看出，对每个region的更新在hlog里不是连续的，而是分散在Hlog里的。Hlog中的每项更新都会记录该更新所属的region, 
HBase要通过在每个region上应用hlog中的更新来恢复数据，因此需要把hlog中的更新按照region分组，这一把hlog中更新日志分组的过程就称为log split。

HMaster启动时会启动SplitLogManager，该类负责把log split任务通过创建znode发布到ZooKeeper上，并监控znode了解log split任务完成状态。
在每个region server中有单独的线程监控ZooKeeper中log split相关的znode, 一旦发现自己的任务，就开始进行log split, 最终负责log split实际工作的类是WALSplitter

## 过程：日志分割，分步执行
新目录按以下模式命名:
1. /hbase/WALs/<host>,<port>,<startcode>目录被重新命名。重命名该目录非常重要，因为即使HMaster认为它已关闭，RegionServer仍可能启动并接受请求，如果RegionServer
没有立即响应，也没有检测到它的Zookeeper会话，HMaster可能会将其解释为RegionServer失败。重命名日志目录可确保现有的有效WAL文件仍然由活动的regionserver使用，而不会意外写入。
命名：`/hbase/WALs/<host>,<port>,<startcode>-splitting`
可能的目录名字：`/hbase/WALs/srv.example.com,60020,1254173957298-splitting`

2. 每个日志文件都被拆分，每次一个，日志拆分器一次读取一个编辑项的日志文件，并将每个编辑条目放入对应于编辑区域的缓冲区中。同时拆分器启动多个编写器线程。编写器线程选取相应的缓冲区，
并将缓冲区中的编辑项写入临时恢复的编辑文件。临时编辑文件使用以下命名模式存储到磁盘：
`/hbase/<table_name>/<region_id>/recovered.edits/.temp`
该文件用于存储此区域的WAL日志中的所有编辑。日志拆分完成后，.temp文件将被重命名为写入文件的第一个日志的序列ID。要确定是否所有编辑都已写入，将序列ID与写入HFile的上次编辑的序列进行比较。
如果最后编辑的序列大于或等于文件名中包含的序列ID，则很明显，编辑文件中的所有写入操作都已完成。

3. 日志拆分完成后，每个受影响的region将分配给RegionServer。打开该region时，会检查recovereded文件夹以找到恢复的编辑文件。如果存在任何这样的文件，则通过读取编辑并将保存到MemStore来重播他们。
在重放所有编辑文件后，MemStore的内容被写入磁盘，编辑文件被删除。

## 处理日志分割期间的错误
如果您将该hbase.hlog.split.skip.errors选项设置为true,则错误处理如下：
* 拆分过程中遇到的任何错误都将被记录
* 有问题的WAL日志将被移到hbase rootdir下的.corrupt目录中
* WAL的处理将继续进行
如果该hbase.hlog.split.skip.errors选项设置为false默认值，则将传播该异常，并将该拆分记录为失败。

## 拆分崩溃的RegionServer的WAL时如何处理EOFException
如果在拆分日志时发生EOFException，即使hbase.hlog.split.skip.errors设置为false，拆分也会继续。在读取要拆分的文件集合中的最后一个日志时，可能会出现EXFException
，因为RegionServer可能在崩溃时写入记录的过程中。
### 在日志分割期间的性能改进
WAL日志拆分和恢复可能需要大量资源并需要很长时间，具体取决于崩溃中涉及的RegionServer的数量和区域的大小。启用或禁用分布式日志分割是为了提高日志分割期间的性能。
分布式日志处理自HBase 0.92开始默认启用。该设置由hbase.master.distributed.log.splitting属性控制，可以设置为true或false，但默认为true。
配置分布式日志拆分后，HMaster控制进程。HMaster在日志拆分过程中注册每个RegionServer，实际拆分日志的工作由RegionServers完成。分布式日志拆分中逐步描述的日志拆分的一般过程在这里仍然适用。

* 如果启用分布式日志处理，则HMaster会在集群启动时创建拆分日志管理器实例。
    1. 拆分日志管理器管理所有需要扫描和拆分的日志文件
    2. 拆分日志管理器将所有日志作为任务放入ZooKeeper splitWAL节点（ hbase/splitWAL）中
    3. 您可以通过发出以下zkCli命令来查看splitWAL的内容。
* 拆分日志管理器监视日志拆分任务和工作人员。拆分日志管理器负责以下正在进行的任务：
    1. 一旦拆分日志管理器将所有任务发布到splitWAL znode，它就会监视这些任务节点并等待它们被处理。
    2. 检查是否有任何排队等待的分组日志工人。如果发现没有响应的工作人员所要求的任务，它将重新提交这些任务。如果由于某些ZooKeeper异常而导致重新提交失败，则无法使用的工作者将再次排队等待重试
    3. 检查是否有未分配的任务。如果找到，它会创建一个短暂的重新扫描节点，以便通知每个拆分的日志工作者通过nodeChildrenChangedZooKeeper事件重新扫描未分配的任务。
    4. 检查已分配但过期的任务。如果发现任何东西，它们会再次返回到TASK_UNASSIGNED状态，以便它们可以重试。这些任务可能被分配给缓慢的工作人员，或者他们可能已经完成。这不是问题，因为日志拆分任务具有幂等性。换句话说，相同的日志拆分任务可以被处理多次而不会引起任何问题。
    5. 拆分日志管理器不断监视HBase拆分日志节点。如果任何拆分日志任务节点数据发生更改，拆分日志管理器将检索节点数据。节点数据包含任务的当前状态。您可以使用该zkCli get命令来检索任务的当前状态
* 每个RegionServer的拆分日志工作器执行日志拆分任务
每个RegionServer运行一个称为拆分日志工作器的守护进程线程，它负责拆分日志。守护程序线程在RegionServer启动时启动，并注册自己以观察HBase znode。
如果任何splitWAL znode子项发生更改，它会通知睡眠工作器线程唤醒并获取更多任务。如果工作人员当前任务的节点数据发生更改，则工作人员将检查该任务是否已由其他工作人员执行。
如果是这样，工作线程会停止当前任务的工作。工作人员不断监视splitWAL znode。出现新任务时，拆分日志工作人员将检索任务路径并检查每个任务路径，直到找到未声明的任务，
并尝试声明该任务。如果声明成功，它将尝试执行该任务并state根据拆分结果更新任务的属性。此时，拆分日志工作者会扫描另一个无人认领的任务。拆分日志工作者如何接近任务。
    1. 它查询任务状态，只在任务处于TASK_UNASSIGNED状态时采取行动
    2. 如果任务处于TASK_UNASSIGNED状态，则工作人员尝试TASK_OWNED自行设置状态。如果它没有设置状态，另一名工人将尝试抓住它。如果任务保持未分配，拆分日志管理器还会要求所有工作人员稍后重新扫描。
    3. 如果工作人员成功地完成任务，它会尝试再次获取任务状态，以确保它真正异步获取它。同时，它启动一个拆分任务执行器来完成实际工作：
        * 获取HBase根文件夹，在根目录下创建一个临时文件夹，并将日志文件拆分为临时文件夹
        * 如果拆分成功，任务执行程序将任务设置为状态TASK_DONE。
        * 如果工作人员捕获到意外的IOException，则该任务将设置为状态TASK_ERR。
        * 如果工作人员正在关闭，请将任务设置为状态TASK_RESIGNED。
        * 如果任务是由另一名工作人员完成的，则只需登录即可。
* 拆分日志管理器监视未完成的任务。拆分日志管理器在所有任务成功完成时返回。如果所有任务都完成并出现一些故障，则拆分日志管理器将引发异常，以便日志拆分可以重试。
由于异步实现，在极少数情况下，拆分日志管理器会丢失一些已完成任务的跟踪。因此，它定期检查其任务图或ZooKeeper中剩余的未完成任务。如果没有找到，它会抛出一个异常，
以便日志拆分可以马上重试，而不是挂在那里等待不会发生的事情。    