package org.apache.hadoop.hdfs.server.namenode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.tools.offlineImageViewer.OfflineImage2Rocksdb;
import org.apache.hadoop.hdfs.tools.offlineImageViewer.OfflineImageViewerPB;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FsimageTest {
    public static void createNewFsimage() throws IOException {
        System.out.println("================== Begin ==================");
        Configuration conf = new Configuration();
        conf.addResource(new Path("./conf/core-site.xml"));
        conf.addResource(new Path("./conf/hdfs-site.xml"));
        String fsimagePath = "/root/fsimage/jiujiu70/fsimage_0000000000000225539";
        FSNamesystem fns = new FSNamesystem(conf, new FSImage(conf));
        FSImageFormatProtobuf.Loader loader = new FSImageFormatProtobuf.Loader(conf, fns,false);
        loader.load(new File(fsimagePath));
        long txId = loader.getLoadedImageTxId();
        System.out.println("Loaded image for txId " + txId);
        System.out.println("Try to save a new image");
        FSImage fsImage = fns.getFSImage();
        fns.setSafeMode(HdfsConstants.SafeModeAction.SAFEMODE_ENTER);
        fsImage.lastAppliedTxId = txId;

        String blockPoolID = fsImage.getBlockPoolID();
        String clusterID = fsImage.getClusterID();
        long lastAppliedOrWrittenTxId = fsImage.getLastAppliedOrWrittenTxId();
        long lastAppliedTxId = fsImage.getLastAppliedTxId();
        int layoutVersion = fsImage.getLayoutVersion();
        int namespaceID = fsImage.getNamespaceID();
        NNStorage storage = fsImage.getStorage();
        long correctLastAppliedOrWrittenTxId = fsImage.getCorrectLastAppliedOrWrittenTxId();
        long mostRecentCheckpointTxId = fsImage.getMostRecentCheckpointTxId();

        System.out.println("blockPoolID: " + blockPoolID);
        System.out.println("clusterID: " + clusterID);
        System.out.println("lastAppliedOrWrittenTxId: " + lastAppliedOrWrittenTxId);
        System.out.println("lastAppliedTxId: " + lastAppliedTxId);
        System.out.println("layoutVersion: " + layoutVersion);
        System.out.println("namespaceID: " + namespaceID);
        System.out.println("correctLastAppliedOrWrittenTxId: " + correctLastAppliedOrWrittenTxId);
        System.out.println("mostRecentCheckpointTxId: " + mostRecentCheckpointTxId);
        System.out.println("storage: " + storage.toString());

        fns.saveNamespace();
        System.out.println("================== Done ==================");
    }

    public static void testOfflineImageViewerPB(String[] args) throws Exception {
        OfflineImageViewerPB.run(args);
    }

    public static void testOfflineImageRocksdb(String[] args) throws Exception {
        OfflineImage2Rocksdb.run(args);
    }

    public static void main(String[] args) {
        try {
            testOfflineImageViewerPB(args);
//            testOfflineImageRocksdb(args);
//            createNewFsimage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

