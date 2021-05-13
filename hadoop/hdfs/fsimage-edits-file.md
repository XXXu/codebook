## 概念：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200701143738.png)
* Fsimage文件：HDFS文件系统元数据的一个永久性的检查点，包含hdfs文件系统的所有目录和文件inode的序列化信息。
* Edits文件：存放HDFS所有的更新操作，hdfs客户端执行的所有写操作首先会被记录到Edits文件中。
* seen_txid文件：保存的是一个数字，就是最后一个edits的数字。
> 每次Namenode启动的时候都会将fsimage文件读入内存，加载Edits文件里面的更新操作，保证内存中的元数据信息是最新的，可以看成namenode启动的时候就就将fsimage和edit文件进行了合并。

## 查看Fsimage
hdfs oiv -p 文件类型 -i 输入路径 -o 转换后文件输出路径 
例子：hdfs oiv -p XML -i fsimage_0000000000000364036 -o /root/fsimage.xml 
> 查看fsimage.xml文件可以看到并没有对应的dn节点，在fsimage中，并没有记录每一个block对应到哪几个datanodes的对应表信息，而只是存储了所有的关于namespace的相关信息。而真正每个block对应到datanodes列表的信息在hadoop中并没有进行持久化存储，而是在所有datanode启动时，每个datanode对本地磁盘进行扫描，将本datanode上保存的block信息汇报给namenode，namenode在接收到每个datanode的块信息汇报后，将接收到的块信息，以及其所在的datanode信息等保存在内存中。HDFS就是通过这种块信息汇报的方式来完成 block -> datanodes list的对应表构建。Datanode向namenode汇报块信息的过程叫做blockReport，而namenode将block -> datanodes list的对应表信息保存在一个叫BlocksMap的数据结构中。

## 查看edits文件
hdfs oev -p 文件类型 -i 输入路径 -o 转换后文件输出路径

> Namenode如何确定下次开机启动的时候合并哪些edits，NameNode启动的时候合并的是上次停机前正在写入的Edits，即edits_inprogress_xxx。
