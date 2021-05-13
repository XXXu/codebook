# dstool运维工具
当HBase出现异常或要执行特殊功能时可使用DSTools，在DSTools包含runDSTools、runHFileCheck 和 mergeRegion 三个工具
## 使用工具的前提：
* 确保HDFS服务正常，可以通过HDFS相关命令进行状态检查（hdfs fsck /）
* 如果HBase集群本身起动不了，则保证所有HBase所有角色服务都关闭。 
* 如果HBase集群已经启动正在运行，则保证所有regionserver都是在线存活状态，并且至少有两个及以上的master角色在线存活状态。
### runDSTools：
可修复常见情况，如HBase集群因meta表损坏无法启动或Region长时间处于 Transition 阶段。
#### 使用步骤：
* 在工具所在的目录下，直接sh runDSTools.sh [table_name] 运行即可；指定table_name选项可以针对特定的表进行修复。
当用户要修整个集群或者一张大表时，推荐 nohup sh runDSTools.sh [table_name] & 后台运行。 注意：一个集群只能一个DSTool工具实例在跑，否则会出现hdfs目录操作失败类似问题
* 查看工具运行输出的日志信息 vim /tmp/dstool.log.1,查看最后的输出日志提示。如果运行时，集群是在线模式，正确的日志应该显示 inconsistance 为0。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201013113419.png)

#### 分析runDSTools:
##### 在线fix步骤：
1. make sure all lingering reference store files if offline
2. make sure all orphans file is ok . such as .regioninfo/.tableinfo/version three kinds of files 
3. make sure all overlaps is fixed
4. make sure all holes is fixed
5. make sure all META/deploy is fixed {TODO restart active master }

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201013174034.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201013175207.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014153533.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014154533.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014154724.png)

>在线fix总结：主要针对region没上线，如果meta信息在zk上不一致那么建议离线fix

##### 离线fix步骤：
means that cluster can not be startup by shell/transwarp web
this kind most caused by invalid META info/zk info 
the useful way is to delete META/zk infos , restart cluster and fix all regions online again 
new steps are:
1. backup META/namespace table,move out hbase:meta,namespace
2. offline hdfs fix, like online fix step 1/2/3/4

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014153533.png)

3. delete zk info , diretories are follow
   /hyperbase1/table    
   /hyperbase1/table-lock
   /hyperbase1/namespace
   /hyperbase1/region-in-transition

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014152211.png)

4. rebuild meta 

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201014151923.png)

5. start cluster 

> 离线fix总结：离线修复的话，主要针对meta表和zk上的信息不一致，首先把zk的信息给清掉，然后把meta表重建，重启hbase后，zk上的信息重新写入，这个时候信息就一致了。

### runHFileCheck：
检查HFile文件
#### 使用步骤：
* 先运行一次命令 sh runHFileCheck.sh tableName 1>&2 2> /tmp/hfilecheck.log.1 检查看是否有 hfile有损坏或无效引用存在, 此信息会在工具运行的日志中输出：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20201013114839.png)

* 如果有损坏，运行命令进行修复sh runHFileCheck.sh -fixReferenceFiles –sidelineCorruptHFiles tableName 1>&2 2> /tmp/hfilecheck.log.2 
* 再次运行命令验证是否已经修复 sh runHFileCheck.sh tableName 1>&2 2> /tmp/hfilecheck.log.3

### mergeRegion：
合并region
#### 使用步骤：
直接运行sh mergeRegion.sh activemasterHost tableName 
通过wget命令得到tablename的region信息，所以wget命令得有，然后通过merge_region命令两两合并region，本身很简单。需要考虑两个region本来就很大了或者两个region很小的情况，可以更人性化一点。



