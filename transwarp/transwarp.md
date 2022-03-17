### WIFI:
* Transwarp-byod    PW:Transwarp@byod01
* Transwarp-guest    PW:Transwarp@guest01
* Transwarp-wireless    PW:Transwarp@wireless01

### maven
* 编译：hadoop
> mvn clean && mvn package -DskipTests  
编译完成之后，执行mvn test -Dtest=TestDFSAdminWithHA
执行测试之前先：export LICENSE_QUORUM=172.16.1.161:2181
mvn test -Dtest=DockerNetworkPolicyHostGWTest
* 编译hyperbase shaded包
> mvn -B clean package -f pom.xml -DskipTests -Dsource.skip=true -Dmaven.javadoc.skip=true -Prelease -Drat.skip=true
> mvn -B clean package -f pom.xml -DskipTests -Dsource.skip=true -Dmaven.javadoc.skip=true -Prelease -Drat.skip=true apache-rat:check -Drat.numUnapprovedLicenses=600
> mvn -B clean package -f pom.xml -DskipTests -Dsource.skip=true -Dmaven.javadoc.skip=true -Prelease -Drat.skip=true -Dlicense.skip=true -Drat.ignoreErrors=true
> mvn -B clean package -Pdist,native,docker-httpfs -Dsource.skip=true -Dmaven.javadoc.skip=true -DskipTests

* 打hbase镜像：
> mvn -B clean package -DskipTests -Dsource.skip=true -Dmaven.javadoc.skip=true -Pnative -Prelease -Drat.skip=true -Pdocker -DpullOnBuild=true 

### 进入inceptor命令：
* beeline -u  "jdbc:hive2://node123:10000/default"
* beeline -u "jdbc:hive2://172.22.33.1:10000/default" -n hive -p 123456

### kinit命令：
* kinit hdfs/hadoop5 -kt /etc/hdfs1/hdfs.keytab
* kinit yarn/hadoop5 -kt /etc/yarn1/yarn.keytab
* kinit hdfs/tos_tdcsys@TDCSYS.TDH -kt /etc/keytabs/keytab

### mapreduce 任务开启debug日志：
* mapreduce.map.log.level=DEBUG
* mapreduce.reduce.log.level=DEBUG
* yarn.app.mapreduce.am.log.level=DEBUG 

### client端开启debug日志
> https://wiki.transwarp.io/pages/viewpage.action?pageId=22677415

### namenode服务端开启debug
> /etc/hdfs1/conf/hadoop-hdfs-env.sh 加-Dhadoop.root.logger=DEBUG,RFA

### 开yarn服务端debug日志
> 修改镜像里的/bin/transwarp/yarn.sh -Dhadoop.root.logger=DEBUG,RFA -Dyarn.root.logger=DEBUG,RFA

### 性能命令
* egrep -o "Slow.*?(took|cost)" hadoop-hdfs-datanode*.log | sort | uniq -c
* iostat -p ALL -dtmxy 3
* pidstat -dl -T ALL

### git 
> 回退：git reset --soft HEAD^ （保留代码）git reset --hard HEAD^(不保留代码) git push origin 分支名 –-force
> 生成patch： git format-patch HEAD^ 

### 密码
* harbor 账号密码：xuxiaoxiao/Xinghuan123456
* jira wiki 账号密码：xiaoxiao.xu/Xinghuan123%$
* OA账号密码：xiaoxiao.xu/Xinghuan123%$

### scan
* scan 'fct_pf_vs_hour',{LIMIT=>5}
* scan 'hbase:meta',FILTER=>"PrefixFilter('tablename')"

### manager
> systemctl restart transwarp-manager