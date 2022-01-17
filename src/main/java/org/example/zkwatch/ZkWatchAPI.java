package org.example.zkwatch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkWatchAPI implements Watcher {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZkWatchAPI.class);

    private static final int SESSION_TIMEOUT = 10000;

    private ZooKeeper zk = null;

    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public void connectionZookeeper(String connectString){
        connectionZookeeper(connectString,SESSION_TIMEOUT);
    }

    public void connectionZookeeper(String connectString, int sessionTimeout){
        this.releaseConnection();
        try {
            // ZK客户端允许我们将ZK服务器的所有地址都配置在这里
            try {
                zk = new ZooKeeper(connectString, sessionTimeout, this );
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 使用CountDownLatch.await()的线程（当前线程）阻塞直到所有其它拥有CountDownLatch的线程执行完毕（countDown()结果为0）
            connectedSemaphore.await();
        } catch ( InterruptedException e ) {
            log.error("连接创建失败，发生 InterruptedException , e " + e.getMessage(), e);
        } catch ( Exception e ) {
            log.error( "连接创建失败，发生 IOException , e " + e.getMessage(), e );
        }
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        if ( null != zk ) {
            try {
                this.zk.close();
            } catch ( InterruptedException e ) {
                log.error("release connection error ," + e.getMessage() ,e);
            }
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        log.info("收到事件通知：" + watchedEvent.getState());

        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            connectedSemaphore.countDown();
        }
    }

    public boolean createPath( String path, String data) {
        try {
            String zkPath =  this.zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            log.info( "节点创建成功, Path: " + zkPath + ", content: " + data );
            return true;
        } catch (Exception e ) {
            log.error( "节点创建失败, 发生 InterruptedException! path: " + path + ", data:" + data
                    + ", errMsg:" + e.getMessage(), e );
        }
        return false;
    }

    public String readData(String path){
        String data = null;
        try {
            Stat stat = new Stat();

            data = new String(this.zk.getData( path, false, stat));

            log.info("==stat.getEphemeralOwner: " + Long.toHexString(stat.getEphemeralOwner()));
            System.out.println("==stat.getEphemeralOwner: " + Long.toHexString(stat.getEphemeralOwner()));

            log.info("读取数据成功, path:" + path + ", content:" + data);
            System.out.println("读取数据成功, path:" + path + ", content:" + data);
        } catch (Exception e) {
            log.error( "读取数据失败,发生InterruptedException! path: " + path
                    + ", errMsg:" + e.getMessage(), e);
        }
        return  data;
    }

    public static void main(String[] args) {
        ZkWatchAPI zkWatchAPI = new ZkWatchAPI();

        String ip = args[0];
        zkWatchAPI.connectionZookeeper(ip + ":2181");

        zkWatchAPI.createPath("/test3", "oko");

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("asd");

        }

        //        String znodeStr = args[1];
//
//        zkWatchAPI.connectionZookeeper(ip + ":2181");
//
//        zkWatchAPI.readData(znodeStr);
//
//        zkWatchAPI.releaseConnection();
    }
}
