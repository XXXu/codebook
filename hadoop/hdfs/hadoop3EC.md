# Hadoop 3.0 EC技术
## EC的设计目标
* Hadoop默认的3副本方案需要额外的200%的存储空间、和网络IO开销
* 而一些较低I/O的warn和cold数据，副本数据的访问是比较少的（hot数据副本会被用于计算）
* EC可以提供同级别的容错能力，存储空间要少得多（官方宣传不到50%），使用了EC，副本始终为1

## EC在Hadoop架构的调整
使用EC有几个重要优势:1.Online-EC，在写入数据的时候就是以EC方式写入的，而不是先存完数据再开始进行EC编码处理（offline-EC）。2.Online-EC将一个小文件分发到多个DataNode，而不是将多个文件放到一个编码组中。这样，删除数据、Qutoa、数据迁移是更容易的。
### NameNode元数据存储
基于EC的文件存储与Hadoop经典分块存储方式做了调整。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220111154147.png)

基于条纹的HDFS存储逻辑上是由Block Group（块组组成），每个Block Group包含了一定数量的Internal Block（后续我们称为EC Block）。如果一个文件有很多的EC Block，会占用NameNode较大的内存空间。HDFS引入了新的分层Block命名协议，通过Block的ID可以推断出Block Group的ID，NameNode是基于Block Group而不是EC Block级别管理。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220111154311.png)

### Client
客户端读取、写入HDFS也做了调整，当以Online-EC写入一个文件时，是以并行方式来处理Block Group中的Internal Block。
* 当写入文件时，数据流通过DFSStripedOutputStream实现，会管理一组数据流，每个DataNode节点都对应一个数据流，并将Block Group的一个Internal Block存储。这些流操作都是异步进行的。还有一个Coordinator，负责一个文件的所有Block Group操作，包括：结束当前的Block Group、分配新的Block Group等。
* 当读取文件时，数据流通过DFSStripedInputStream实现，它将请求的文件转换为存储在DataNode的Internal Block，然后进行并行读取，出现故障时，发出奇偶校验数据请求来进行解码恢复Internal Block。

### Datanode
DataNode上运行一个ErasureCodingWorker（ECWorker）任务，专门用于失败的EC Block进行后台数据恢复。一旦NameNode检测到失败的EC Block，NameNode会选择一个DataNode进行数据恢复。
* 先从错误数据所在的Block Group数据节点中读取正常的EC Block作为输入，基于EC策略，通过最小的EC Block进行数据恢复。
* 从输入的EC Block以及奇偶校验码块进行解码数据恢复，生成正常的EC Block。
* EC解码完成后，将恢复的EC Block传输到对应的DataNode中。

## EC存储方案
### EC编码和解码
EC编解码器是对EC Block上的条纹单元进行处理。编码器将EC Block中的多个条纹单元作为输入，并输出许多奇偶校验单元。这个过程称为编码。条纹单元和奇偶校验单元称为EC编码组。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220111154605.png)

解码的过程就是恢复数据的过程，可以通过剩余的条纹单元和奇偶校验单元来恢复数据。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20220111154644.png)

### EC策略关键属性
为了适应不同的业务需求，在HDFS中可以针对文件、目录配置不同的副本和EC策略。EC策略实现了如何对文件进行编码/解码方式。每个策略包含以下属性：
* EC schema：EC schema包含了EC Group中的EC Block数量以及奇偶校验Block的数量，例如：6+3，以及编解码器算法，例如：Reed-Solomon（索罗蒙算法）、XOR（异或算法）。
* 条纹单元大小（EC Block）：条纹单元的大小决定了条纹单元的读取、写入速度、Buffer的大小、以及编码的效率。

### EC策略命名
EC策略命名策略：EC编码器-EC Block数量-奇偶校验Block数量-条纹单元大小。Hadoop中内置了5种策略：
* RS-3-2-1024K
* RS-6-3-1024K
* RS-10-4-1024K
* RS-LEGACY-6-3-1024K
* XOR-2-1-1024K
同时，默认的副本策略也是支持的。副本策略设置在目录上，这样可以前置目录使用3副本方案，指定该目录不继承EC编码策略。这样，目录中是可以切换副本存储方式的。

### online-EC
Replication存储方式是始终启用的，默认启用的EC策略是：RS-6-3-1024K。与Replication存储方式一样，如果父目录设置了EC策略，子文件/目录会继承父目录的EC策略。
目录级别的EC策略仅会影响在目录中创建的新文件，这也意味着就的文件不会重新进行EC编码，HDFS是使用online-EC，文件一旦创建，可以查询它的EC策略，但不能再更改。
如果将已经进行EC编码的文件移动到其他EC策略的目录，文件的EC编码也不会改变。如果想要将文件转换为其他的EC策略，需要重写数据。可以通过distcp来移动数据，而不是mv。

### 自定义EC策略
HDFS允许用户基于XML自己来定义EC策略，可以参考conf目录下的user_ec_policies.xml.template文件。
```
<?xml version="1.0"?>
<configuration>
<!-- The version of EC policy XML file format, it must be an integer -->
<layoutversion>1</layoutversion>
<schemas>
  <!-- schema id is only used to reference internally in this document -->
  <schema id="XORk2m1">
    <!-- The combination of codec, k, m and options as the schema ID, defines
     a unique schema, for example 'xor-2-1'. schema ID is case insensitive -->
    <!-- codec with this specific name should exist already in this system -->
    <codec>xor</codec>
    <k>2</k>
    <m>1</m>
    <options> </options>
  </schema>
  <schema id="RSk12m4">
    <codec>RS</codec>
    <k>12</k>
    <m>4</m>
    <options> </options>
  </schema>
  <schema id="RS-legacyk12m4">
    <codec>RS-legacy</codec>
    <k>12</k>
    <m>4</m>
    <options> </options>
  </schema>
</schemas>
    
<policies>
  <policy>
    <!-- the combination of schema ID and cellsize(in unit k) defines a unique
     policy, for example 'xor-2-1-256k', case insensitive -->
    <!-- schema is referred by its id -->
    <schema>XORk2m1</schema>
    <!-- cellsize must be an positive integer multiple of 1024(1k) -->
    <!-- maximum cellsize is defined by 'dfs.namenode.ec.policies.max.cellsize' property -->
    <cellsize>131072</cellsize>
  </policy>
  <policy>
    <schema>RS-legacyk12m4</schema>
    <cellsize>262144</cellsize>
  </policy>
</policies>
</configuration>
```
配置文件很容易理解，主要包含两个部分组成：1.EC Schema：编码器、k（EC Block数量）、m（奇偶校验Block数量） 2.Policy：绑定schema、以及指定条纹单元大小

## 部署HDFS EC
### 集群配置要求
* EC对Hadoop集群的CPU、网络有额外的要求。EC编码、解码会消耗HDFS客户端、DataNode更多的CPU资源
* EC要求集群中的DataNode最起码和EC条纹宽度（条纹宽度 = EC Block数量 + 奇偶校验Block数量）是一样的。也就是，如果我们用使用RS-6-3策略，至少需要9台DataNode。
* EC Block文件也是分布在整个机架上，以实现机架级别的容错。在读写EC Block文件时，也需要保证机架的带宽。如果要实现机架级别的容错，需要拥有一定数量的机架容错。每个机架所存放的EC Block不能超过就校验块的数量。机架数量计算公式为：（EC Block数量 + 奇偶校验块数量）/ 奇偶校验块数量，然后四舍五入。例如：针对RS-6-3如果要实现机架级别的容错，至少需要（6 + 3）/ 3 = 3个机架。如果机架数小于这个数，将无法保证机架级别的容错。如果有进行机架级别停机维护需求，官方建议提供6 + 3以上个机架。

### EC配置
默认，除了dfs.namenode.ec.system.default.policy指定的默认策略，其他的内置的EC策略都是禁用的。我们可以根据Hadoop集群的大小、以及所需的容错属性，通过hdfs ec -enablePolicy -policy 策略名称来启用EC策略。例如：如果有5个节点的集群，比较适合的就是RS-3-2-1024k，而RS-10-4-1024k策略就不合适了。

### EC命令
C相关的操作，使用hdfs ec命令:
```
hdfs ec [generic options]
[-setPolicy -path <path> [-policy <policyName>] [-replicate]]
[-getPolicy -path <path>]
[-unsetPolicy -path <path>]
[-listPolicies]
[-addPolicies -policyFile <file>]
[-listCodecs]
[-enablePolicy -policy <policyName>]
[-disablePolicy -policy <policyName>]
[-verifyClusterSetup -policy <policyName>...<policyName>]
[-help [cmd ...]]
```

### 例子
因为我的测试集群只有3个节点，所以只能使用XOR-2-1-1024k,先要将XOR-2-1-1024k启用。
```
-- 创建用于存放冷数据的目录
[root@node1 hadoop]# hdfs dfs -mkdir -p /workspace/feng/cold_data

-- 启用XOR-2-1-1024 EC策略
[root@node1 hadoop]# hdfs ec -enablePolicy -policy XOR-2-1-1024k
Erasure coding policy XOR-2-1-1024k is enabled

-- 验证当前集群是否支持所有启用的或者指定的EC策略（这个命令应该是3.2.x添加的，我当前是3.1.4，还不支持这个命令）
-- hdfs ec -verifyClusterSetup -policy XOR-2-1-1024k

-- 设置冷数据EC存储策略
[root@node1 hadoop]# hdfs ec -setPolicy -path /workspace/feng/cold_data -policy XOR-2-1-1024k
Set XOR-2-1-1024k erasure coding policy on /workspace/feng/cold_data

-- 查看冷数据目录的存储策略
[root@node1 hadoop]# hdfs ec -getPolicy -path /workspace/feng/cold_data
XOR-2-1-1024k
```
