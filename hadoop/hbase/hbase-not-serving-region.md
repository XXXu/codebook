# HBase集群出现NotServingRegionException问题的排查及解决方法
Hbase集群在读写过程中，可能由于Region Split或Region Blance等导致Region短暂下线，此时客户端与Hbase集群进行RPC操作时会抛出NotServingRegionException
异常，从而导致读写操作失败。
## 解决
首先，从客户端考虑，其实就是要保证Region下线不可服务期间，读写请求能够在集群恢复后继续，具体可以采取如下措施：
* 对于写端，可以将未写入成功的记录，添加到一个客户端缓存中，隔一段时间后交给一个后台线程统一重新提交一次；也可以通过setAutoFlush(false)保证失败的记录不被抛弃，留在客户端Writebuffer
中等待下次writeBuffer满了后再次尝试提交，知道提交成功为止。
* 对于读端，捕获异常后，可以采取休眠一段时间后进行重试等方式
* 当然，还可以根据实际情况合理调整hbase.client.reties.number和hbase.client.pause配置选项
然后，从服务端考虑，需要分别Region Split和Region Balance进行解决：
* 由于建表时，我们已经考虑到了数据在不同的region server上的均匀分布，而且预先在不同的region server上创建并分配了相同数目的region，所以可以选择关掉Hbase的region自动balance
功能，当然关掉后可以选择在每天读写压力小的时候触发一次balance操作即可。
* 接下来，regoin总是被创建，不能被复用的问题改如何解决呢？根本原因时rowkey中包含了timestamp字段，而每时每刻timestamp总是向上增长的，但是，使用方确实需要能够根据timestamp字段进行顺序scan
操作，因此，timestamp字段必须保留。据此，这里给出两种解决思路：
    * 一种常用方法是将表按照时间分表，例如按天进行分表，这样可以通过预先建表创建好region分区，避免实际读写过程中频繁触发region slipt等过程，但是这一方法的缺点是每天需要预先建好表，而这一DDL
    过程可能出现问题进而导致读写出现问题，同时跨天时读写端也需要做出适应，调整为读写新创建的表。
    * 还可以通过修改表的rowkey结构，将timestamp字段改成一个周期循环的timestamp，如取timestamp % TS_MODE须大于等于表的TTL
    时间周期，这样才能保证数据不会被覆盖掉，经过这样的改造后，即可实现Region复用，避免region的无限上涨，对于读写端的变更也较小，读写端操作时只需将timestamp字段取模后作为rowkey
    进行读写，另外，读端需要考虑能适应scan扫描时处理[startTsMode,endTsMode]和[endTsMode,startTsMode]两种情况。
