package org.example.zkwatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkWatchTest {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZkWatchTest.class);
    private static CountDownLatch cound = new CountDownLatch(1);
    private static volatile Boolean flag = true;
    private String connect;
    private int sessionTimeout;
    private ZooKeeper zk;
    public ZooKeeper getZookeeper() throws InterruptedException {
        final ZooKeeper[] zk = {null};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    zk[0] = new ZooKeeper(connect, sessionTimeout, new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            log.info("watchedEvent.getPath(); => " + watchedEvent.getPath());
                            log.info("watchedEvent.getState(); => " + watchedEvent.getState());
                            log.info("watchedEvent.getType(); =>" + watchedEvent.getType());
                        }
                    });
                    while (flag) {
                        System.out.println(zk[0].getState() + ".....");
                        Thread.sleep(1000);
                        if (zk[0].getState().equals(ZooKeeper.States.CONNECTING)) {
                            log.info("正在连接中。。。");
                        }
                        if (zk[0].getState().equals(ZooKeeper.States.CONNECTED)) {
                            log.info("连接成功！");
                            flag = false;
                            cound.countDown();
                        }
                    }
                } catch (IOException e) {
                    log.error("连接出错：", e);
                } catch (InterruptedException e) {
                    log.error("线程错误：", e);
                }
            }
        }).start();
        cound.await();
        return zk[0];
    }
    public ZkWatchTest(String connect, int sessionTimeout) throws InterruptedException {
        this.connect = connect;
        this.sessionTimeout = sessionTimeout;
        zk = getZookeeper();
    }

    /**
     * 删除一个节点
     * @param path 节点路径
     * @return
     */
    public boolean deteleZKNode(String path){
        try{
            zk.delete(path,-1);
            log.info("Zookeeper删除节点成功，节点地址：" + path);
            return  true;
        } catch (Exception e){
            log.error("删除节点失败：" + e.getMessage() + ",path:" + path,e);
        }
        return false;
    }

    public boolean isExists(String path){
        try {
            log.info("wathcer注册exists<=====" + path + "==============>");
            Stat stat = zk.exists(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("exists调用  getPath => " + watchedEvent.getPath());
                    log.info("exists调用 getState => " + watchedEvent.getState().toString());
                    log.info("exists调用 getType => " + watchedEvent.getType().toString());
                }
            });
            return null != stat;
        } catch (Exception e) {
            log.error( "读取数据失败,发生InterruptedException! path: " + path
                    + ", errMsg:" + e.getMessage(), e );
        }
        return  false;
    }

    public String readData(String path){
        String data=null;
        try {
            log.info("wathcer注册getData<=====" + path + "==============>");
            data = new String(zk.getData(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("getData调用  getPath => " + watchedEvent.getPath());
                    log.info("getData调用 getState => " + watchedEvent.getState().toString());
                    log.info("getData调用 getType => " + watchedEvent.getType().toString());
                }
            },null));
            log.info("读取数据成功，其中path:" + path+ ", data-content:" + data);
        } catch (Exception e) {
            log.error( "读取数据失败,InterruptedException! path: " + path + ", errMsg:" + e.getMessage(), e );
        }
        return data;
    }

    public List<String> getChild(String path){
        log.info("wathcer注册getChildren<=====" + path + "==============>");
        try {
            List<String> list = zk.getChildren(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("getChildren调用  getPath => " + watchedEvent.getPath());
                    log.info("getChildren调用 getState => " + watchedEvent.getState().toString());
                    log.info("getChildren调用 getType => " + watchedEvent.getType().toString());
                }
            });
            if(list.isEmpty()){
                log.info(path + "的路径下没有节点");
            }
            return list;
        } catch (Exception e) {
            log.error( "读取子节点数据失败,InterruptedException! path: " + path
                    + ", errMsg:" + e.getMessage(), e );
        }
        return null;
    }
}
