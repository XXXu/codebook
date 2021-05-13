# yarn的日志分析
## yarn中的id
Yarn中的id种类繁多，比较乱，下面整理下yarn中常出现的几种id，分别介绍一下:
* jobId:
    * 描述：出自MapReduce任务，对作业的唯一标识
    * 格式：job_${clusterStartTIme}_${jobid}
* applicationId:
    * 描述：在yarn中对作业的唯一标识
    * 格式：application_${clusterStartTime}_${applicationId}
* taskId:
    * 描述：作业中的任务的唯一标识
    * 格式：task_${clusterStartTime}_${applicationId}_[m|r]_${taskId}
* attempId:
    * 描述：任务尝试执行的一次id
    * 格式：attempt_${clusterStartTime}_${applicationId}_[m|r]_${taskId}_${attempId}
* appAttempId:
    * 描述：ApplicationMaster的尝试执行的一次id
    * 格式：appattempt_${clusterStartTime}_${applicationId}_${appAttemptId}
* containerId:
    * 描述：container的id
    * 格式：container_e*epoch*_${clusterStartTime}_${applicationId}_${appAttempId}_${containerId}
## yarn中的日志
### 服务类日志
诸如ResourceManage、NodeManager等系统自带的服务输出来的日志默认是存放在${HADOOP_HOME}/logs目录下，此参数可以通过参数YARN_LOG_DIR（yarn-env.sh配置文件）指定。
比如resourcemanager的输出日志为yarn-${USER}-resourcemanager-${hostname}.log，其中${USER}s是指启动resourcemanager进程的用户，${hostname}是resourcemanager进程所在机器的hostname，nodemanager的输出日志格式为：yarn-${USER}-nodemanager-${hostname}.log，这类日志可以查看当前resourcemanager和nodemanager两个服务的运行情况。
### 任务日志
#### 作业的统计日志
历史作业的记录里面包含了一个作业用了多少个Map、用了多少个Reduce、作业提交时间、作业启动时间、作业完成时间等信息；
这些信息对分析作业是很有帮助的，我们可以通过这些历史作业记录得到每天有多少个作业运行成功、有多少个作业运行失败、每个队列作业运行了多少个作业等很有用的信息。这部分日志会用于JobHistoryServer。
目录参数：mapreduce.jobhistory.done-dir、mapreduce.jobhistory.intermediate-done-dir(hdfs的路径)
#### 作业的运行日志
Container日志包含ApplicationMaster日志和普通Task日志等信息，主要包含container的启动脚本，还有container的运行日志。
目录参数：yarn.nodemanager.log-dirs

## 日志的聚合
上面的运行日志中如果查看了，会发现container的日志在一台机器的所有块盘上都会存在日志，
而且并不知道container的日志会在哪个盘上，默认情况下，每块盘上都会创建相同的applicationid，
而且applicationid中都会创建相同的containerid，但是并不是每个container中都会存在日志，
这块由container自己的机制选择往哪个container目录中写入日志，其他的container目录则为空。这在一定程度上导致了想查看任务的运行日志比较困难。
> 日志的聚合功能可以解决这个问题：yarn.log-aggregation-enable,此项功能会把各nodemanager上的application的所有盘上的container上传到hdfs,
参数：yarn.nodemanager.remote-app-log-dir、yarn.nodemanager.remote-app-log-dir-suffix

## 日志清理
参数：mapreduce.jobhistory.max-age-ms、yarn.log-aggregation.retain-seconds，负责清理hdfs路径下的log
