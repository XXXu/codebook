package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class HdfsDemo {

    public static void main(String[] args) throws IOException {
        System.setProperty("java.security.krb5.conf","./conf2/krb5.conf");
        DistributedFileSystem fileSystem = (DistributedFileSystem) getFileSystem();
        System.out.println(fileSystem);
        fileSystem.copyFromLocalFile(new Path("./conf/hbase-site.xml"), new Path("/tmp"));
        fileSystem.close();
    }

    public static FileSystem getFileSystem() throws IOException {
        Configuration conf = new HdfsConfiguration();
        conf.addResource(new Path("./conf2/core-site.xml"));
        conf.addResource(new Path("./conf2/hdfs-site.xml"));

        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hdfs/amen05-18@TDH","./conf2/hdfs.keytab");
        FileSystem fs = FileSystem.get(conf);;
        return fs;
    }
}
