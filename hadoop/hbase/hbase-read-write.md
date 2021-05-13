# 读数据
## 流程总览
1. 从zookeeper中获取meta信息，并通过meta信息找到需要查找的table的startkey所在的region信息
2. 和该region所在的regionserver进行rpc交互获取result
3. region server查询memstore（memstore是是一个按key排序的树形结构的缓冲区），如果有该rowkey，则直接返回，若没有进入步骤4
4. 查询blockcache，如果有该rowkey，则直接返回，若没有进入步骤5
5. 查询storefile，不管有没有都直接返回
## client代码分析
hbase读数据除了直接操作hfile之外有3个入口，get()，batch()和scan()，get()相对而言就比较简单，找到对应的regionserver然后发rpc即可，batch()采用单rpc多action
的策略流程和get()类似，下面主要对scan涉及的核心接口进行分析。核心接口有以下几个：
* Connection：负责和zk建立连接
* Table：负责维护相关对象
* ResultScanner：负责给使用者遍历纾解
* Caller：负责调用Callable
* Callable：客户端和hbase交互的主要接口
### Connection
默认的连接器是HConnectionImplementation，可以通过配置`hbase.client.connection.impl`修改。核心思路是基于zk的watcher，保持长连接，然后获取hbase元数据
### Table
table通过Connection.getTable()实例化，默认的实现是HTable。这个类比较简单，只是维护了针对hbase一张表所用到的对象。主要关注遍历的方法，通过HTable.getScanner()实例化一个新的ResultScanner，使用者通过ResultScanner迭代器遍历获取result数据。
### Scanner
client提供了4种scanner，参考HTable.getScanner()，1. ClientScanner，读取result的流程需要3次rpc，openScanner，next和closeScanner；2
. 针对小量数据优化的ClientSmallScanner，和ClientScanner的区别在于，将openScanner,next和closeScanner合并到一个rpc执行，官方建议拉取的数据在64KB之内可以考虑用SmallScanner的方式；另外两个是基于reversed配置，也就是倒序遍历region，需要交换startkey和endkey的位置。ClientScanner是我们最常用的Scanner，也是默认的Scanner，下面对其进行分析：
1. 在初始化的时候通过nextScanner()方法，实例化一个新的Callable对象，并调用其call()方法
2. next()方法，当使用者不断的调用next()时，ClientScanner()会先从缓存中找，是否还有result，如果还有那么直接返回，如果缓存中没有result，那么调用loadCache()方法
3. loadCache()方法，调用Callable.call()，获取result数组。这里的异常处理需要特别关注，如果是UnkonwnScannerException，那么重试rpc直到本次scan超时，如果是OutOfOrderScannerNextException异常，scanner会重试rpc请求重复步骤3，如果已经重试过，那么直接抛出异常。重试的超时时间的配置`hbase.client.scanner.timeout.period`，默认是60s
4. 拉取到result后，ClientScanner会进行合并，这是由于拉取到的result是部分的，不是完整的，说到底hbase是以Cell为最小单位进行存储或者传输的，要封装成result的话就需要进行合并。合并完之后将result缓存在内存中，缓存策略基于caching和maxResultSize，caching表示hbase client最多可以缓存在内存多少条数据，也就是多少个result；maxResultSize表示hbase client最多可以缓存多少内存大小的result，也就是控制result占用堆的大小
5. 判断是否还需要再拉取result，这里有两种拉取判断，一种是之前的region拉取失败，转而拉取其replica，另一种是调用rpc拉取下一组result。
6. result达到内存限制或者数量（maxResultSize，caching）则返回
### ScannerCallable
ClientScanne对应的Callable是ScannerCallable，也是最典型的Callable，下面对其核心方法进行分析：
prepare()方法：核心功能是通过RPCRetryingCallerWithReadReplicas.getRegionLocations获取待遍历的table startkey的region，从而定位到region server
核心call()方法：
1. 首次调用call()，client会发送一次开始rpc，高速region server本次scan开始了，此次rpc不获取result，只生成scannerId，之后的rpc不需要再传递scan配置，这形成了一个会话的概念
2. 通过rpc controller获取CellScanner，再转换成Result数组，这里参考`ResponseConverter.getResults`。注意，这里由于获取的result是连续的，也就是说region server是有状态的服务，client每次rpc都会带上当前请求的序号，也就是nextCallSeq，这有的类似传统数据库中的分页作用。当出现序号不匹配，region server会抛出异常
3. 如果需要关闭，那么向region server发送close的rpc
## 总结
hbase-client的scan操作总体上可以看成是两层迭代器，面向使用者的Scanner以及面向region server的Callable。Callable负责从regionserver中获取result，主要解决，Scanner负责整合result提供给使用者。这样做的思路很明显，数据大小是肯定会大于内存的，通过迭代器接口，可以让使用者处理完之前的result再拉取其他result，从而起到分页的效果，这操作对使用者是透明的。如果需要详细的scan日志，可以通过配置`hbase.client.log.scanner.activity`来打开开关，默认是false

# 写数据
## 流程总览
1. zookeeper中获取meta信息，并通过meta信息找到需要查找的table的startkey所在的region信息（和读数据相似）
2. 将put缓存在内存中，参考`BufferedMutatorImpl`，并计算其内存大小，当超过`hbase.client.write.buffer`默认2097152字节也就是2MB，client会将put 通过rpc交给region server
3. region server接收数据后分别写到HLog和MemStore上一份
4. MemStore达到一个阈值后则把数据刷成一个StoreFile文件。若MemStore中的数据有丢失，则可以从HLog上恢复
5. 当多个StoreFile文件达到一定的数量，会触发Compact和Major Compaction操作，这里不对compaction的细节做展开。
6. 当Compact后，逐步形成越来越大的StoreFIle后，会触发Split操作，把当前的StoreFile分成两个，这里相当于把一个大的region分割成两个region，细节也不展开了。

# 总结
对于scan操作而言，拿ClientScanner来说，一次“完整rpc”过程包含3次rpc，open，result和close。如果失败了，region不可用或者在split，那么client会重试新的一次“完整rpc”，那么就是6次rpc。其他操作会少一点，例如SmallClientScanner一次“完整rpc”只需要1次rpc，它把open，close集成到了一起。hbase在client还是花了不少心思的。