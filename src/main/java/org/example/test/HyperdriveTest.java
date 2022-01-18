package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hyperbase.client.HyperbaseAdmin;
import org.apache.hadoop.hyperbase.client.HyperbaseHTable;
import org.apache.hadoop.hyperbase.datatype.serde.HPut;
import org.apache.hadoop.hyperbase.metadata.HyperbaseMetadata;
import org.apache.hadoop.hyperbase.util.SchemaUtil;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 drop table if exists testhyperdrive1;
 create table testhyperdrive1(
 key string,
 id string,
 name string,
 age string
 )
 stored by 'io.transwarp.hyperdrive.HyperdriveStorageHandler'
 with serdeproperties('hbase.columns.mapping'=':key,f:a0,f:a1,f:a2')
 TBLPROPERTIES('hbase.table.name'='testhyperdrive1');

 DROP INDEX testhyperdrive1_index1 ON testhyperdrive1;

 CREATE GLOBAL INDEX testhyperdrive1_index1 ON testhyperdrive1 (id(10),name(5),age(3));

 client_insert 'testhyperdrive1','on'

 set ngmr.exec.mode=local;
 select * from testhyperdrive1 where id = '1' and name = 'lk' and age not in ('26');
 */

public class HyperdriveTest {
    private Configuration conf;
    private HyperbaseAdmin admin;
    private HyperbaseHTable hyperbaseHTable;
    private HyperbaseMetadata hyperbaseMetadata;
    private static final byte[] fam1 = Bytes.toBytes("f");
    private static final byte[] q_1 = Bytes.toBytes("a0");
    private static final byte[] q_2 = Bytes.toBytes("a1");
    private static final byte[] q_3 = Bytes.toBytes("a2");

    public HyperdriveTest(String tableName) throws IOException, KeeperException, InterruptedException {
        this.conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum","jiujiu-tdh-70,jiujiu-tdh-71,jiujiu-tdh-72");
        conf.set("hbase.master","jiujiu-tdh-70:60010,jiujiu-tdh-71:60010,jiujiu-tdh-72:60010");
        conf.set("zookeeper.znode.parent", "/hyperbase1");
        conf.set("hbase.client.retries.number","35");
        conf.set("hbase.client.pause", "100");
        this.admin = new HyperbaseAdmin(this.conf);
        this.hyperbaseHTable = new HyperbaseHTable(this.conf, TableName.valueOf(tableName));
        this.hyperbaseMetadata = this.admin.getTableMetadata(TableName.valueOf(tableName));
    }

    public void put(List<String[]> values) throws IOException {
        List<Put> puts = new ArrayList<Put>();
        HPut hput;
        for (String[] strings : values) {
            hput = HPut.BuilderPut(strings[0], hyperbaseMetadata);
            hput.add(fam1, q_1, SchemaUtil.genObjectFromPuts(hyperbaseMetadata, strings[1], fam1, q_1));
            hput.add(fam1, q_2, SchemaUtil.genObjectFromPuts(hyperbaseMetadata, strings[2], fam1, q_2));
            hput.add(fam1, q_3, SchemaUtil.genObjectFromPuts(hyperbaseMetadata, strings[3], fam1, q_3));
            puts.add(hput.getPut());
        }

        this.hyperbaseHTable.put(puts);
    }

    public void close() throws IOException {
        this.admin.close();
        this.hyperbaseHTable.close();
    }

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        String tableName = "testhyperdrive5";
        List<String[]> values = new ArrayList<String[]>();
        HyperdriveTest stringRow = new HyperdriveTest(tableName);
        String[] value1 = {"aaa","1","lk","24"};
        String[] value2 = {"aaa","1","lk","25"};
        values.add(value1);
        values.add(value2);

        stringRow.put(values);
        stringRow.close();
    }
}
