## Namenode
HDFS metadata主要存储两种类型的文件:
* fsimage:记录某一永久性检查点（Checkpoint）时整个HDFS的元信息
* edits:所有对HDFS的写操作都会记录在此文件中
Checkpoint介绍:
HDFS会定期（dfs.namenode.checkpoint.period，默认3600秒）的对最近的fsimage和一批新edits文件进行Checkpoint（也可以手工命令方式），Checkpoint发生后会将前一次Checkpoint后的所有edits文件合并到新的fsimage中，HDFS会保存最近两次checkpoint的fsimage。Namenode启动时会把最新的fsimage加载到内存中。
下面是一个标准的dfs.namenode.name.dir目录结构：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701160647.png)
* VERSION
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701160827.png)
    * namespaceID/clusterID/blockpoolID - 这三个ID在整个HDFS集群全局唯一，作用是引导Datanode加入同一个集群。在HDFS Federation机制下，会有多个Namenode，所以不同Namenode直接namespaceID是不同的，分别管理一组blockpoolID，但是整个集群中，clusterID是唯一的，每次format namenode会生成一个新的，也可以使用-clusterid手工指定ID
    * storageType - 有两种取值NAME_NODE /JOURNAL_NODE，对于JournalNode的参数dfs.journalnode.edits.dir，其下的VERSION文件显示的是JOURNAL_NODE
    * cTime - HDFS创建时间，在升级后会更新该值
    * layoutVersion - HDFS metadata版本号，通常只有HDFS增加新特性时才会更新这个版本号
* edits_start transaction ID-end transaction ID
finalized edit log segments，在HA环境中，Standby Namenode只能读取finalized log segments
* edits_inprogress__start transaction ID
当前正在被追加的edit log，HDFS默认会为该文件提前申请1MB空间以提升性能
* fsimage_end transaction ID
每次checkpoint（合并所有edits到一个fsimage的过程）产生的最终的fsimage，同时会生成一个.md5的文件用来对文件做完整性校验
* seen_txid
保存最近一次fsimage或者edits_inprogress的transaction ID。需要注意的是，这并不是Namenode当前最新的transaction ID，该文件只有在checkpoing(merge of edits into a fsimage)或者edit log roll(finalization of current edits_inprogress and creation of a new one)时才会被更新。
这个文件的目的在于判断在Namenode启动过程中是否有丢失的edits，由于edits和fsimage可以配置在不同目录，如果edits目录被意外删除了，最近一次checkpoint后的所有edits也就丢失了，导致Namenode
状态并不是最新的，为了防止这种情况发生，Namenode启动时会检查seen_txid，如果无法加载到最新的transactions，Namenode进程将不会完成启动以保护数据一致性。
* in_use.lock
防止一台机器同时启动多个Namenode进程导致目录数据不一致：进程号@主机名
## Datanode
Datanode主要存储数据，下面是一个标准的dfs.datanode.data.dir目录结构：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701161839.png)
* BP-random integer-NameNode-IP address-creation time
BP代表BlockPool的意思，就是上面Namenode的VERSION中的集群唯一blockpoolID，如果是Federation HDFS，则该目录下有两个BP开头的目录，IP部分和时间戳代表创建该BP的NameNode的IP地址和创建时间戳
* VERSION 
与Namenode类似，其中storageType是DATA_NODE
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701162207.png)
* finalized/rbw目录
这两个目录都是用于实际存储HDFS BLOCK的数据，里面包含许多block_xx文件以及相应的.meta文件，.meta文件包含了checksum信息。
rbw是“replica being written”的意思，该目录用于存储用户当前正在写入的数据。