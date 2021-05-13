# hbase运维手册
## region情况
### region的数量
总数和每台regionserver上的region数：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200709180719.png)
### region的大小
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710094610.png)

## 缓存命中率
缓存命中率对hbase的读有很大的影响，可以观察这个指标来调整blockcache的大小。
注意：HBase上Regionserver的内存分为两个部分，一部分作为Memstore，主要用来写；另外一部分作为BlockCache，主要用于读。
* 写请求会先写入Memstore，Regionserver会给每个region提供列族数提供一定数量的Memstore，当Memstore满64MB以后，会启动 flush刷新到磁盘。当Memstore的总大小超过限制时（heapsize * hbase.regionserver.global.memstore.upperLimit * 0.9），会强行启动flush进程，从最大的Memstore开始flush直到低于限制。
* 读请求先到Memstore中查数据，查不到就到BlockCache中查，再查不到就会到磁盘上读，并把读的结果放入BlockCache。由于BlockCache采用的是LRU策略，因此BlockCache达到上限(heapsize * hfile.block.cache.size * 0.85)后，会启动淘汰机制，淘汰掉最老的一批数据。
一个Regionserver上有一个BlockCache和N个Memstore，它们的大小之和不能大于等于heapsize * 0.8，否则HBase不能正常启动。
默认配置下，BlockCache为0.2，而Memstore为0.4。在注重读响应时间的应用场景下，可以将 BlockCache设置大些，Memstore设置小些，以加大缓存的命中率。
HBase RegionServer包含三个级别的Block优先级队列：
* Single：如果一个Block第一次被访问，则放在这一优先级队列中；
* Multi：如果一个Block被多次访问，则从Single队列移到Multi队列中；
* InMemory：如果一个Block是inMemory的，则放到这个队列中。
以上将Cache分级思想的好处在于：
* 首先，通过inMemory类型Cache，可以有选择地将in-memory的column families放到RegionServer内存中，例如Meta元数据信息；
* 通过区分Single和Multi类型Cache，可以防止由于Scan操作带来的Cache频繁颠簸，将最少使用的Block加入到淘汰算法中。
默认配置下，对于整个BlockCache的内存，又按照以下百分比分配给Single、Multi、InMemory使用：0.25、0.50和0.25。
注意，其中InMemory队列用于保存HBase Meta表元数据信息，因此如果将数据量很大的用户表设置为InMemory的话，可能会导致Meta表缓存失效，进而对整个集群的性能产生影响。
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710105503.png)

## 读写请求数
通过读写请求数可以大概看出每台regionServer的压力，如果压力分布不均匀，应该检查regionServer上的region以及其它指标。
整个hbase的读写压力：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710111416.png)
单个regionserver的region压力：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710111517.png)

## 刷新队列
单个region的memstore写满(128M)或regionServer上所有region的memstore大小总合达到门限时会进行flush操作,flush操作会产生新的storeFile

## rpc调用队列
没有及时处理的rpc操作会放入rpc操作队列，从rpc队列可以看出服务器处理请求的情况，如果处理时间比较长，可能需要增加处理的线程数

## 文件块保存在本地的百分比
datanode和regionserver一般都部署在同一台机器上，所以region server管理的region会优先存储在本地，以节省网络开销。如果block locality较低有可能是刚做过balance或刚重启，经过compact之后region的数据都会写到当前机器的datanode，block locality也会慢慢达到接近100
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710153214.png)

## 内存使用情况
内存使用情况,主要可以看used Heap和memstore的大小，如果usedHeadp一直超过80-85%以上是比较危险的，memstore很小或很大也不正常：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200710154357.png)

## 检查数据一致性以及修复方法
数据一致性：
* 每个region都被正确的分配到一台regionserver上，并且region的位置信息及状态都是正确的。
* 每个table都是完整的，每一个可能的rowkey 都可以对应到唯一的一个region
用以下命令检查：
hbase hbck：有时集群正在启动或者region正在做split操作，会造成数据不一致
hbase hbck -details: 会列出更加详细的检查信息，包括正在进行的split任务
hbase hbck tablename: 检查某个表

### 局部的修复
如果出现数据不一致，修复时要最大限度的降低可能出现的风险，使用以下命令对region进行修复风险降低：
* hbase hbck -fixAssignments:  修复region没有分配(unassigned)，错误分配（incorrectly assigned）以及多次分配（multiply assigned）的问题
* hbase hbck -fixMeta: 删除META表里有记录但HDFS里没有数据记录的region,添加HDFS里有数据但是META表里没有记录的region到META表
* hbase hbck -fixHdfsHoles：如果rowkey出现空洞，即相邻的两个region的rowkey不连续，则使用这个参数会在hdfs里面创建一个新的region，创建新的region之后要使用-fixMeta
和-fixAssignments参数来使用挂载这个region，所以一般和前两个参数一起使用。
* hbase hbck -repairHoles: 等价于hbase hbck -fixAssignments -fixMeta -fixHdfsHoles

### region重叠修复
进行以下操作非常危险，因为这些操作会修改文件系统，需要谨慎操作！以下操作前先使用hbck -details查看详细问题，如果需要进行修复先停掉应用，如果执行以下命令同时有数据操作可能会造成不可期的异常。
* hbase hbck -fixHdfsOrphans 将文件系统中的没有metadata文件(.regioninfo)的region目录加入到hbase中，即创建.regioninfo目录并将region分配到regionserver。
通过两种方式可以将rowkey有重叠的region合并：
    * merge：将重叠的region合并成一个大的region。
    * sideline：将region重叠的部分去掉，并将重叠的数据先写入到临时文件，然后再导入进来。如果重叠的数据很大，直接合并成一个大的region会产生大量的split和compact操作，可以通过以下参数控制region过大：
    -maxMerge 合并重叠region的最大数量
    -sidelineBigOverlaps 假如有大于maxMerge个数的 region重叠, 则采用sideline方式处理与其它region的重叠.
    -maxOverlapsToSideline 如果用sideline方式处理重叠region，最多sideline n个region
* hbase hbck -repair 以下命令的缩写：
`hbase hbck -fixAssignments -fixMeta -fixHdfsHoles -fixHdfsOrphans -fixHdfsOverlaps -fixVersionFile -sidelineBigOverlaps`
可以指定表名：hbase hbck -repair tablename

* hbase hbck -fixMetaOnly –fixAssignments: 如果只有META表region不一致，可以用此命令修复

* hbase hbck –fixVersionFile： Hbase的数据文件启动时需要一个version file，如果这个文件丢失，可以用这个命令来新建一个，但是要保证hbck的版本和Hbase集群的版本是一样的

* hbase org.apache.hadoop.hbase.util.hbck.OfflineMetaRepair：如果ROOT表和META表都出问题了，hbase无法启动可以用这个命令创建新的ROOT和META
表。这个命令的前提是hbase已经关闭，执行时它会从hbase的home目录加载hbase的相关信息（.regioninfo），如果表的信息是完整的就会创建新的ROOT和META目录及数据

* hbase hbck –fixSplitParents：当region做split操作的时候，父region会被自动清除掉。但是有时候子region在父region被清除之前又做了split。造成有些延迟离线的父region存在于META表和HDFS中，但是没有部署，HBASE又不能清除他们。这种情况下可以使用此命令重置这些在META表中的region为在线状态并且没有split。然后就可以使用之前的修复命令把这个region修复

### 手动merge region
进行操作前先将balancer关闭，操作完成后再打开balancer，经过一段时间的运行之后有可能会产生一些很小的region，需要定期检查这些region并将它们和相邻的region合并以减少系统的总region数，减少管理开销。
合并方法：
* 找到需要合并的region的encoded name
* 进入hbase shell
* 执行merge_region 'region1','region2'

### 手动分配region
如果发现某台regionServer资源占用特别高，可以检查这台regionserver上的region是否存在过多比较大的region,通过hbase shell将部分比较大的region分配给其他不是很忙的regions server：
move  'encodeRegionName', 'ServerName' 
> encodeRegionName指的regioName后面的编码,ServerName指的是master-status的Region Servers列表
> 例：move '24d9eef6ba5616b1a60180503e62bae7','DN1,60020,1429840460046'

### balance_switch
balance_switch true 打开balancer
balance_switch flase 关闭balancer
配置master是否执行平衡各个regionserver的region数量，当我们需要维护或者重启一个regionserver时，会关闭balancer，这样就使得region在regionserver上的分布不均，这个时候需要手工的开启balance。

### 手动major_compact
进行操作前先将balancer关闭，操作完成后再打开balancer，选择一个系统比较空闲的时间手工major_compact，比较消耗io资源，操作尽量避免高峰期。

### regionserver重启
graceful_stop.sh --restart --reload --debug nodename
进行操作前先将balancer关闭，操作完成后再打开balancer，这个操作是平滑的重启regionserver进程，对服务不会有影响，它会先将需要重启的regionserver上面的所有region
迁移到其他节点，然后重启，最后又会将之前region迁移回来，当我们修改一个配置时，可以用这种方式重启每一台机器，对于hbase regionserver重启，不要直接kill进程，这样会造成zookeeper.session
.timeout这个时间长的中断，也不要通过bin/hbase-daemon.sh stop regionserver去重启，如果运气不太好，-ROOT-或者.META表在上面的话，所有的请求会全部失败。

### regionserver关闭下线
graceful_stop.sh  nodename
进行操作前先将balancer关闭，操作完成后再打开。和上面一样，系统会在关闭之前迁移所有的region，然后stop进程。

### flush表
所有memstore刷新到hdfs，通常如果发现regionserver的内存使用过大，造成该机的regionserver很多线程block，可以执行一下flush操作，这个操作会造成hbase的storefile
数量剧增。在hbase进行迁移的时候，如果选择拷贝文件方式，可以先停写入，然后flush所有表，拷贝文件。

### 强制split
split 'forced_table'
region splits 执行过程：
regionserver处理写请求的时候，会先写入memstore，当memstore达到一定大小的时候，会写入磁盘成为一个store file，这个过程叫做memstore flush，当store file
堆积到一定大小的时候，regionserver会执行compact操作，把他们合成一个大的文件，当每次执行完flush或者compact操作，都会判断是否需要split，当split的时候，会生成两个region A和region B
但是parent region数据file并不会发生复制等操作，而是region A和region B会有这些file的引用。这些引用文件会在下次发生compact操作的时候清理掉，并且当region
中有引用文件的时候是不会再进行split操作的。
这个地方需要注意一下：
大量的写入会刷大量hfile，一个region就会对这大量的hfile进行compact操作。如果这时候触发了split操作，这个region会成为父region，而两个子region会保留父region
的引用文件，而在这期间，子region会继续写入数据，那么又可能触发子region的compact，这里的关键点来了--子region如果做compact的文件都是新写入的文件，而迟迟不去compact父region
引用的文件，会导致一个问题--就是这个子region无法split（因为含有父region引用的region是不能被split的）。那么子region越来越大，由于写入文件数据急剧增长，父region的ref
文件总也得不到机会compact，就形成了大region的恶性循环情况--由于region太大，compact无法完成，但是由于compact无法完成大致region无法split，无法分摊compact
的压力给其他regionserver。

虽然split region操作是regionserver单独确定的，但是split过程必须和很多其他部件合作，regionserver在split开始前和结束前通知master，并且需要更新META
表，这样客户端就能知道有新的reiogn，在hdfs中重新排列目录结构和数据文件，split是一个复杂的操作。在split region的时候会记录当前执行的状态，当出错的时候，会根据状态进行回滚。
* region server决定split region，第一步region server在zk中/hbase/region-in-transition/region-name 目录下，创建一个znode，状态为SPLITING.
* 因为master有对region-in-transision的znode做监听，所以master得知parent region需要split。
* region server在hdfs的parent region的目录下创建一个名为".splits"的子目录
* region server关闭parent region，强制flush缓存，并且在本地数据结构中标记region为下线状态，如果这个客户端刚好请求到parent region
，会抛出NotServingRegionException，这时客户端会进行补偿性重试。
* region server在.split目录下分别两个daughter region创建目录和必要的数据结构，然后创建两个引用文件指向parent regions的文件。
* region server在hdfs中，创建真正的region目录，并且把引用文件移到对应的目录下。
* region server发送一个put的请求到meta表，并且在meta表中设置parent region为下线状态，并且在parent region对应的row中两个daughter region的信息。但是这个时候在meta
表中daughter region还不是独立的row，这个时候如果client scan meta表，会发现parent region正在split，但是client还看不到daughter region的信息，当这个put
成功之后，parent region split会被正在执行。如果在rpc成功之前region server就失败了，master和下次打开parent region的region server会清楚关于这次split
的脏状态。但是当rpc返回结果给到parent region，即meta成功更新之后，region split的流程还会继续进行下去。相当于是个补偿机制，下次在打开这个parent region的时候会进行相应的清理操作。
* region server 打开daughter region接受写操作。
* region server在meta表中增加daughter A和B region的相关信息，在这以后，client就能发现这两个新的regions并且能发送请求到这两个新的region了。client本地具体meta
表的缓存，当他们访问到parent region的时候，发现parent region下线了，就会重新访问meta表获取最新的信息，并且更新本地缓存。
* region server更新znode的状态为split，master就能状态更新了，master的平衡机制会判断是否需要把daughter regions分配到其他region server中。
* 在split之后，meta和hdfs依然会有引用指向parent region，当compact操作发生在daughter regions中，会重写数据file，这个时候引用就会被逐渐的去掉。垃圾回收任务定时检测daughter
 regions是否还有引用parent files，如果没有引用指向parent files的话，parent region就会被删除。
 








