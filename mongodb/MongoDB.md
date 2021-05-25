## MongoDB

### 安装部署

#### 集群硬件信息

共四个节点：

| Hostname | IP           | 磁盘                             |
| -------- | ------------ | -------------------------------- |
| test515  | 192.168.51.5 | /mnt/disk3,/mnt/disk4,/mnt/disk5 |
| test516  | 192.168.51.6 | /mnt/disk3,/mnt/disk4,/mnt/disk5 |
| test517  | 192.168.51.7 | /mnt/disk3,/mnt/disk4,/mnt/disk5 |
| test518  | 192.168.51.8 | /mnt/disk3,/mnt/disk4,/mnt/disk5 |

#### mongodb集群拓扑

##### router，路由服务器

| 节点         | 端口  | 目录                      |
| ------------ | ----- | ------------------------- |
| 192.168.51.5 | 27017 | /mnt/disk2/mongodb/mongos |
| 192.168.51.6 | 27017 | /mnt/disk2/mongodb/mongos |
| 192.168.51.7 | 27017 | /mnt/disk2/mongodb/mongos |

##### configserver，配置服务器

| 节点         | 端口  | 目录                      |
| ------------ | ----- | ------------------------- |
| 192.168.51.5 | 27019 | /mnt/disk2/mongodb/config |
| 192.168.51.6 | 27019 | /mnt/disk2/mongodb/config |
| 192.168.51.8 | 27019 | /mnt/disk2/mongodb/config |

##### shardserver，分片服务器

| shard名 | 节点         | 端口  | 目录                    |
| ------- | ------------ | ----- | ----------------------- |
| shard0  | 192.168.51.5 | 27020 | /mnt/disk3/mongodb/data |
| shard0  | 192.168.51.6 | 27020 | /mnt/disk3/mongodb/data |
| shard0  | 192.168.51.7 | 27020 | /mnt/disk3/mongodb/data |
| shard1  | 192.168.51.5 | 27021 | /mnt/disk4/mongodb/data |
| shard1  | 192.168.51.6 | 27021 | /mnt/disk4/mongodb/data |
| shard1  | 192.168.51.8 | 27021 | /mnt/disk4/mongodb/data |
| shard2  | 192.168.51.5 | 27022 | /mnt/disk5/mongodb/data |
| shard2  | 192.168.51.7 | 27022 | /mnt/disk5/mongodb/data |
| shard2  | 192.168.51.8 | 27022 | /mnt/disk5/mongodb/data |
| shard3  | 192.168.51.6 | 27023 | /mnt/disk5/mongodb/data |
| shard3  | 192.168.51.7 | 27023 | /mnt/disk4/mongodb/data |
| shard3  | 192.168.51.8 | 27023 | /mnt/disk3/mongodb/data |

#### 安装步骤

1. 拷贝如下安装包到各个节点，并解压到/opt目录
   
   c8913b8c740293a07c10f627258e2392  transwarp-mongodb-4.2.14-new.tar.gz

   

2. 创建相关目录，四个节点都要执行：

   ```bash
   # mkdir -p /mnt/disk2/mongodb/{mongos,config}
   # mkdir -p /mnt/disk{3,4,5}/mongodb/data
   # mkdir -p /var/{run,log}/mongodb
   # mkdir -p /mnt/disk1/mongodb/arbiter
   ```

   

3. 生成keyfile

   ```bash
   # openssl rand -base64 90 > /opt/transwarp-mongodb/conf/mongodb-keyfile
   # chmod 700 /opt/transwarp-mongodb/conf/mongodb-keyfile
   ```

   

4. 创建mongod用户

   ```bash
   # /usr/sbin/groupadd -r mongod
   # /usr/sbin/useradd -M -r -g mongod -s /bin/false mongod
   # chown -R mongod:mongod /opt/transwarp-mongodb /mnt/disk2/mongodb/{mongos,config} /mnt/disk{3,4,5}/mongodb/data
    /var/{run,log}/mongodb /mnt/disk1/mongodb/arbiter
   ```

   

5. 启动config server，三个节点都要执行：

   ```bash
   # /opt/transwarp-mongodb/mongo_config.sh start
   ```

   启动成功后，在任一节点上执行副本集初始化：

   ```bash
   # /opt/transwarp-mongodb/bin/mongo --port 27019 --host 192.168.51.5
   > MongoDB server version: 4.2.14
   > cfg={
       _id:"TranswarpMongoConfigServer", 
       configsvr: true,
       members:[
           {_id:0, host:'192.168.51.5:27019'},
           {_id:1, host:'192.168.51.6:27019'}, 
           {_id:2, host:'192.168.51.8:27019'}
       ]};
   rs.initiate(cfg);
   验证： rs.status();
   ```

   

6. 启动shard server，四个节点都要执行：

   ```bash
   # /opt/transwarp-mongodb/mongo_shard*.sh start 
   ```

   启动成功后，同样需要在某一节点上执行副本集初始化：

   ```bash
   # /opt/transwarp-mongodb/bin/mongo --port 27020 --host 192.168.51.5
   > MongoDB server version: 4.2.14
   > cfg={
       _id:"TranswarpMongoShard0", 
       members:[
           {_id:0, host:'192.168.51.5:27020'},
           {_id:1, host:'192.168.51.6:27020'}, 
           {_id:2, host:'192.168.51.7:27020'}
       ]};
   rs.initiate(cfg);
   
   # /opt/transwarp-mongodb/bin/mongo --port 27021 --host 192.168.51.5
   > MongoDB server version: 4.2.14
   > cfg={
       _id:"TranswarpMongoShard1", 
       members:[
           {_id:0, host:'192.168.51.5:27021'},
           {_id:1, host:'192.168.51.6:27021'}, 
           {_id:2, host:'192.168.51.8:27021'}
       ]};
   rs.initiate(cfg);
   
   # /opt/transwarp-mongodb/bin/mongo --port 27022 --host 192.168.51.5
   > MongoDB server version: 4.2.14
   > cfg={
       _id:"TranswarpMongoShard2", 
       members:[
           {_id:0, host:'192.168.51.5:27022'},
           {_id:1, host:'192.168.51.7:27022'}, 
           {_id:2, host:'192.168.51.8:27022'}
       ]};
   rs.initiate(cfg);
   
   # /opt/transwarp-mongodb/bin/mongo --port 27023 --host 192.168.51.6
   > MongoDB server version: 4.2.14
   > cfg={
       _id:"TranswarpMongoShard3", 
       members:[
           {_id:0, host:'192.168.51.6:27023'},
           {_id:1, host:'192.168.51.7:27023'}, 
           {_id:2, host:'192.168.51.8:27023'}
       ]};
   rs.initiate(cfg);
   ```

   > 问题，这样的添加方式，是否会影响到选举？
   >
   > 如果前三个shard中，192.168.51.5都是primary，那么需要调整下顺序

7. 启动mongos server，三个节点都要执行：

   ```bash
   # /opt/transwarp-mongodb/mongo_mongos.sh start
   ```

   启动成功后，接入其中一个，添加分片：

   ```bash
   # /opt/transwarp-mongodb/bin/mongo --port 27017 --host 192.168.51.5
   mongos> MongoDB server version: 4.2.14
   mongos> sh.addShard("TranswarpMongoShard0/192.168.51.5:27020")
   mongos> sh.addShard("TranswarpMongoShard1/192.168.51.5:27021")
   mongos> sh.addShard("TranswarpMongoShard2/192.168.51.5:27022")
   mongos> sh.addShard("TranswarpMongoShard3/192.168.51.6:27023")
   ```

## mongodb用户权限管理配置
mongodb用户权限列表

| 角色 | 含义 |
| ------- | ------- |
| read  | 允许用户读取指定数据库 |
| readWrite  | 允许用户读写指定数据库 |
| dbAdmin  | 允许用户在指定数据库中执行管理函数，如索引创建、删除，查看统计或访问system.profile |
| userAdmin  | 允许用户向system.users集合写入，可以在指定数据库里创建、删除和管理用户 |
| clusterAdmin  | 只在admin数据库中可用，赋予用户所有分片和复制集相关函数的管理权限 |
| readAnyDatabase  | 只在admin数据库中可用，赋予用户所有数据库的读权限 |
| readWriteAnyDatabase  | 只在admin数据库中可用，赋予用户所有数据库的读写权限 |
| userAdminAnyDatabase  | 只在admin数据库中可用，赋予用户所有数据库的userAdmin权限 |
| dbAdminAnyDatabase  | 只在admin数据库中可用，赋予用户所有数据库的dbAdmin权限 |
| root  | 只在admin数据库中可用，超级账号，超级权限 |

### mongodb用户使用
#### 创建DB管理用户
mongodb有一个用户管理机制，简单描述为，有一个管理用户组，这个组的用户是专门为管理普通用户而设的，暂且称之为管理员。  
管理员通常没有数据库的读写权限，只有操作用户的权限, 因此我们只需要赋予管理员userAdminAnyDatabase角色即可。
另外管理员账户必须在admin数据库下创建，3.0版本后没有admin数据库，但我们可以手动use一个。注：use命令在切换数据库时，
如果切换到一个不存在的数据库，MongodDB会自动创建该数据库。

##### 1.1 切换到admin库
管理员需要在admin数据库下创建，所以我们需要切换到admin数据库。
```
mongos> use admin
switched to db admin
```
##### 1.2 查看admin中的用户
可以通过db.system.users.find()函数来查看admin库中的所有用户信息。

```
mongos> db.system.users.find()
```

##### 1.3 db.createUser 函数

```
db.createUser({
    user: "<name>",
    pwd: "<cleartext password>",
    customData: { <any information> },
    roles: [
        { role: "<role>", db: "<database>" } | "<role>",
        ...
    ]
});
```

1）user:新建用户名。

2）pwd:新建用户密码。

3）customData:存放一些用户相关的自定义数据，该属性也可忽略。

4）roles:数组类型，配置用户的权限。

##### 1.4 创建管理员用户
我们现在需要在admin库中创建一个名为admin的管理员用户，密码为admin。

```bash
   use admin
   db.createUser({
       user:'admin',pwd:'admin',
       roles:[
           {role:'clusterAdmin',db:'admin'},
           {role:'userAdminAnyDatabase',db:'admin'},
           {role:'dbAdminAnyDatabase',db:'admin'},
           {role:'readWriteAnyDatabase',db:'admin'}
   ]})
```

   当前admin用户具有集群管理权限、所有数据库的操作权限。
   需要注意的是，在第一次创建用户之后，localexception不再有效，接下来的所有操作要求先通过鉴权。

```bash
use admin
db.auth('admin','admin')
```


##用keyfile开启集群认证
mongod.conf配置文件中：
```
security:
  keyFile: /opt/transwarp-mongodb/conf/mongodb-keyfile
  authorization: enabled
```
mongos.conf配置文件中也得加上：
```
security:
  keyFile: /opt/transwarp-mongodb/conf/mongodb-keyfile
```
> config进程和shard进程都要重启
> mongos比mongod少了authorization：enabled的配置。原因是，副本集加分片的安全认证需要配置两方面的，副本集各个节点之间使用内部身份验证，用于内部各个mongo实例的通信，只有相同keyfile才能相互访问。所以都要开启keyFile: /opt/transwarp-mongodb/conf/mongodb-keyfile
>  然而对于所有的mongod，才是真正的保存数据的分片。mongos只做路由，不保存数据。所以所有的mongod开启访问数据的授权authorization:enabled。这样用户只有账号密码正确才能访问到数据



   

9. 检查集群状态

   ```bash
   mongos> sh.status()
   ```

   mongostat命令：

   ```bash
   mongostat
   ```


11. 测试数据操作

    ```bash
    use appdb
    
    # 创建用户并启用分片
    db.createUser({user:'appuser',pwd:'AppUser@01',roles:[{role:'dbOwner',db:'appdb'}]})
    sh.enableSharding("appdb")
    
    # 创建集合并执行分片初始化
    db.createCollection("book")
    db.device.ensureIndex({createTime:1})
    sh.shardCollection("appdb.book", {bookId:"hashed"}, false, { numInitialChunks: 4} )
    
    # 写入数据，观察chunks的分布
    var cnt = 0;
    for(var i=0; i<10; i++){
        var dl = [];
        for(var j=0; j<10; j++){
            dl.push({
                    "bookId" : "BBK-" + i + "-" + j,
                    "type" : "Revision",
                    "version" : "IricSoneVB0001",
                    "title" : "Jackson's Life",
                    "subCount" : 10,
                    "location" : "China CN Shenzhen Futian District",
                    "author" : {
                          "name" : 50,
                          "email" : "RichardFoo@yahoo.com",
                          "gender" : "female"
                    },
                    "createTime" : new Date()
                });
          }
          cnt += dl.length;
          db.book.insertMany(dl);
          print("insert ", cnt);
    }
    
    # 查看shard分布
    db.book.getShardDistribution()
    ```
    
## 添加开机自启
```
touch /usr/lib/systemd/system/mongod-cluster.service
chmod 755 /usr/lib/systemd/system/mongod-cluster.service
内容见mongodb/auto目录下
systemctl daemon-reload
systemctl start mongod-cluster.service
systemctl enable mongod-cluster.service




touch /usr/lib/systemd/system/mongod-config.service
touch /usr/lib/systemd/system/mongod-shards.service
touch /usr/lib/systemd/system/mongod-router.service

chmod 755 /usr/lib/systemd/system/mongod-config.service
chmod 755 /usr/lib/systemd/system/mongod-shards.service
chmod 755 /usr/lib/systemd/system/mongod-router.service

sudo systemctl daemon-reload

sudo systemctl start mongod-config.service
sudo systemctl start mongod-shards.service
sudo systemctl start mongod-router.service

sudo systemctl enable mongod-config.service
sudo systemctl enable mongod-shards.service
sudo systemctl enable mongod-router.service

```




    

