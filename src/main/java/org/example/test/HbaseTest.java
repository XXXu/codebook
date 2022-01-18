package org.example.test;/*
package org.example.transwarp;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.mvel2.util.Make;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HbaseTest {
    protected  static Connection conn;
    private static Configuration conf;
    private static final String ZK_QUORUM = "hbase.zookeeper.quorum";
    private static final String ZK_CLIENT_PORT = "hbase.zookeeper.property.clientPort";
//    private static final String ZK_POS = "tdh620-7:2181,tdh620-8:2181,tdh620-9:2181";
    private static final String ZK_POS = "node01:2181,node03:2181,node04:2181";
    private static final String ZK_PORT_VALUE = "2181";

    static {
        conf = HBaseConfiguration.create();
        conf.addResource(new Path("./conf/hbase-site.xml"));
        conf.set(ZK_QUORUM, ZK_POS);
        conf.set(ZK_CLIENT_PORT, ZK_PORT_VALUE);
        //创建连接池
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addData(String tableName,String columnFamily,String column) throws IOException {
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        List<Put> putList = new ArrayList<Put>();
        String rowkey = "rowkey";
//        String value = "value";
        for (long i = 1; i < 11; i++) {
            Put put = new Put(Bytes.toBytes(rowkey + i));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(i));
            putList.add(put);
        }
        table.put(putList);
        table.setAutoFlushTo(true);
        table.flushCommits();
        table.close();
        conn.close();
    }

    public static void addData1(String tableName,String columnFamily,String column) throws IOException {

    }

    public static void createTable(String tableName) throws IOException {
        Admin admin = conn.getAdmin();
        HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
        // 给表描述对象增加列族
        tableDescriptor.addFamily(new HColumnDescriptor("bi"));
        admin.createTable(tableDescriptor);
        conn.close();
    }

    public static void scanData(String tableName,String startRow,String endRow) throws IOException, DecoderException {
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        scan.setReversed(false);
        scan.setStartRow(Bytes.toBytes(startRow));
        scan.setStopRow(Bytes.toBytes(endRow));
        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Cell cell : result.rawCells()) {
                String row = new String(CellUtil.cloneRow(cell));
                String family = new String(CellUtil.cloneFamily(cell));
                String Qualifier = new String(CellUtil.cloneQualifier(cell));
                Long value = Bytes.toLong(CellUtil.cloneValue(cell));
                System.out.println("row: " + row + " family: " + family + " Qualifier: "+Qualifier +" value: " + value);
            }
            System.out.println("----------------------");
        }
        table.close();
        conn.close();
    }

    public static void scanData(String tableName) throws IOException, DecoderException {
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        scan.setReversed(false);

        byte[] row = null;
        String s = null;

        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            for (Cell cell : result.rawCells()) {
                row=cell.getRow();
                s = new String(row);
                System.out.println(new String(row));
            }
            System.out.println("----------------------");
        }

        Get get = new Get(s.getBytes());

        Result result1 = table.get(get);
        System.out.println("size: "+result1.size());
        for (Cell cell : result1.rawCells()) {
            row=cell.getRow();
            System.out.println(new String(row));
            System.out.println(new String(CellUtil.cloneRow(cell)));
        }
        System.out.println("----------------------");



        table.close();
        conn.close();
    }

    public static void getData(String tableName,String rowkey) throws IOException, DecoderException {
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        Get get = new Get(rowkey.getBytes());
        Result result1 = table.get(get);
        System.out.println("size: "+result1.size());
        table.close();
        conn.close();
    }

    public static HyperbaseMetadata getHyperbaseMeta(Configuration conf, TableName tableName) throws Exception {
        ZooKeeperWatcher zkw = new ZooKeeperWatcher(conf, "HyperbaseAdmin", null);
        HyperbaseMetadataZookeeper zookeeper = new HyperbaseMetadataZookeeper(zkw);
        HyperbaseMetadata metadata = zookeeper.getMetadata(tableName);
//        System.out.println("---------------------");
//        System.out.println("metadata:"+metadata);
//        System.out.println("-----------------------");
//        System.out.println("metadata.getGlobalIndexes:"+metadata.getGlobalIndexes());
//        System.out.println("--------------------------");
//        Map<byte[], SecondaryIndex> map =  metadata.getGlobalIndexes();
//        for(byte[] bytes :map.keySet()){
////            System.out.println(new String (bytes));
////            System.out.println("-------------------------");
////        }
        zkw.close();
        zookeeper.close();
        return metadata;
    }

    public static void getIndexRowkey(HyperbaseMetadata metadata, String indexName) throws IOException {

        SecondaryIndex index = metadata.getGlobalIndexes().get(Bytes.toBytes(indexName));
        System.out.println("indexName: " + index.toString());
        Put put = new Put(Bytes.toBytes("025ad219ece1125a8f5a0e74e32676cb"));
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("update_time"), Bytes.toBytes("2020-12-20 22:34:34"));

        Put indexPut = index.genIndexPut(new Result(), put,
                HConstants.HYPERBASE_INDEX_FAMILY);
        System.out.println("=================");
        System.out.println(put);
        String indexRowkey = new String(indexPut.getRow());
        System.out.println("----"+indexRowkey);

        Get get = index.genIndexGet(Bytes.toBytes(indexRowkey), indexPut);
        System.out.println("*****");
        System.out.println(new String(get.getRow()));
    }

    public static void truncatet(String indexName) {

    }

    public static void main(String[] args) throws Exception {
//        addData("ttl_test111", "bi", "a");
//        scanData("ttl_test","rowkey1000","rowkey1007");
//        createTable("ttl_test111");
//        scanData("debug_test_timeindex");
//        getData("debug_test_timeindex","2020-12-20 22:34:34025ad219ece1125a8f5a0e74e32676cb\\x00\\x13\\x00");
        TableName tableName = TableName.valueOf("Student");
//        getIndexRowkey(getHyperbaseMeta(conf, tableName), "timeindex");

        scanData("Student", "0001", "0008");

    }

}

*/
