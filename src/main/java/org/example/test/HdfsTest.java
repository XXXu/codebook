package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;

import java.io.IOException;
import java.net.URI;

public class HdfsTest {
//    public static final Logger log = Logger.getLogger(HdfsTest.class);

    public static void main(String[] args) throws IOException, InterruptedException {
//        FileSystem fileSystem = getFileSystem();
//        fileSystem.create(new Path("/data1/data2"));
//        fileSystem.mkdirs(new Path("/data2"));
//        fileSystem.copyToLocalFile(new Path("/data/anaconda-ks.cfg-0-99"), new Path("./conf"));
//        fileSystem.copyFromLocalFile(new Path("./conf/core-site.xml"), new Path("/test1"));
//        fileSystem.delete(new Path("/test/anaconda-ks.cfg"), true);
//        FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path("/test/1.txt"));
//        fsDataOutputStream.writeUTF("ajsidjiasdjijsai");
//        fsDataOutputStream.close();

//        copyFileToHDFS("./conf/core-site.xml", "/test", fileSystem);
//        FsStatus status = fileSystem.getStatus();
//        log.info("status.getCapacity(): " + status.getCapacity());

//        FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path("/test1/9.txt"));
        /*for (int i = 0; i < 5000; i++) {
            fsDataOutputStream.writeUTF("asjdijaisjidiasjdiajsidjiajdisajidjasidiasjd" + i);
            fsDataOutputStream.writeUTF("\n");
            fsDataOutputStream.flush();
            fsDataOutputStream.hflush();
            Thread.sleep(100);
        }*/
//        Thread.sleep(1000 * 60);
//        fsDataOutputStream.flush();
//        fsDataOutputStream.close();

//        fileSystem.close();


        int threadCount = Integer.parseInt(args[0]);
        String srcFile = args[1];
        String destPath = args[2];

        for (int i = 0; i < threadCount; i++) {
            Work work = new Work(srcFile, destPath + "-" + i);
            work.start();
        }

    }

    public static class Work extends Thread {
        private String srcFile;
        private String destPath;

        public Work(String srcFile, String destPath) {
            this.srcFile = srcFile;
            this.destPath = destPath;
        }

        @Override
        public void run() {
            FileSystem fileSystem = getFileSystem();
            for (int i = 0; i < 500000; i++) {
                copyFileToHDFS(srcFile, destPath + "/" + i, fileSystem);
            }
            try {
                fileSystem.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void copyFileToHDFS(String srcFile, String destPath, FileSystem fs) {
        Path srcPath = new Path(srcFile);

        Path dstPath = new Path(destPath);
        try {
            fs.copyFromLocalFile(srcPath, dstPath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static FileSystem getFileSystem() {
        Configuration conf = new HdfsConfiguration();
        conf.addResource(new Path("./conf/core-site.xml"));
        conf.addResource(new Path("./conf/hdfs-site.xml"));

//        conf.setInt("dfs.client.failover.connection.retries.on.timeouts", 1);
//        conf.setInt("dfs.client.failover.connection.retries", 1);
//        conf.setInt("dfs.client.failover.max.attempts", 3);

        String defaultFS = conf.get("fs.defaultFS");
//        System.out.println("defaultFS:" + defaultFS);
//        log.info("defaultFS:" + defaultFS);
//        log.info("dfs.client.failover.connection.retries.on.timeouts: " + conf.get("dfs.client.failover.connection.retries.on.timeouts"));
//        log.info("dfs.client.failover.connection.retries: " + conf.get("dfs.client.failover.connection.retries"));
//        log.info("dfs.client.failover.max.attempts: " + conf.get("dfs.client.failover.max.attempts"));
        FileSystem fs = null;

        try {
            URI uri = new URI(defaultFS.trim());
//            System.out.println("getScheme: " + uri.getScheme());
//            System.out.println("getAuthority: " + uri.getAuthority());
            // hdfs user get a filesystem
            fs = FileSystem.get(uri, conf, "hdfs");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return fs;
    }
}
