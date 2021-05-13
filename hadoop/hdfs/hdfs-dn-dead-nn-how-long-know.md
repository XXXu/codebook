## datanode磁盘损坏或者block损坏自动恢复过程
### 相关参数说明
* dfs.blockreport.intervalMsec：datanode向namenode报告块信息的时间间隔，默认6小时。
```
2020-08-12 13:11:14,885 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x941a5d27807f5,  containing 2 storage report(s), of which we sent 2. The reports had 301 total blocks and used 1 RPC(s). This took 0 msec to generate and 14 msecs for RPC and NN processing. Got back one command: FinalizeCommand/5
```
* dfs.datanode.directoryscan.interval：datanode进行内存和磁盘数据集块校验，更新内存中的信息和磁盘中信息的不一致情况，默认6小时
```
2020-08-12 16:45:01,727 INFO org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: BlockPool BP-70691628-172.22.27.7-1567757676314 Total blocks: 333, missing metadata files:0, missing block files:0, missing blocks in memory:250, mismatched blocks:0
```
## 测试
* 直接删除dn目录下的block
* 执行好后马上执行fsck 还是显示healthy状态，复制个数还是3（因为datonode节点还没有检测内存和磁盘上的数据块状态）,下面的日志我已经改过时间了，改成一分钟。
```
2020-08-26 21:01:33,667 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f8339c4d33d5,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 12 msec to generate and 50 msecs for RPC and NN processing. Got back no commands.
2020-08-26 21:01:54,092 INFO org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: BlockPool BP-1996945766-172.22.24.60-1588684903649 Total blocks: 64324, missing metadata files:0, missing block files:0, missing blocks in memory:0, mismatched blocks:0
2020-08-26 21:01:54,669 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f838810a81af,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 12 msec to generate and 35 msecs for RPC and NN processing. Got back one command: FinalizeCommand/5.
2020-08-26 21:01:54,669 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Got finalize command for block pool BP-1996945766-172.22.24.60-1588684903649
2020-08-26 21:02:33,680 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f841963a0294,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 22 msec to generate and 36 msecs for RPC and NN processing. Got back no commands.
2020-08-26 21:02:54,721 INFO org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: BlockPool BP-1996945766-172.22.24.60-1588684903649 Total blocks: 64324, missing metadata files:0, missing block files:0, missing blocks in memory:0, mismatched blocks:0
2020-08-26 21:02:54,771 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f8467f582dd9,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 9 msec to generate and 36 msecs for RPC and NN processing. Got back one command: FinalizeCommand/5.
2020-08-26 21:02:54,771 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Got finalize command for block pool BP-1996945766-172.22.24.60-1588684903649
2020-08-26 21:03:33,670 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f84f8e2c22e9,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 12 msec to generate and 31 msecs for RPC and NN processing. Got back no commands.
2020-08-26 21:03:54,837 INFO org.apache.hadoop.hdfs.server.datanode.DirectoryScanner: BlockPool BP-1996945766-172.22.24.60-1588684903649 Total blocks: 64324, missing metadata files:0, missing block files:0, missing blocks in memory:0, mismatched blocks:0
2020-08-26 21:03:54,906 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Successfully sent block report 0x1f8547ea3cc5f,  containing 2 storage report(s), of which we sent 2. The reports had 64328 total blocks and used 1 RPC(s). This took 10 msec to generate and 53 msecs for RPC and NN processing. Got back one command: FinalizeCommand/5.
2020-08-26 21:03:54,907 INFO org.apache.hadoop.hdfs.server.datanode.DataNode: Got finalize command for block pool BP-1996945766-172.22.24.60-1588684903649
```
nn的日志：
```
2020-08-26 21:01:40,019 INFO BlockStateChange: BLOCK* processReport 0x1f831f84c53b1: from storage DS-c21b0106-e34f-435e-aa9c-602b6586c71f node DatanodeRegistration(172.22.24.61:50010, datanodeUuid=9589bc95-92b5-4395-bc83-b022a1d9806a, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32089, hasStaleStorage: false, processing time: 10 msecs
2020-08-26 21:02:09,314 INFO BlockStateChange: BLOCK* processReport 0x1f838810a81af: from storage DS-e039ebff-6a39-4b51-b311-6e5042090e51 node DatanodeRegistration(172.22.24.60:50010, datanodeUuid=a3bef5a8-de66-469b-b233-9230b139aaee, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32105, hasStaleStorage: false, processing time: 14 msecs
2020-08-26 21:02:09,325 INFO BlockStateChange: BLOCK* processReport 0x1f838810a81af: from storage DS-3ae505f6-4ee6-4ba0-970f-238dbfd48d4b node DatanodeRegistration(172.22.24.60:50010, datanodeUuid=a3bef5a8-de66-469b-b233-9230b139aaee, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32223, hasStaleStorage: false, processing time: 11 msecs
2020-08-26 21:02:16,867 INFO BlockStateChange: BLOCK* processReport 0x1f8398f002590: from storage DS-944ebe72-aee4-4da8-a0bc-0d026745ef2e node DatanodeRegistration(172.22.24.62:50010, datanodeUuid=7eb7d04d-57a7-423e-92d5-5a5d7e8e2b44, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32232, hasStaleStorage: false, processing time: 19 msecs
2020-08-26 21:02:16,879 INFO BlockStateChange: BLOCK* processReport 0x1f8398f002590: from storage DS-6a9b8877-c5a6-4f12-9536-d99d9560c296 node DatanodeRegistration(172.22.24.62:50010, datanodeUuid=7eb7d04d-57a7-423e-92d5-5a5d7e8e2b44, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32096, hasStaleStorage: false, processing time: 12 msecs
2020-08-26 21:02:37,035 INFO BlockStateChange: BLOCK* processReport 0x1f83f3e1c7d04: from storage DS-63518d82-b7a0-4666-8cca-fb9c9b94c40f node DatanodeRegistration(172.22.24.61:50010, datanodeUuid=9589bc95-92b5-4395-bc83-b022a1d9806a, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32239, hasStaleStorage: false, processing time: 34 msecs
2020-08-26 21:02:37,049 INFO BlockStateChange: BLOCK* processReport 0x1f83f3e1c7d04: from storage DS-c21b0106-e34f-435e-aa9c-602b6586c71f node DatanodeRegistration(172.22.24.61:50010, datanodeUuid=9589bc95-92b5-4395-bc83-b022a1d9806a, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32089, hasStaleStorage: false, processing time: 13 msecs
2020-08-26 21:03:09,416 INFO BlockStateChange: BLOCK* processReport 0x1f8467f582dd9: from storage DS-e039ebff-6a39-4b51-b311-6e5042090e51 node DatanodeRegistration(172.22.24.60:50010, datanodeUuid=a3bef5a8-de66-469b-b233-9230b139aaee, infoPort=50075, infoSecurePort=0, ipcPort=50020, storageInfo=lv=-56;cid=hdfs1;nsid=1189803414;c=0), blocks: 32105, hasStaleStorage: false, processing time: 15 msecs
blockLog.debug("BLOCK* ask {} to replicate {} to {}", rw.srcNode,rw.block, targetList);
```
## 总结：
单纯的模拟了其中一个数据块损坏的情况，数据块损坏后，在该节点执行directoryscan之前（dfs.datanode.directoryscan.interval决定），都不会发现损坏，在向namenode报告数据块信息之前（dfs.blockreport.intervalMsec决定），都不会恢复数据块，当namenode收到块信息后才会采取恢复措施

## DN异常多久后，会被NN感知
DataNode一固定周期（dfs.heartbeat.interval，默认3秒）向NameNode发送心跳，NameNode如果在一段时间内没有收到心跳，就会标记DataNode宕机。
计算公式：
```
this.heartbeatExpireInterval = 2 * heartbeatRecheckInterval+ 10 * 1000 * heartbeatIntervalSeconds;
```
默认 dfs.namenode.heartbeat.recheck-interval是5分钟 



