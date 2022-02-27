package org.apache.hadoop.hdfs.server.namenode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
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

    public static void main(String[] args) {
        try {
            testOfflineImageViewerPB(args);
//            createNewFsimage();
            /*String per1 = "rwxr-xr-x";
            String per2 = "rw-r---wx";
            String per3 = "r---w-rwt";
            String per4 = "--x---r-T";
            System.out.println(String.format("%04o",getFsPermission(per1).toExtendedShort()));
            System.out.println(String.format("%04o",getFsPermission(per2).toExtendedShort()));
            System.out.println(String.format("%04o",getFsPermission(per3).toExtendedShort()));
            System.out.println(String.format("%04o",getFsPermission(per4).toExtendedShort()));
            System.out.println(getFsPermission(per2).toExtendedShort());
            System.out.println(getFsPermission(per3).toExtendedShort());
            System.out.println(getFsPermission(per4).toExtendedShort());*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FsPermission getFsPermission(String permissonStr) {
        String user = permissonStr.substring(0, 3);
        String group = permissonStr.substring(3, 6);
        String other = permissonStr.substring(6, 8);
        boolean stickyBit = false;

        if ("t".equals(permissonStr.substring(8))) {
            other = other + "x";
            stickyBit = true;
        } else if ("T".equals(permissonStr.substring(8))) {
            other = other + "-";
            stickyBit = false;
        } else {
            other = other + permissonStr.substring(8);
        }

        FsAction ufsAction = FsAction.getFsAction(user);
        FsAction gfsAction = FsAction.getFsAction(group);
        FsAction ofsAction = FsAction.getFsAction(other);

        return new FsPermission(ufsAction, gfsAction, ofsAction, stickyBit);
    }
}

