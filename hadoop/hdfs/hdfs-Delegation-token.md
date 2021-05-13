# Hadoop Delegation Tokens
## Hadoop Security 简单介绍
Hadoop最初的实现中并没有认证机制，这意味着存储在Hadoop中的数据很容易泄露。在2010年，安全特性被加入Hadoop（HADOOP-4487），主要实现下面两个目标：
* 拒绝未授权的操作访问HDFS中的数据。
* 在实现1的基础上，避免太大的性能损耗。
为了实现第一个目标，我们需要保证：
* 任何一个客户端要访问集群必须要经过认证，以确保它就是自己声称的身份。
* 集群中任何服务器，需要被认证为集群中的一部分。
为了实现这个目标，Kerberos被选作基础的认证服务。其它的机制，如：Delegation Token, Block Access Token, Trust等被加入当做Kerberos的补充。特别是Delegation Token机制被引入，其主要用以实现第二个目标（详情见下一节）。下图简要描述了Kerberos and Delegation Tokens在HDFS中应用。（其它服务类似）

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907111739.png)

在上面的样例中，会涉及到下面几条认证流程：
1. 用户（joe）利用Kerberos来访问NameNode.
2. 用户（joe）提交的分布式任务用joe的Delegation Tokens来访问NameNode。这是本文接下来的重点。
3. HDFS中的DataNode通过Kerberos和NameNode进行交互。
4. 用户及其提交的任务通过Block Access Tokens来访问DataNodes。

## 什么是Delegation Token？
理论上，虽然可以只使用Kerberos实现认证机制，但这会有一定问题，尤其是应用在像Hadoop这样的分布式系统中。想像一下，对于每个MapReduce任务，如果所有的任务都需要使用TGT (Ticket Granting Ticket)通过Kerberos来进行认证，KDC（Kerberos Key Distribution Center）将很快成为系统瓶颈。下图中的红线说明该问题：一个任务中可能涉及到成千个节点之间的通信，从而导致KDC网络拥堵。事实上，如果集群规模较大，这无意间就对KDC执行了一次DDos（distributed denial of service attack）攻击。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907112811.png)

因此，Delegation Tokens作为Kerberos的一个补充，实现了一种轻量级的认证机制。Kerberos是三方认证协议，而Delegation Tokens只涉及到两方。
Delegation Tokens的认证过程如下：
1. client通过Kerberos与Server完成认证，并从server获取相应的Delegation Tokens。
2. client与server之间后续的认证都是通过Delegation Tokens，而不进过Kerberos。
client可以把Delegation Tokens传递给其它的服务（如：YARN），如此一来，这些服务（如：MapReduce任务）以client身份进行认证。换句话说，client可以将身份凭证"委托"给这些服务。Delegation Tokens有一个过期时间的概念，需要周期性的更新以保证其有效性。但是，它也不能无限制的更新，这由最大生命周期控制。此外，在Delegation Token过期前也被取消。
Delegation Tokens可以避免分发Kerberos TGT 或 keytab，而这些信息一旦泄露，将获得所有服务的访问权限。另一方面，每个Delegation Token与其关联服务严格的绑定在一起，且最终会过期。所以，即使Delegation Token泄露，也不会造成太大损失。此外，Delegation Token使身份凭证的更新更加轻量化。这是因为Token更新过程只涉及到"更新者"和相关服务。token本身并不会改变，所以已经使用token的各个组件并不需要更新。
考虑到高可用性，Delegation Tokens会被server进行持久化。HDFS NameNode将Delegation Tokens持久化到元数据中（又称为：fsimage and edit logs），KMS会将其以ZNodes形式持久化到ZooKeeper中。即使服务重启或故障切换，Delegation Tokens也会一直可用。
server和client在处理Delegation Tokens时会有不同的职责。下面两节内容作详细说明。

## server端的Delegation Tokens
server端（图2中的HDFS NN和KMS）主要负责：
1. 发布Delegation Tokens，并保存用以验证
2. 响应更新Delegation Tokens请求。
3. 当client端执行删除操作或token过期时，移除Token。
4. 通过验证client提供的Tokens和server端存储的token是否一致，来对client进行认证。

Hadoop中Delegation Tokens的生成和验证主要依赖于HMAC机制。Delegation Token主要由两部分组成：public部分和private部分，在Server端以<key，value>形式存储在hashmap中，其中public部分作为key，private部分作为value。

public部分信息用于token识别，以identifier对象形式存在。主要组成如下：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907140204.png)

private部分信息是由AbstractDelegationTokenSecretManager中的DelegationTokenInformation类来表示，主要组成如下：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907140411.png)

注意Table1中的Master key ID，其存储于server端，并用于生成每个Delegation Token。该ID会定期更新，且一直只存在于server端。Server同样可以配置更新周期（renew-interval，默认24小时），以及Delegation Token的过期时间。过期的Delegation Tokens不能用于认证，且Server端专门有一个后台线程用于将过期token移除。
只有Delegation Token的renewer可以在token过期前进行更新操作。每次更新过后，token的过期时间会延长一个更新周期（renew-interval），直到token达到最大生命周期（默认7天）。

## client端的Delegation tokens
Client主要负责：
1. 从server端请求一个新的Delegation Tokens，请求同时可以指定token的更新者（renewer）。
2. 更新Delegation Tokens（如果client将自己指定为renewer），亦或请求别的组件更新token（指定的renewer）
3. 向server发送取消Delegation Tokens的请求。
4. 提供Delegation Tokens供server进行认证。
client端Token主要包含以下信息：

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907141322.png)

## Example: Delegation Tokens的生命周期
我们现在已经知道Delegation Token是什么了，下面来探究下其在实际场景中如何使用。下图展示的是一个运行一个典型应用的认证流程，先通过YARN提交作业，然后将任务分发到各个worker节点执行。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907142218.png)

简单起见，此处将忽略Kerberos认证和Task分发流程，图中通常有5个步骤：
1. client希望在集群中运行一个job，它分别从NameNode和KMS获取HDFS Delegation Token和KMS Delegation Token
2. client将作业提交到YARN资源管理器（RM），同时提交的还有step1中获取的Delegation Token以及ApplicationSubmissionContext
3. YARN RM通过更新操作来核实接收的Token，随后，YARN启动job，并将其和Delegation Tokens一同分发到各个worker节点上
4. 每个工作节点中的Task利用这些Token来进行认证，比如：需要访问HDFS上数据时，使用HDFS Delegation Token进行认证。需要解密HDFS加密区的文件时，使用KMS Delegation Token。
5. job结束后，RM则取消该job的Delegation Tokens
注意：有一个步骤没有在上图中画出，就是RM会跟踪每个Delegation Token的过期时间，并在即将过期时（达到过期时间的90%）更新Delegation Token。此外，RM是对每个Token进行跟踪，而不是按照Token种类。这样即使每个token的更新间隔（renewal intervals）不同，也能正确地被更新。toekn更新者（renewer）是通过Java ServiceLoader机制实现，因此RM不需要知道Token的种类。

## InvalidToken错误
到目前为止，我们已经了解什么是Delegation Tokens以及其如何在运行作业时使用。下面让我们来看几个典型的Delegation Token相关的错误。
### Token is expired
有的时候，应用失败是由于AuthenticationException，其中包含是InvalidToken异常。异常信息显示"Token is expired"，猜测下这是为什么？

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907150713.png)

### Token can not be found in cache
还有的时候，应用失败也是由InvalidToken造成，而其中的异常日志显示"token can’t be found in cache"，猜测下这又是为什么？

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907150905.png)

### 解释
上述两个错误都是由一个共同的原因引起的：被用于认证的token过期了，因此无法使用。第一个异常日志中可以明确看到token过期信息，因为token依然存在于server端。因此，当server验证token有效性的时候，会因token过期而验证失败，抛出“token is expired”异常。现在你可以猜测下第二个异常如何发生的？在"server端的Delegation Tokens"这一小节，我们提到server端有一个后台线程用于移除过期的token。因此，当某个过期token被移除后，server端在进行token验证的过程中就无法找到该token，即抛出"token can’t be found"异常。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907151106.png)

需要注意的是，当一个token被明确的撤销后，那么该token立即会被移除。因此，对于被撤销的token，错误日志只可能是"token can’t be found"。为了进一步debug这些errors，获取client和server端日志中的token序列号（样例中的“sequenceNumber=7” or “sequenceNumber=8”）是非常有必要的，你应该可以看到和token相关的事件信息，如：creations,renewals,cancelations等。

## 长时间运行的应用
至此，你已经基本了解Hadoop Delegation Tokens。但是还有一点未提及。我们知道Delegations Tokens超过其最大生命周期后无法被更新，那么如果一个任务需要运行时间比token的最大生命周期还要长怎么办？
## 实现一个Token Renewer
首先需要了解更新token操作都做了什么，最好的方式学习Yarn RM的DelegationTokenRenewer代码。
这个类中关键点如下：
1. DelegationTokenRenewer用以管理所有的token，并通过其内部的一个线程池来更新token，还有一个线程用来取消token。更新操作发生在过期时间的90%。取消操作有一个延迟（30秒）用以避免竞争。
2. 每个token的过期时间被单独管理，并通过调用renew() API来获取，该方法返回一个过期时间。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907154334.png)

这就是为什么YARN RM在接收到token后立即更新它，就是为了获取该token的过期时间。
3. token的最大生命周期是从token的identifier中解码得到，调用其 getMaxDate() API。identifier中的其它字段也可以通过这个API得到。

![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200907154921.png)

4. 根据2和3可知，server端不需要通过读取配置文件来确定token过期时间和最大生命周期。由于server的配置可能随时变动，client不应该依赖于它。

## 最大生命周期后如何处理Token
token的更新者直到最大生命周期才会执行更新操作。最大生命周期后，作业就会失败。如果你的作业是一个长耗时作业，你应该考虑利用YARN documentation about long-lived services中描述的机制，或者为token更新者增加一些自定义逻辑，用来在现有tokens即将过期时，重新获取delegation tokens
## Token的其它用处
恭喜！你已经浏览完delegation tokens的概念和细节，不过还有一些相关内容上文没有提及，下面简单介绍下.
* Delegation Tokens在其它服务中的应用，如：Apache Oozie, Apache Hive, and Apache Hadoop’s YARN RM，这些服务都是用Delegation Tokens认证机制。
* Block Access Token：client在访问HDFS上的文件时，首先需要和NameNode通信，获取该文件的Block位置信息。然后直接和DataNode通信访问这些Blocks。访问权限的检查是在NameNode端完成。但是，client直接访问DataNode中的Block数据，这也需要做权限认证。Block Access Tokens就是用来解决这一问题。Block Access Tokens是由NameNode发布给Client，然后由Client发送给DataNode。Block Access Tokens的生命周期很短（默认10小时），并且不能更新。也就意味着如果一个Block Access Token过期，那么client必须重新获取一个新的token。
* Authentication Token：我们已经介绍很多的内容都是关于Delegation Tokens。Hadoop中还有一种机制称为：Authentication Token，主要目的是实现一种更轻量级、高可扩展的认证方案。类似于client和server端的cookie。Authentication Tokens由server端授权，且无法更新以及仿冒他人。和Delegation Tokens不同的是，server端不需要单独存储Authentication Tokens

# 总结
Delegation Tokens在Hadoop生态中发挥着非常重要的作用，你现在应该理解设计Delegation Tokens的初衷、如何使用以及为什么这么使用。这些内容在开发和debug时尤为重要。
