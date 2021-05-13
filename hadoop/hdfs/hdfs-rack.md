# HDFS--机架感知
Hadoop的设计考虑：设计分布式的存储和计算解决方案架构在廉价的集群之上，所以，服务器节点出现宕机的情况是常态。数据的安全是重要考虑点。HDFS的核心设计思路就是对用户存进HDFS里的所有数据都做冗余备份，以此保证数据的安全
那么Hadoop在设计时考虑到数据的安全，数据文件默认在HDFS上存放三份。显然，这三份副本肯定不能存储在同一个服务器节点。那怎么样的存储策略能保证数据既安全也能保证数据的存取高效呢？
HDFS分布式文件系统的内部有一个副本存放策略：以默认的副本数=3为例：
* 第一个副本块存本机
* 第二个副本块存跟本机同机架内的其他服务器节点
* 第三个副本块存不同机架的一个服务器节点上

HDFS为了降低整体的网络带宽消耗和数据读取延时，HDFS集群一定会让客户端尽量去读取近的副本，那么按照以上头解释的副本存放策略的结果：
* 如果在本机有数据，那么直接读取
* 如果在跟本机同机架的服务器节点中有该数据块，则直接读取
* 如果该HDFS集群跨多个数据中心，那么客户端也一定会优先读取本数据中心的数据

默认情况下没有启动机架感知，需要在core-site.xml加一个配置项：net.topology.script.file.name
脚本内容，仅供参考：
```
#!/bin/sh

RACK_DATA=/etc/transwarp/conf/topology.data

while [ $# -gt 0 ] ; do
  nodeArg=$1
  result=""
  if [ -f $RACK_DATA ]; then
    exec< ${RACK_DATA}
    while read line ; do
      ar=( $line )
      if [ "${ar[0]}" = "$nodeArg" ] ; then
        result="${ar[1]}"
      fi
    done
  fi

  if [ -z "$result" ] ; then
    echo -n "/default "
  else
    echo -n "$result "
  fi
  shift

```
topology.data里面就是ip对应的机架，最好IP和主机名都配上。
hdfs  dfsadmin  -printTopology：此命令可以查看整个集群的拓扑。

