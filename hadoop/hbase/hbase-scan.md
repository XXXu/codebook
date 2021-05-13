# Hbase scan 流程分析
## 客户端
从客户端scan-api代码开始：`table.getScanner(scan)`,然后进入HTable的getScanner(final Scan scan)方法：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200917170814.png)

默认进入ClientScanner的构造函数中：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200917171736.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200917172051.png)

接着：
```
callable = getScannerCallable(localStartKey, nbRows);
// Open a scanner on the region server starting at the
// beginning of the region
call(callable, caller, scannerTimeout);
this.currentRegion = callable.getHRegionInfo();
if (this.scanMetrics != null) {
this.scanMetrics.countOfRegions.incrementAndGet();
}
```

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200917172556.png)

请求由ScannerCallable.call()发起:

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200917174756.png)

## 服务端
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200918152920.png)
接下来初始化storeScanner
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200918153253.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200918161859.png)

###Seek语义：
seek是针对KeyValue的，seek的语义是seek到指定KeyValue，如果指定KeyValue不存在，则seek到指定KeyValue的下一
个。举例来说，假设名为X的column family里有两列a和b，文件中有两行rowkey分别为aaa和 bbb，如下表所示：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200918164746.png)

HBase客户端设置scan请求的start key为aaa，那么matcher.getStartKey()会被初始化为(rowkey, family, qualifier,timestamp,type)=(aaa,X,null,LATEST_TIMESTAMP,Type.DeleteFamily)，根据KeyValue的比较原则，这个KeyValue比aaa行的第一个列a更
小(因为没有qualifier)，所以对这个StoreFileScanner seek时，会seek到aaa这行的第一列a。

`seekScanners(scanners, matcher.getStartKey(), explicitColumnQuery && lazySeekEnabledGlobally,isParallelSeekEnabled);`

有可能不会对StoreFileScanner进行实际的seek，而是进行lazy seek，seek的工作放到不得不做的时候。后续会专门说lazy seek.
堆用类KeyValueHeap表示,看KeyValueHeap构造函数做了什么:

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200918165547.png)

### Lazy Seek优化:
在这个优化之前，读取一个column family(Store),需要seek其下的所有Hfile和MemStore到指定的查询KeyValue，seek的语义为如果KeyValue存在则seek到对应位置，如果不存在，则seek
到这个Keyvalue的后一个KeyValue，假设Store下有3个Hfile和一个MemStore，按照时序递增记为[HFile1,HFile2,HFile3,Memstore]，在lazy seek
优化之前，需要对所有的HFile和MemStore进行seek，对HFile文件的seek比较慢，往往需要将HFile相应的block加载到内存，然后定位。在有了lazy seek优化之后，如果需要的KeyValue在HFile3
中就存在，那么HFile1和HFile2都不需要seek，大大提高了速度。大体来说，思路时请求seek某个KeyValue时实际上没有对StoreFileScanner进行真正的seek，而是对于每个StoreFileScanner
，设置它的seek为(rowkey,family,qualifier,lastTimestampInStoreFile) KeyValueHeap有两个重要的接口，peek()和next(),他们都是返回堆顶，区别在于next
()会将堆顶出堆，然后重新调整堆，对外来说就是迭代器向前移动，而peek()不会将堆顶出堆，堆顶不变。视线中peek()操作非常简单，只需要调用堆的成员变量current
的peek()方法操作即可，拿StoreScannner堆举例，current要么是StoreFileScanner类型要么是MemStore，那么到底current是如何选择出来的以及lazy seek是如何实现的？
下面举个例子说明。
#### 前提：
Hbase开启了lazy seek优化(实际上默认开启)
#### 假设：
Store下有三个HFile和MemStore，按照时间顺序记作[HFile1,HFile2,HFile3,MemStore],seek KeyValue为(rowkey,family,qualifier,timestamp)，记作seekKV.
并且它只在HFile3中存在，不在其他HFile和MemStore中存在
#### lazy seek过程：
seekScanner()的逻辑，如果是lazy seek，则对于每个Scanner都调用requestSeek(seekKV)方法，方法内部首先进行rowcol类型的bloom filter过滤。
1.如果结果判定seekKV在StoreFile中肯定不存在，则直接设置StoreFileScanner的peek为kv.createLastOnRowCol(),并且将realSeekDone设置true，表示实际的seek完成。
```
public KeyValue createLastOnRowCol() {
    return new KeyValue(
        bytes, getRowOffset(), getRowLength(),
        bytes, getFamilyOffset(), getFamilyLength(),
        bytes, getQualifierOffset(), getQualifierLength(),
        HConstants.OLDEST_TIMESTAMP, Type.Minimum, null, 0, 0);
  }
```

