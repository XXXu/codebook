package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class HdfsDemo {

    public static void main(String[] args) throws IOException {
//        System.setProperty("java.security.krb5.conf","./conf2/krb5.conf");
        DistributedFileSystem fileSystem = (DistributedFileSystem) getFileSystem();
//        System.out.println(fileSystem);
        FileStatus fileStatus = fileSystem.getFileStatus(new Path("/hyperbase1/oldWALs/tdh8145%2C60020%2C1653649861834.1653729093562"));
        System.out.println(fileStatus.getAccessTime());
//        fileSystem.copyFromLocalFile(new Path("./conf/hbase-site.xml"), new Path("/tmp"));
//        fileSystem.close();
    }

    public static FileSystem getFileSystem() throws IOException {
        Configuration conf = new HdfsConfiguration();
        conf.addResource(new Path("./conf3/core-site.xml"));
        conf.addResource(new Path("./conf3/hdfs-site.xml"));

//        UserGroupInformation.setConfiguration(conf);
//        UserGroupInformation.loginUserFromKeytab("hdfs/amen05-18@TDH","./conf2/hdfs.keytab");
        FileSystem fs = FileSystem.get(conf);;
        return fs;
    }
}
