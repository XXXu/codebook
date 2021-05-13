# 说明
## 在manager页面配置
## servicerpc说明
* servicerpc配置启用之前，所有其他节点与namenode的通信都通过rpc-address对应的端口。
* 启用之后，NN和NN之间，DN和NN之间以及zkfc和NN之间会通过servicerpc-address配置的端口进行通信，客户端和NN之间仍旧通过rpc-address通信。
* dfs.namenode.service.handler.count 对应的是servicerpc的处理线程数量配置项，默认值为10，如有需要可以根据实际使用情况配置。
## nn重启过程：
* 首先对StandbyNN进行重启；然后关闭ActiveNN，随即对StandbyNN进行ZKFC格式化，成为新的ActiveNN；最后启动原ActiveNN。
* 经测试服务不可用的时间是从StandbyNN重启后，ActiveNN关闭开始，一直到新的原StandbyNN完成zkfc格式化，升级为新的ActiveNN结束

# 步骤
## 检查service-rpc未使用
检查文件/etc/hdfs1/conf/hdfs-site.xml中是否有dfs.namenode.servicerpc-address配置项，若没有，则未使用
## 找一个未被使用的端口，如8021
注意，如果有防火墙等安全相关配置，需要开放该端口，具体权限和8020相同。
## 新增配置项dfs.namenode.servicerpc-address，参考配置项dfs.namenode.rpc-address
```
dfs.namenode.servicerpc-address.nameservice1.nn1

tw-node07:8021

dfs.namenode.servicerpc-address.nameservice1.nn2

tw-node08:8021
```
## 重启Namenode流程
* 首先重启StandbyNN
* 关闭ActiveNN
* 进入StandbyNN，格式化ZKFC。原先的StandbyNN变成ActiveNN
    * 选择Standby Namenode的NAMENODE进程的容器
    * hdfs zkfc -formatZK
* 再重启原本的ActiveNN

## 检查namenode的状态，为一个Active，一个Standby，正常
## 重启集群内的datanode结点，重启后连接到新的端口8021上面（若不重启，仍然连接8020端口）
重启后使用netstat -anp 分别检查8020端口和8021端口的连接情况

# 开启guardian的情况
执行hafs zkfc -formatZK命令的时候，需要导入认证配置:
export HADOOP_OPTS="-Djava.security.auth.login.config=/etc/hdfs1/conf/jaas.conf"
否则会出现AuthFailedException异常信息，如下:
```
2019-10-18 10:19:11,748 INFO zookeeper.ClientCnxn: Socket connection established to tw-node07/172.22.0.207:2181, initiating session
2019-10-18 10:19:11,755 INFO zookeeper.ClientCnxn: Session establishment complete on server tw-node07/172.22.0.207:2181, sessionid = 0x8d6dc80e52dc004d, negotiated timeout = 18000
2019-10-18 10:19:11,758 INFO ha.ActiveStandbyElector: Session connected.
2019-10-18 10:19:11,762 INFO zookeeper.ClientCnxn: Unable to read additional data from server sessionid 0x8d6dc80e52dc004d, likely server has closed socket, closing socket connection and attempting reconnect
2019-10-18 10:19:11,864 INFO zookeeper.ZooKeeper: Session: 0x8d6dc80e52dc004d closed
2019-10-18 10:19:11,864 WARN ha.ActiveStandbyElector: Ignoring stale result from old client with sessionId 0x8d6dc80e52dc004d
2019-10-18 10:19:11,864 INFO zookeeper.ClientCnxn: EventThread shut down
2019-10-18 10:19:11,864 FATAL tools.DFSZKFailoverController: Got a fatal error, exiting now
java.io.IOException: Couldn't determine existence of znode '/hdfs1-ha/nameservice1'
......
...
Caused by: org.apache.zookeeper.KeeperException$AuthFailedException: KeeperErrorCode = AuthFailed for /hdfs1-ha/nameservice1
```
    