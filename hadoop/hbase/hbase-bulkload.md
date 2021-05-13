## 概述
在进行数据传输中，批量加载数据到HBase集群有多种方式，比如通过HBase API进行批量写入数据、使用Sqoop工具批量导数到HBase集群、使用MapReduce
批量导入等。这些方式，在导入数据的过程中，如果数据量过大，可能耗时会比较严重或者占用HBase集群资源较多（如磁盘IO、HBase Handler数等）。
## 内容
在使用BulkLoad之前，我们先来了解一下HBase的存储机制。HBase存储数据其底层使用的是HDFS来作为存储介质，HBase的每一张表对应的HDFS目录上的一个文件夹，文件夹名以HBase表进行命名（如果没有使用命名空间，则默认在default目录下），在表文件夹下存放在若干个Region命名的文件夹，Region文件夹中的每个列簇也是用文件夹进行存储的，每个列簇中存储就是实际的数据，以HFile的形式存在。路径格式如下：
`/hbase/data/default/<tbl_name>/<region_id>/<cf>/<hfile_id>`
### 实现原理
按照HBase存储数据按照HFile格式存储在HDFS的原理，使用MapReduce直接生成HFile格式的数据文件，然后在通过RegionServer将HFile数据文件移动到相应的Region上去。流程如下图所示：
![](https://raw.githubusercontent.com/XXXu/imgbed/master/img/20200707171527.png)
### 生成HFile文件
HFile文件的生成，可以使用MapReduce来进行实现，将数据源准备好，上传到HDFS进行存储，然后在程序中读取HDFS上的数据源，进行自定义封装，组装RowKey，然后将封装后的数据在回写到HDFS上，以HFile的形式存储到HDFS指定的目录中。实现代码如下：
```
public class GemeratorHFile2 {
    static class HFileImportMapper2 extends Mapper<LongWritable, Text, ImmutableBytesWritable, KeyValue> {
        
        protected final String CF_KQ = "cf";

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            System.out.println("line : " + line);
            String[] datas = line.split(" ");
            String row = new Date().getTime() + "_" + datas[1];
            ImmutableBytesWritable rowkey = new ImmutableBytesWritable(Bytes.toBytes(row));
            KeyValue kv = new KeyValue(Bytes.toBytes(row), this.CF_KQ.getBytes(), datas[1].getBytes(), datas[2].getBytes());
            context.write(rowkey, kv);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("<Usage>Please input hbase-site.xml path.</Usage>");
            return;
        }
        Configuration conf = new Configuration();
        conf.addResource(new Path(args[0]));
        conf.set("hbase.fs.tmp.dir", "partitions_" + UUID.randomUUID());
        String tableName = "person";
        String input = "hdfs://nna:9000/tmp/person.txt";
        String output = "hdfs://nna:9000/tmp/pres";
        System.out.println("table : " + tableName);
        HTable table;
        try {
            try {
                FileSystem fs = FileSystem.get(URI.create(output), conf);
                fs.delete(new Path(output), true);
                fs.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Connection conn = ConnectionFactory.createConnection(conf);
            table = (HTable) conn.getTable(TableName.valueOf(tableName));
            Job job = Job.getInstance(conf);
            job.setJobName("Generate HFile");

            job.setJarByClass(GemeratorHFile2.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapperClass(HFileImportMapper2.class);
            FileInputFormat.setInputPaths(job, input);
            FileOutputFormat.setOutputPath(job, new Path(output));

            HFileOutputFormat2.configureIncrementalLoad(job, table);
            try {
                job.waitForCompletion(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
```
### 使用BulkLoad导入到HBase
然后，在使用BulkLoad的方式将生成的HFile文件导入到HBase集群中，这里有2种方式。一种是写代码实现导入，另一种是使用HBase命令进行导入。
#### 代码实现导入
通过LoadIncrementalHFiles类来实现导入，具体代码如下：
```
public class BulkLoad2HBase {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("<Usage>Please input hbase-site.xml path.</Usage>");
            return;
        }
        String output = "hdfs://cluster1/tmp/pres";
        Configuration conf = new Configuration();
        conf.addResource(new Path(args[0]));
        HTable table = new HTable(conf, "person");
        LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
        loader.doBulkLoad(new Path(output), table);
    }
    
}
```
#### 使用HBase命令进行导入
先将生成好的HFile文件迁移到目标集群（即HBase集群所在的HDFS上），然后在使用HBase命令进行导入，执行命令如下：
```
# 先使用distcp迁移hfile
hadoop distcp -Dmapreduce.job.queuename=queue_1024_01 -update -skipcrccheck -m 10 /tmp/pres hdfs://nns:9000/tmp/pres
   
# 使用bulkload方式导入数据
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /tmp/pres person
```
## 总结
将生成HFile文件和使用BulkLoad方式导入HFile到HBase集群的步骤进行了分解，实际情况中，可以将这两个步骤合并为一个，实现自动化生成与HFile自动导入。如果在执行的过程中出现RpcRetryingCaller
的异常，可以到对应RegionServer节点查看日志信息，这里面记录了出现这种异常的详细原因。