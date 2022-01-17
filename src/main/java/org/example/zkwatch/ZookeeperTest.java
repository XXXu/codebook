package org.example.zkwatch;

public class ZookeeperTest {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZookeeperTest.class);
    public static void main(String[] args) {
        ZkWatchTest zt = null;
        try {
            zt = new ZkWatchTest("172.22.51.5:2181",2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 不支持递归删除 这一行代码会报错 因为该节点下存在watcher_children节点
//        zt.deteleZKNode("/test_watcher");
        log.info("/hdfs1-ha/nameservice1/ActiveStandbyElectorLock read data => " + zt.readData("/hdfs1-ha/nameservice1/ActiveStandbyElectorLock"));
//        zt.deteleZKNode("/test_watcher/watcher_children");

    }
}
