package org.example.test;/*
package org.example.transwarp;

import com.google.inject.internal.cglib.core.$ObjectSwitchCallback;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.hbase.HBaseSerDe;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hyperbase.client.HyperbaseAdmin;
import org.apache.hadoop.hyperbase.metadata.HyperbaseMetadata;
import org.apache.hadoop.hyperbase.metadata.zookeeper.HyperbaseMetadataZookeeper;
import org.apache.thrift.TException;
import org.apache.zookeeper.KeeperException;
import transwarp.org.junit.Before;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HyperbaseMetaDataUtil {
    private static final String ZK_QUORUM = "hbase.zookeeper.quorum";
    private static final String ZK_CLIENT_PORT = "hbase.zookeeper.property.clientPort";
    private static final String ZK_POS = "tdh620-7:2181,tdh620-8:2181,tdh620-9:2181";
    private static final String ZK_PORT_VALUE = "2181";
    private static Configuration conf;
    private static HyperbaseMetadataZookeeper zookeeper = null;

    */
/*private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://tdh620-7:10000/default";

    private static Connection con = null;
    private static Statement state = null;
    private static ResultSet res = null;*//*


    final static public String DEFAULT_PREFIX = "default.";

    public static void main(String[] args) throws IOException, KeeperException, SQLException, ClassNotFoundException, TException, SerDeException {
        conf = HBaseConfiguration.create();
        conf.addResource(new Path("./conf/hbase-site.xml"));
        conf.addResource(new Path("./conf/hdfs-site.xml"));
        conf.addResource(new Path("./conf/core-site.xml"));
        conf.set(ZK_QUORUM, ZK_POS);
        conf.set(ZK_CLIENT_PORT, ZK_PORT_VALUE);

        ZooKeeperWatcher zkw = new ZooKeeperWatcher(conf, "HyperbaseAdmin", null);

        zookeeper = new HyperbaseMetadataZookeeper(zkw);

        HiveConf hiveConf = new HiveConf();
        hiveConf.addResource(new Path("./conf/hive-site.xml"));
        HiveMetaStoreClient storeClient = new HiveMetaStoreClient(hiveConf);

        Table table = storeClient.getTable("default", "test766");

        boolean isExternal = MetaStoreUtils.isExternalTable(table);
        System.out.println("isExternal:"+isExternal);

        System.out.println(table.getSd().getLocation());


        String tableName = getHBaseTableName(table);

        Map<String, String> serdeParam = table.getSd().getSerdeInfo().getParameters();
        String hbaseColumnsMapping = serdeParam.get(HBaseSerDe.HBASE_COLUMNS_MAPPING);
        if (hbaseColumnsMapping == null && !isExternal) {
            // create default mapping
            int size = table.getSd().getCols().size();
            StringBuilder sb = new StringBuilder(HBaseSerDe.HBASE_KEY_COL);
            for(int i=1; i<size; ++i) {
                sb.append(",f:q").append(i);
            }
            hbaseColumnsMapping = sb.toString();
            serdeParam.put(HBaseSerDe.HBASE_COLUMNS_MAPPING, hbaseColumnsMapping);
        }
        List<HBaseSerDe.ColumnMapping> columnsMapping = null;

        columnsMapping = HBaseSerDe.parseColumnsMapping(hbaseColumnsMapping);


        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        Set<String> uniqueColumnFamilies = new HashSet<String>();

        for (HBaseSerDe.ColumnMapping colMap : columnsMapping) {
            if (!colMap.hbaseRowKey) {
                uniqueColumnFamilies.add(colMap.familyName);
            }
        }

        for (String columnFamily : uniqueColumnFamilies) {
            HColumnDescriptor hcd = new HColumnDescriptor(Bytes.toBytes(columnFamily));
            hcd.setDataBlockEncoding(DataBlockEncoding.PREFIX);
            hcd.setCompressionType(Compression.Algorithm.SNAPPY);
            tableDesc.addFamily(hcd);
        }

        // create hyperbase metadata in zk

        HyperbaseMetadata metadata = new HyperbaseMetadata();
        metadata.setSchema(null);
        if (HyperbaseAdmin.isThemisEnable(tableDesc)) {
            metadata.setIsTransactionTable(true);
        } else {
            metadata.setIsTransactionTable(false);
        }

        zookeeper.addMetadata(tableDesc.getTableName(), metadata);
        storeClient.close();

        System.out.println("success!");

    }

    protected static String getHBaseTableName(Table tbl) {
        // Give preference to TBLPROPERTIES over SERDEPROPERTIES
        // (really we should only use TBLPROPERTIES, so this is just
        // for backwards compatibility with the original specs).
        String tableName = tbl.getParameters().get(HBaseSerDe.HBASE_TABLE_NAME);
        if (tableName == null) {
            //convert to lower case in case we are getting from serde
            tableName = tbl.getSd().getSerdeInfo().getParameters().get(
                    HBaseSerDe.HBASE_TABLE_NAME);
            //standardize to lower case
            if (tableName != null) {
                tableName = tableName.toLowerCase();
            }
        }
        if (tableName == null) {
            tableName = (tbl.getDbName() + "." + tbl.getTableName()).toLowerCase();
            if (tableName.startsWith(DEFAULT_PREFIX)) {
                tableName = tableName.substring(DEFAULT_PREFIX.length());
            }
        }
        tbl.getParameters().put(HBaseSerDe.HBASE_TABLE_NAME, tableName);
        return tableName;
    }
}
*/
