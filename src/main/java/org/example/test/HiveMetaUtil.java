package org.example.test;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.hbase.HBaseStorageHandler;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

public class HiveMetaUtil extends HBaseStorageHandler {
    public static void main(String[] args) throws TException {
        HiveConf hiveConf = new HiveConf();
        hiveConf.addResource(new Path("./conf/hive-site.xml"));
        HiveMetaStoreClient storeClient = new HiveMetaStoreClient(hiveConf);

        Table table = storeClient.getTable("default", "test766");

        boolean isExternal = MetaStoreUtils.isExternalTable(table);
        System.out.println("isExternal:"+isExternal);

        System.out.println(table.getSd().getLocation());

//        CreateGlobalIndexDesc indexDesc = new CreateGlobalIndexDesc();
        storeClient.close();

    }

}
