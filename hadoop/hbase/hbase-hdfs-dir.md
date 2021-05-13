Hbase在hdfs上的存储位置，根目录是由配置项hbase.rootdir决定，默认就是"/hbase"
* /hbase/WALs
在该目录下，对于每个RegionServer，都会对应1~n个子目录
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701170908.png)

* /hbase/oldWALs
当/hbase/WALs中的HLog文件被持久化到存储文件时，它们就会被移动到/hbase/oldWALs

* /hbase/hbase.id
集群的唯一ID
* /hbase/hbase.version
集群的文件格式版本信息

* /hbase/corrupt
损坏的日志文件，一般为空

* /hbase/archive
存储表的归档和快照，HBase 在做 split或者 compact 操作完成之后，会将 HFile 移到archive 目录中，然后将之前的 HFile 删除掉，该目录由 HMaster 上的一个定时任务定期去清理。

* /hbase/.tmp
当对表做创建或者删除操作的时候，会将表move 到该 tmp 目录下，然后再去做处理操作.

* /hbase/data
hbase存储数据的核心目录

    * /hbase/data/hbase
    该目录存储了存储了 HBase 的 namespace、meta 和snapshot 三个系统级表
    * /hbase/data/库名
        * /hbase/data/库名/表名/.tabledesc
        表的元数据信息
        * /hbase/data/库名/表名/.tmp
        中间临时数据，当.tableinfo被更新时该目录就会被用到
        * /hbase/data/库名/表名/01a10fbfc443c8a91766ccea497ce4ee
        是由region的表名+Start Key+时间戳产生的HashCode
        * /hbase/data/库名/表名/region名/.regioninfo
        包含了对应region的HRegionInfo的序列化信息，类似.tableinfo。hbase hbck 工具可以用它来生成丢失的表条目元数据
        * /hbase/data/库名/表名/region名/列族名
        每个列族的所有实际数据文件
        * /hbase/data/库名/表名/region名/列族名/文件名
        hbase实际数据文件
        * /hbase/data/库名/表名/region名/.tmp
        存储临时文件，比如某个合并产生的重新写回的文件(compaction操作时合并文件写到.tmp下)