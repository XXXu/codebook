package org.example.test;

import com.google.protobuf.ServiceException;
import org.apache.commons.codec.DecoderException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class HyperbaseTest {
    private static Connection conn;
    private static final String ZK_QUORUM = "hbase.zookeeper.quorum";
    private static final String ZK_CLIENT_PORT = "hbase.zookeeper.property.clientPort";
    private static final String ZK_POS = "hadoop518:2181,hadoop516:2181,hadoop517:2181";
    private static final String ZK_PORT_VALUE = "2181";
    private static Configuration conf;

    public static void main(String[] args) throws IOException, ServiceException, DecoderException {
        System.setProperty("java.security.krb5.conf", "./hyperbaseconf/krb5.conf");
        // jaas.conf keyTab="/etc/hyperbase1/conf/hyperbase.keytab" 这个路径要改成本地的，默认是服务器的路径
        System.setProperty("java.security.auth.login.config", "./hyperbaseconf/jaas.conf");
        conf = HBaseConfiguration.create();
        conf.addResource(new Path("./conf2/hbase-site.xml"));
        conf.addResource(new Path("./conf2/hdfs-site.xml"));
        conf.addResource(new Path("./conf2/core-site.xml"));
        conf.set(ZK_QUORUM, ZK_POS);
        conf.set(ZK_CLIENT_PORT, ZK_PORT_VALUE);
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hbase/hadoop518@TDH", "./hyperbaseconf/hyperbase.keytab");
        //创建连接池
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanData("forpoc");
//        HBaseAdmin.checkHBaseAvailable(conf);
//        Admin admin = conn.getAdmin();
//        HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("forpoc"));
        // 给表描述对象增加列族
//        tableDescriptor.addFamily(new HColumnDescriptor("cf"));
//        admin.createTable(tableDescriptor);
//        conn.close();
    }

    public static void scanData(String tableName) throws IOException, DecoderException {
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            for (Cell cell : result.rawCells()) {
                String row = new String(CellUtil.cloneRow(cell));
                String family = new String(CellUtil.cloneFamily(cell));
                String Qualifier = new String(CellUtil.cloneQualifier(cell));
                String value = new String(CellUtil.cloneValue(cell));

                System.out.println("row: " + row + " family: " + family + " Qualifier: "+Qualifier +" value: " + value);
            }
            System.out.println("----------------------");
        }
        table.close();
        conn.close();
    }
}
