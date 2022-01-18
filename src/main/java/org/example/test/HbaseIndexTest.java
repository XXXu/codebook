package org.example.test;

import com.google.protobuf.HBaseZeroCopyByteString;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.protobuf.generated.HyperbaseProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hyperbase.client.HyperbaseAdmin;
import org.apache.hadoop.hyperbase.secondaryindex.CombineIndex;
import org.apache.hadoop.hyperbase.secondaryindex.IndexedColumn;
import org.apache.hadoop.hyperbase.secondaryindex.SecondaryIndexUtil;

import java.io.IOException;

public class HbaseIndexTest {
    public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.addResource("./conf3/core-site.xml");
        conf.addResource("./conf3/hdfs-site.xml");
        conf.addResource("./conf3/hbase-site.xml");
        HyperbaseAdmin admin = new HyperbaseAdmin(conf);
        TableName tableName = TableName.valueOf("test-1");
        //split key used to index table
        byte[][] splitKeys = null;
        boolean withCompression = false;
        byte[] indexName = Bytes.toBytes("idx");
        byte[] family = Bytes.toBytes("f");
        byte[] q = Bytes.toBytes("q3");
//        byte[] q4 = Bytes.toBytes("q4");
        // build index, index column is q
        HyperbaseProtos.SecondaryIndex.Builder builder = HyperbaseProtos.SecondaryIndex.newBuilder();
        //set index type and properties
        builder.setClassName(CombineIndex.class.getName());
        builder.setUpdate(false);
        builder.setDcop(false);
        // index column
        builder.addColumns(HyperbaseProtos.IndexedColumn.newBuilder().setColumn(HyperbaseProtos.Column.
                newBuilder().
                setFamily(HBaseZeroCopyByteString.wrap(family)).
                setQualifier(HBaseZeroCopyByteString.wrap(q))).setSegmentLength(6));
        // rowkey column
        builder.addColumns(HyperbaseProtos.IndexedColumn.newBuilder().setColumn(HyperbaseProtos.Column.
                newBuilder().
                setFamily(HBaseZeroCopyByteString.wrap(IndexedColumn.ROWKEY_FAMILY)).
                setQualifier(HBaseZeroCopyByteString.wrap(IndexedColumn.ROWKEY_QUALIFIER))).setSegmentLength(7));

        CombineIndex index = (CombineIndex) SecondaryIndexUtil.constructSecondaryIndexFromPb(builder.build());
        //add global index
        admin.addGlobalIndex(tableName, index, indexName, splitKeys, withCompression);
        //delete global index
//        admin.deleteGlobalIndex(tableName, indexName);
        admin.close();
    }
}
