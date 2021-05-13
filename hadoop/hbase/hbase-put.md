# Hbase put流程分析
HBase作为一种列族数据库，其将相关性较高的列聚合成一个列族单元，不同的列族单元物理上存储在不同的文件（HFile）内。一个表的数据会水平切割成不同的region分布在集群中不同的regionserver
上。客户端访问集群时会首先得到该表的region在集群中的分布，之后的数据交换由客户端和regionserver间通过rpc通信实现，下面我们从hbase源码里探究客户端put数据的流程。
## 客户端
put在客户端的操作主要分为三个步骤：
### 客户端缓存用户
get/delete/put/append/increment等等客户可用的函数都在客户端的HTable.java文件中。
如下变量：
```
protected AsyncProcess multiAp;
protected RpcRetryingCallerFactory rpcCallerFactory;
protected RpcControllerFactory rpcControllerFactory;
```
getBufferedMutator().mutate(put)，进入mutate这个函数可以看到它会把用户提交的此次put操作放入到列表writeAsyncBuffer队列中，当buffer中的数据超过规定值时，由后台进程进行提交。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907195539.png)

### 将writeBuffer中的put操作根据region的不同进行分组，分别放入不同的Map集合
进程提交由函数backgroudFlushCommits完成，提交动作包含同步提交和异步提交两种情况，由传入的参数boolean控制。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907200333.png)

可以发现无论异步提交还是同步提交，实际的提交动作是由AsyncProcess ap执行的，调用的语句如下： 
`ap.submit(tableName，writeAsyncBuffer,true,null,false) `
需要注意的是多数情况下执行的是异步提交，只有在异步提交出错的情况下执行同步提交。
submit函数：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907201231.png)

进入locateRegion方法：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907202701.png)

### 提交服务端RegionServer处理，在回调函数中与服务端交互。
最后调用sumitMultiActions函数将所有请求提交给服务端，它接受了上面的actionByServer作为参数，内部实例化一个AsyncRequestFutureImpl类执行异步的提交动作。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200908101026.png)

从sendMultiAction函数中一步步向里查看代码，其将用户的action请求通过getNewMultiActionRunnable、SingleServerRequestRunnable层层调用最终落到了hbase的RPC框架中，每个用户请求包装成包装MultiServerCallable对象，其是一个Runnable对象，在该对象中用户请求与服务端建立起RPC联系。所有的runnable对象最终交到AsyncProcess对象的内部线程池中处理执行。

## 服务端RegionServer如何响应客户端的Put请求
client端是通过MultiServerCallable.call()调用multi()方法来进行rpc请求的。追踪multi()方法：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200908202204.png)

真正写数据：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200910153627.png)

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200910153820.png)




