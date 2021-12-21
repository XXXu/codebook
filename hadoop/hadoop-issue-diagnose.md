# hadoop 问题诊断
## hdfs问题
### 服务启动问题
#### namenode启动问题
* Unable to trigger a roll of the active NN,两个namenode启动后都是standby状态无法变成active
>重新格式化zkfc：hdfs zkfc -formatZK

* 两个namenode频繁切换
>确认集群的大小，有多少元数据，有多少datanode，如果数据比较多，那可能是namenode处理datanode block report处理不过来，可以打namenode jstack来确认，如果确认的话，
>那么需要修改dfs.namenode.handler.count参数，改成200来增加线程数，dfs.blockreport.split.threshold此参数默认为1000000，减少此值也能分担namenode处理的压力。
>如果还是处理不了，那么就需要开启servicerpc，把8020端口分开，新加一个8021端口，[servicerpc配置手册](https://wiki.transwarp.io/pages/viewpage.action?pageId=24907772)

* 迁移nn节点报错无法格式化zkfc
>迁移过程中出了问题，需要手动格式化zkfc，开了安全后需要手动export HADOOP_OPTS="-Djava.security.auth.login.config=/etc/hdfs1/conf/jaas.conf"，然后hdfs zkfc -formatZK

* namenode报错：java.io.IOException: NameNode is not formatted.
>一般是安装hdfs后启动namenode会发生此类报错，有残留文件没有被删掉/var/namenode_format/is_formated.identifier，导致format namenode失败，删掉此文件后，再重新安装即可。

#### datanode启动问题
* java.io.FileNotFoundException: /vdir/mnt/disk1/hadoop/data/in_use.lock (No such file or directory)
>这种异常信息一般事先操作过数据盘导致的，先看看盘符挂载对不对，可以findmnt查看，如果解决不了，请oncall。

* Reporting bad BP*:blk* with volume /vdir/mnt/disk*/hadoop/data Input/output error
>说明磁盘有问题，把有问题的磁盘踢掉重启dn即可

* Cannot register datanode: 0.0.0.0:50010,because current license expired time is xxx
>license 过期，直接申请license换上即可

* Initialization failed for Block pool <registering>(Datanode Uuid unassigned) serivce to <hostname>/ip:8020
 DiskChecker$DiskErrorException: Invalid volume failure config value: 2 
 >dfs.datanode.failed.volumes.tolerated 此参数值是容忍几块盘失败，如果现场只有两个盘，这个值也是2的话，那么datanode就会起不来，这个参数默认值是0

#### journalnode启动问题

### 服务性能问题

* hdfs写数据慢
>egrep -o "Slow.*?(took|cost)" hadoop-hdfs-datanode*.log | sort | uniq -c 用此命令查看datanode的日志，如果出现大量slow信息：Slow
> BlockReceiver write data to disk cost(packet to mirror took)，那就说明一定是磁盘或者网络慢。

### 数据问题
* 挂载一块新的磁盘，数据却写不进去
>通过命令findmnt | grep "disk*"查看新添加的磁盘有没有挂载好，datanode日志可以查看有没有识别到磁盘，大概率是挂载链出问题，参考：[挂载链断裂问题排查方案](https://wiki.transwarp.io/pages/viewpage.action?pageId=23471581)

* 磁盘数据分布不均匀
>1.dfs.datanode.fsdataset.volume.choosing.policy=AvailableSpaceVolumeChoosingPolicy DataNode在选择磁盘存储数据时，会选择可用磁盘空间最大的磁盘来存储数据，这样保证了小盘不会存储过多的数据
>2.dfs.datanode.available-space-volume-choosing-policy.balanced-space-threshold=10737418240（10GB）该参数配置是首先计算出两个值，一个是所有磁盘中最大可用空间，另外一个值是所有磁盘中最小可用空间，如果这两个值相差小于该配置项指定的阀值时，则就用轮询方式的磁盘选择策略选择磁盘存储数据副本
>3.dfs.datanode.available-space-volume-choosing-policy.balanced-space-preference-fraction=0.75 该参数配置是指有多少比例的数据副本应该存储到剩余空间足够多的磁盘上。该配置项取值范围是0.0-1.0，一般取0.5-1.0，如果配置太小，会导致剩余空间足够的磁盘实际上没分配足够的数据副本，而剩余空间不足的磁盘取需要存储更多的数据副本，导致磁盘数据存储不均衡
>如果参数设置为1.0，那么就不应该向空间不足的磁盘继续写数据了,从代码中看，并不是绝对的，即使设置为1也是可能向空间不足的磁盘继续写数据的。**引申**：dfs.datanode.du.reserved和格式化磁盘的保留空间（默认5
>%），如果此参数设置比系统保留小的话磁盘还是有可能被写满的，需要注意。
### 命令问题

**hdfs fsck filepath**

检查某个文件时发现block replication为0，那说明这个文件肯定有问题，这个时候就需要打开客户端的debug日志来查看这个文件block状态，block状态共有四种：
1. UnderConstruction:UnderConstruction状态指的是一个block块处于正在被写入的状态。
2. UnderRecovery:从字面意思上我们也可以理解出，这是正在被恢复的块。
3. Committed：Committed状态的块指的是一个块已经确定好它的字节大小与generation stamp值(可理解为版本号)，但是这2个值与当前块的副本块并不匹配。
4. Complete：当有一个块副本的字节大小值、GS值与当前块匹配，则当前块被认为是Complete状态。如果一个文件的所有块都处于Complete状态，则此文件被认为写入操作结束，可以进行关闭操作了。

如果UnderConstruction=true状态，那说明这个文件虽然创建了，但是block正在写入就断开了，block也不可用了。

**hadoop distcp**

disctp问题建议先看一下wiki：[distcp](https://wiki.transwarp.io/pages/viewpage.action?pageId=22677475)
* 使用distcp传输某个文件到另一个集群时出现could not obatain the last block location异常时
>排查思路先用fsck 查看这个文件block状态，接下来如果没有思路，请oncall。

* 出现异常DIGEST-MD5: No common protection layer between client and server
>客户端和另一个集群的服务端参数dfs.data.transfer.protection值不一样，请配置一样即可。

**hdfs dfs -put**

* 集群外使用hdfs dfs -put命令发现传输慢。
>1.集群外使用-put命令慢不慢 2.在集群外使用scp命令慢不慢 3.ping命令有没有丢包 4.网卡是多少兆，网络带宽有没有打满 5.请oncall


### 漏洞问题
请先查看wiki：[漏洞相关](https://wiki.transwarp.io/pages/viewpage.action?pageId=24600475)


## yarn问题
### 服务启动问题
#### resourcemanager启动问题
**javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated**
>请直接oncall guardian同事

#### nodemanager启动问题
**Caused by: java.io.IOException: Login failure for yarn/tos_5tw631u@5TW631U.TDH from keytab /etc/keytabs/keytab: javax.security.auth.login.LoginException: Client not found in Kerberos database (6) - Client not found in Kerberos database**
>请直接oncall guardian同事

**Cannot run program "/usr/lib/hadoop-yarn/bin/container-executor": error=13,Permission denied**
>遇到permission denied一定是目录权限不对，请参考正常的节点目录权限修改

## zookeeper问题

### 服务启动问题
* 启动报错信息：invalid snapshot /var/zookeeper1/version-2/xxxx
> 通过df查看磁盘目录，发现跟目录满了，清理根目录冗余数据，释放空间。

* java.net.BindException: Address already in use
> 端口被占用，使用命令netstat -antp | grep <port>查看什么进程占用此端口，如果是客户业务进程直接询问客户这个进程是干嘛的，如果是tdh的进程直接oncall让对应的研发介入，不能随便kill此进程。
### zookeeper未授权访问漏洞
[开启安全的ZooKeeper允许未认证连接漏洞修复方案](https://wiki.transwarp.io/pages/viewpage.action?pageId=20261612)

