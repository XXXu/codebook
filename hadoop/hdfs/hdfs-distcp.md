# 双网卡安全模式下的distcp

## 网络拓扑
源和目标两个集群，都配置了双网卡，并开启安全。两个集群间配置了互信。详细信息如下：
源集群：
节点名称|内网地址|外网地址
----|:----:|----:
dualnic1|10.10.0.2|172.22.51.2
dualnic2|10.10.0.3|172.22.51.3
dualnic3|10.10.0.4|172.22.51.4
目标集群：
节点名称|内网地址|外网地址
----|:----:|----:
tdh6-1|10.10.0.18|172.22.0.218
tdh6-2|10.10.0.19|172.22.0.219
tdh6-3|10.10.0.20|172.22.0.220

两个集群即可以通过外网地址通信，也可以通过内网地址通信。也就是说，两个网段都是互通的。
但是/etc/hosts中配置的是内网地址：
```
# cat /etc/transwarp/conf/hosts
127.0.0.1 localhost localhost.localdomain localhost4 localhost4.localdomain4
::1 localhost localhost.localdomain localhost6 localhost6.localdomain6
10.10.0.2 dualnic1
10.10.0.3 dualnic2
10.10.0.4 dualnic3
10.10.0.18 tdh6-1
10.10.0.19 tdh6-2
10.10.0.20 tdh6-3
```
TDH-Client（从源集群下载）部署在一个第三方节点上，该节点只配有外网地址，172.22.x.x。所以，其/etc/hosts中两个集群的节点配置如下：
```
172.22.51.2 dualnic1
172.22.51.3 dualnic2
172.22.51.4 dualnic3
172.22.0.218 tdh6-1
172.22.0.219 tdh6-2
172.22.0.220 tdh6-3
```
这时，如下操作可以执行成功：
```
# kinit admin
# klist
Ticket cache: FILE:/tmp/krb5cc_d055d47e-e6ea-4e98-938b-94b6bb30de7b
Default principal: admin@dualtest
Valid starting Expires Service principal
2020-07-20T14:22:05 2020-07-21T14:22:05 krbtgt/dualtest@dualtest
renew until 2020-07-27T14:22:05
# hdfs dfs -ls hdfs://dualnic3:8020/tmp
# hdfs dfs -ls hdfs://tdh6-1:8020/tmp
# hdfs dfs -ls webhdfs://tdh6-2/tmp
# hdfs dfs -put maven-metadata.xml webhdfs://tdh6-2/tmp
``` 
## 问题描述
在第三方节点执行如下distcp命令:
`hadoop distcp -m 240 -update -skipcrccheck hdfs://dualnic3:8020/tmp/distcp.log hdfs://tdh6-1:8020/tmp/`
本地测试环境yarn的resource manager报错：
```
2020-07-20 15:54:59,784 WARN org.apache.hadoop.yarn.server.resourcemanager.security.DelegationTokenRenewer: Unable to add the application to the delegation token renewer.
java.io.IOException: Failed to renew token: Kind: HDFS_DELEGATION_TOKEN, Service: 172.22.51.4:8020, Ident: (HDFS_DELEGATION_TOKEN to
ken 1 for admin)
...
Caused by: java.net.NoRouteToHostException: No Route to Host from dualnic2/10.10.0.3 to 172.22.51.4:8020 failed on socket timeout exception: java.net.NoRouteToHostException: No route to host; For more details see: http://wiki.apache.org/hadoop/NoRouteToHost
```
这个报错和现场并不完全一样，现场报错如下：
```
2020-07-01 16:45:38,203 WARN org.apache.hadoop.yarn.server.resourcemanager.security.DelegationTokenRenewer: Unable to add the application to the delegation token renewer.
java.io.IOException: Failed to renew token: Kind: HDFS_DELEGATION_TOKEN, Service: 10.10.26.36:8020, Ident: (HDFS_DELEGATION_TOKEN token 30999 for mdm)
...
Caused by: org.apache.hadoop.net.ConnectTimeoutException: 20000 millis timeout while waiting for channel to be ready for connect. ch : java.nio.channels.SocketChannel[connection-pending local=/192.168.1.20:59063 remote=10.10.26.36/10.10.26.36:8020]
```
## 问题原因
虽然错误不完全一样，但是问题是一样的:
* distcp向目标集群认证，并获得token，注意该token的中Service字段被设置为：172.22.51.4:8020。因为client节点只有该网段的IP。
* distcp向yarn提交任务，并把该token传给yarn的resource manager
* resource manager拿到token，会进行renew操作；

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907154334.png)

* renew时，使用yarn用户与目标集群的namenode通信。而yarn用户的principal，是这样的：yarn/dualnic2@dualtest；
* DFSClient从该principal中解析出hostname：dualnic2，进而解析并绑定本地IP地址：dualnic2/10.10.0.3为源地址，尝试去连接namenode；
* 而namenode的地址是从distcp提交的token中的Service字段中获取的：172.22.51.4:8020；
* 所以TCP连接不能建立，超时失败。
之所以错误不一样，发现本地环境是先renew的访问本集群的token，而现场是先renew目标集群的token。

### 第一次workaround：修改bind逻辑
为了绕过这个问题，修改了代码，不再bind本地IP地址：
```
private synchronized void setupConnection() throws IOException {
 ...
 /*
* Bind the socket to the host specified in the principal name of the
* client, to ensure Server matching address of the client connection
* to host name in principal passed.
*/
 UserGroupInformation ticket = remoteId.getTicket();
 if (ticket != null && ticket.hasKerberosCredentials()) {
 KerberosInfo krbInfo =
remoteId.getProtocol().getAnnotation(KerberosInfo.class);
 if (krbInfo != null && krbInfo.clientPrincipal() != null) {
 String host =
 SecurityUtil.getHostFromPrincipal(remoteId.getTicket().getUserName());
 // If host name is a valid local address then bind socket to it
 InetAddress localAddr = NetUtils.getLocalInetAddress(host);
 if (localAddr != null) {
~ //this.socket.bind(new InetSocketAddress(localAddr, 0));
 }
 }
 }
 NetUtils.connect(this.socket, server, connectionTimeout);
 if (rpcTimeout > 0) {
pingInterval = rpcTimeout; // rpcTimeout overwrites pingInterval
 }
 this.socket.setSoTimeout(pingInterval);
 return;
```
换包后，renew token的步骤可以过了。
```
2020-07-20 18:31:51,144 INFO org.apache.hadoop.yarn.server.resourcemanager.security.DelegationTokenRenewer: Renewed delegation-token= [Kind: HDFS_DELEGATION_TOKEN, Service: 172.22.51.4:8020, Ident: (HDFS_DELEGATION_TOKEN token 11 for admin);exp=1595327511151; apps=[application_1595240758340_0001]], for [application_1595240758340_0001]
2020-07-20 18:32:11,221 INFO org.apache.hadoop.yarn.server.resourcemanager.security.DelegationTokenRenewer: Renewed delegation-token= [Kind: HDFS_DELEGATION_TOKEN, Service: 172.22.0.218:8020, Ident: (HDFS_DELEGATION_TOKEN token 9 for admin);exp=1595327531181; apps=[application_1595240758340_0001]], for [application_1595240758340_0001]
```
可以看到两个token都成功renew了。但是会有新的报错：
```
2020-07-20 18:59:11,028 WARN [main] org.apache.hadoop.mapred.YarnChild: Exception running child : java.io.IOException: Failed on local exception: java.io.IOException: org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS]; Host Details : local host is: "dualnic3/10.10.0.4"; destination host is: "tdh6-1":8020;
...
Caused by: org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS]
at org.apache.hadoop.security.SaslRpcClient.selectSaslClient(SaslRpcClient.java:182)
at org.apache.hadoop.security.SaslRpcClient.saslConnect(SaslRpcClient.java:422)
at org.apache.hadoop.ipc.Client$Connection.setupSaslConnection(Client.java:563)
at org.apache.hadoop.ipc.Client$Connection.access$1900(Client.java:376)
at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:743)
at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:739)
at java.security.AccessController.doPrivileged(Native Method)
at javax.security.auth.Subject.doAs(Subject.java:415)
at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:2197)
at org.apache.hadoop.ipc.Client$Connection.setupIOstreams(Client.java:738)
```
定位发现问题是这样的：
```
2020-07-20 18:59:11,009 INFO [main] org.apache.hadoop.ipc.Client: kkk UGI: admin (auth:SIMPLE)
2020-07-20 18:59:11,010 INFO [main] org.apache.hadoop.security.SaslRpcClient: kkk selectSaslClient: [method: "TOKEN"
mechanism: "DIGEST-MD5"
protocol: ""
serverId: "default"
challenge: "realm=\"default\",nonce=\"yMOfmLmJdGjcywR56CdL0Admm2nUxHqYvxM+RPua\",qop=\"auth\",charset=utf-8,algorithm=md5-sess"
, method: "KERBEROS"
mechanism: "GSSAPI"
protocol: "hdfs"
serverId: "tdh6-1"
]
2020-07-20 18:59:11,011 INFO [main] org.apache.hadoop.security.SaslRpcClient: kkk Get token info proto: interface org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolPB info: @org.apache.hadoop.security.token.TokenInfo(value=class org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenSelector)
2020-07-20 18:59:11,012 INFO [main] org.apache.hadoop.security.SaslRpcClient: kkk ugi.getTokens: [Kind: mapreduce.job, Service: 10.10.0.2:33602, Ident: (org.apache.hadoop.mapreduce.security.token.JobTokenIdentifier@5787f270), Kind: HDFS_DELEGATION_TOKEN, Service: 172.22.51.4:8020, Ident: (HDFS_DELEGATION_TOKEN token 13 for admin), Kind: HDFS_DELEGATION_TOKEN, Service: 10.10.0.4:8020, Ident: (HDFS_DELEGATION_TOKEN token 14 for admin), Kind: HDFS_DELEGATION_TOKEN, Service: 10.10.0.2:8020, Ident: (HDFS_DELEGATION_TOKEN token 14 for admin), Kind: HDFS_DELEGATION_TOKEN, Service: 172.22.0.218:8020, Ident: (HDFS_DELEGATION_TOKEN token 10 for admin), Kind: HDFS_DELEGATION_TOKEN, Service: ha-hdfs:nameservice1, Ident: (HDFS_DELEGATION_TOKEN token 14 for admin)]
2020-07-20 18:59:11,012 INFO [main] org.apache.hadoop.security.SaslRpcClient: kkk createSaslClient TOKEN: null
2020-07-20 18:59:11,012 INFO [main] org.apache.hadoop.security.SaslRpcClient: kkk createSaslClient KERBEROS: SIMPLE
```
token虽然renew是好了，但是token的Service还是172.22.0.218:8020。所以当需要访问目标集群的namenode tdh6-1:8020时，找不到合适的token——我理解这里会把tdh6-1解析成10.10.0.18，所以没有匹配的token。所以报错：Client cannot authenticate via:[TOKEN, KERBEROS]。

### 第二次workaround：配置hadoop.security.token.service.use_ip
配置hadoop.security.token.service.use_ip为false，这样token里的Service字段不再使用IP地址，而是使用hostname。 
这个需要同时修改client和server两端的配置，意思是同时修改TDH-Client和HDFS/Yarn里的core-site.xml。 
然而，一旦配置了这个参数，HDFS会尝试获取FQDN来生成用户的principal。 
家里的环境中，没有配置FQDN也没有配置DNS服务，所以获取不到。 
导致使用了IP地址来生成用户principal，类似yarn/192.xxx@BJLTDH。 
这样的用户principal在keytab中不存在，会直接导致hdfs和yarn启动失败，比如yarn的nodemanager的错误日志：
```
2020-07-27 10:26:14,113 INFO org.apache.hadoop.service.AbstractService: Service NodeManager failed in state STARTED; cause: org.apache.hadoop.yarn.exceptions.YarnRuntimeException: Failed NodeManager login
org.apache.hadoop.yarn.exceptions.YarnRuntimeException: Failed NodeManager login
at org.apache.hadoop.yarn.server.nodemanager.NodeManager.serviceStart(NodeManager.java:270)
at org.apache.hadoop.service.AbstractService.start(AbstractService.java:193)
at org.apache.hadoop.yarn.server.nodemanager.NodeManager.initAndStartNodeManager(NodeManager.java:496)
at org.apache.hadoop.yarn.server.nodemanager.NodeManager.main(NodeManager.java:543)
Caused by: java.io.IOException: Login failure for yarn/172.22.51.3@dualtest from keytab /etc/yarn1/conf/yarn.keytab: javax.security.auth.login.LoginException: Unable to obtain password from user
at org.apache.hadoop.security.UserGroupInformation.loginUserFromKeytab(UserGroupInformation.java:1232)
at org.apache.hadoop.security.SecurityUtil.login(SecurityUtil.java:276)
at org.apache.hadoop.security.SecurityUtil.login(SecurityUtil.java:237)
at org.apache.hadoop.yarn.server.nodemanager.NodeManager.doSecureLogin(NodeManager.java:136)
at org.apache.hadoop.yarn.server.nodemanager.NodeManager.serviceStart(NodeManager.java:268)
 ... 3 more
Caused by: javax.security.auth.login.LoginException: Unable to obtain password from user
```
问题很明显，因为yarn.keytab里，都是这样的：
```
# kubectl exec hadoop-yarn-nodemanager-yarn1-pod -- klist -kt /etc/yarn1/conf/yarn.keytab
Keytab name: FILE:/etc/yarn1/conf/yarn.keytab
KVNO Timestamp Principal
---- ------------------- ------------------------------------------------------
 0 07/17/2020 17:32:54 yarn/dualnic3@dualtest
 0 07/17/2020 17:32:54 yarn/dualnic3@dualtest
 ...
```
这里之所以会尝试使用FQDN，是因为配置hadoop.security.token.service.use_ip为false后，HDFS中会使用QualifiedHostResolver，而不再是默认的StandardHostResolver。 

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907162451.png)

其主要目的是为了避免同一个集群里的节点并没有使用同样的域名，而可能产生的安全问题：
```
The hostname is fully qualified. This avoids security issues if not 
all hosts in the cluster do not share the same search domains. It 
also prevents other hosts from performing unnecessary dns searches.
```

### 第三次workaround
配置hadoop.security.token.service.use_ip为false后，修改代码，仍然使用StandardHostResolver。 (直接写死成StandarHostResolver)
这时，distcp终于可以正常执行。但风险就是上面提到安全问题。
