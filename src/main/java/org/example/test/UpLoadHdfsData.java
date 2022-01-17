package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpLoadHdfsData {
    public static FileSystem getFileSystem() {
        System.setProperty("java.security.krb5.conf", "./conf/krb5.conf");
//        System.setProperty("java.security.auth.login.config", "./conf/jaas.conf");
        Configuration conf = new HdfsConfiguration();
        conf.addResource(new Path("./conf/core-site.xml"));
        conf.addResource(new Path("./conf/hdfs-site.xml"));

        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab("hdfs/vqa150@TDH", "./conf/hdfs.keytab");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String defaultFS = conf.get("fs.defaultFS");
        FileSystem fs = null;
        try {
            URI uri = new URI(defaultFS.trim());
            // hdfs user get a filesystem
            fs = FileSystem.get(uri, conf, "hdfs");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return fs;
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

    public static void mkdirs(Path path, FileSystem fs) {
        try {
            fs.mkdirs(path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createFile(Path file, FileSystem fileSystem) {
        try {
            fileSystem.create(file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static class WorkThread extends Thread {
        private String path;
        private int dirCount;
        private CountDownLatch countDownLatch;

        public WorkThread(String path, int dirCount, CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
            this.path = path;
            this.dirCount = dirCount;
        }

        @Override
        public void run() {
            try {
                FileSystem fileSystem = getFileSystem();
                for (int i = 0; i < dirCount; i++) {
                    String dir = path + "/test" + i;
                    mkdirs(new Path(dir), fileSystem);
                    createFile(new Path(dir + "/1.txt"), fileSystem);
                }
                fileSystem.close();
                countDownLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        /*int threadCount = Integer.parseInt(args[0]);
        int dirCount = Integer.parseInt(args[1]);
        String dirPath = args[2];*/

        int threadCount = 2;
        int dirCount = 10;
        String dirPath = "/demo";

        System.out.println("threadCount: " + threadCount);
        System.out.println("dirCount: " + dirCount);
        System.out.println("dirPath: " + dirPath);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        FileSystem fileSystem = getFileSystem();
        mkdirs(new Path(dirPath), fileSystem);
        fileSystem.close();


        for (int i = 0; i < threadCount; i++) {
            String dir = dirPath+"/test" + i;
            FileSystem fs = getFileSystem();
            mkdirs(new Path(dir), fileSystem);
            fs.close();
            WorkThread workThread = new WorkThread(dir, dirCount, countDownLatch);
            executorService.execute(workThread);
        }
        countDownLatch.await();
        executorService.shutdown();

    }
}
