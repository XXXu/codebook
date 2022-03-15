package org.apache.hadoop.hdfs.server.namenode;

import java.io.*;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.server.namenode.FSImage;
import org.apache.hadoop.hdfs.server.namenode.FSImageFormatProtobuf;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;

public class FixFSImage {
    private static void printUsage() {
        String usage = "Required command line arguments:\n-i, --inputFile <arg>   FSImage file to process.\n-c, --confdir <arg>     config directory where core-site.xml and hdfs-site.xml can be found.\n-f, --fix               save a new fsimage";
        System.out.println("Required command line arguments:\n-i, --inputFile <arg>   FSImage file to process.\n-c, --confdir <arg>     config directory where core-site.xml and hdfs-site.xml can be found.\n-f, --fix               save a new fsimage");
    }

    private static Options buildOptions() {
        Options options = new Options();

        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("inputFile");
        options.addOption(OptionBuilder.create("i"));

        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("confdir");
        options.addOption(OptionBuilder.create("c"));

        options.addOption("f", "fix", false, "");

        return options;
    }

    private static int run(String[] args) throws IOException {
        System.out.println("================== Begin ==================");
        Options options = buildOptions();
        CommandLine cmd;
        CommandLineParser parser = new PosixParser();
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.out.println("Error parsing command-line options: " + e.getMessage());
            printUsage();
            return 1;
        }

        String fsimage = cmd.getOptionValue("i");
        String confdir = cmd.getOptionValue("c");
        System.out.println("fsimage: " + fsimage);
        System.out.println("confdir: " + confdir);

        Configuration conf = new Configuration();
        conf.addResource(new FileInputStream(new File(confdir + "/core-site.xml")));
        conf.addResource(new FileInputStream(new File(confdir + "/hdfs-site.xml")));

        FSNamesystem fns = new FSNamesystem(conf, new FSImage(conf));
        FSImageFormatProtobuf.Loader loader = new FSImageFormatProtobuf.Loader(conf, fns, false);

        loader.load(new File(fsimage));
        long txId = loader.getLoadedImageTxId();
        System.out.println("Loaded image for txId " + txId);

        if (cmd.hasOption("f")) {
            System.out.println("Try to save a new image");
            fns.setSafeMode(HdfsConstants.SafeModeAction.SAFEMODE_ENTER);
            fns.getFSImage().lastAppliedTxId = txId;
            fns.saveNamespace();
        }

        System.out.println("================== Done ==================");
        return 0;
    }

    public static void main(String[] args) throws IOException {
        int status = run(args);
        System.exit(status);
    }
}

class NameSection implements Serializable {
    private long genstampV1;
    private long genstampV2;
    private long genstampV1Limit;
    private long lastAllocatedBlockId;
    private long txid;

    public long getGenstampV1() {
        return genstampV1;
    }

    public void setGenstampV1(long genstampV1) {
        this.genstampV1 = genstampV1;
    }

    public long getGenstampV2() {
        return genstampV2;
    }

    public void setGenstampV2(long genstampV2) {
        this.genstampV2 = genstampV2;
    }

    public long getGenstampV1Limit() {
        return genstampV1Limit;
    }

    public void setGenstampV1Limit(long genstampV1Limit) {
        this.genstampV1Limit = genstampV1Limit;
    }

    public long getLastAllocatedBlockId() {
        return lastAllocatedBlockId;
    }

    public void setLastAllocatedBlockId(long lastAllocatedBlockId) {
        this.lastAllocatedBlockId = lastAllocatedBlockId;
    }

    public long getTxid() {
        return txid;
    }

    public void setTxid(long txid) {
        this.txid = txid;
    }
}
class Blocks implements Serializable {
    private List<Block> block;

    public List<Block> getBlock() {
        return block;
    }

    public void setBlock(List<Block> block) {
        this.block = block;
    }
}
class Block implements Serializable {
    private long id;
    private long genstamp;
    private long numBytes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGenstamp() {
        return genstamp;
    }

    public void setGenstamp(long genstamp) {
        this.genstamp = genstamp;
    }

    public long getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(long numBytes) {
        this.numBytes = numBytes;
    }
}

class FileUnderConstruction implements Serializable {
    private String clientName;
    private String clientMachine;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientMachine() {
        return clientMachine;
    }

    public void setClientMachine(String clientMachine) {
        this.clientMachine = clientMachine;
    }
}
class INode implements Serializable {
    private long id;
    private String type;
    private String name;
    private long mtime;
    private String permission;
    private long nsquota;
    private long dsquota;
    private int replication;
    private long atime;
    private String target;
    private long perferredBlockSize;
    private Blocks blocks;
    private FileUnderConstruction file_under_construction;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getNsquota() {
        return nsquota;
    }

    public void setNsquota(long nsquota) {
        this.nsquota = nsquota;
    }

    public long getDsquota() {
        return dsquota;
    }

    public void setDsquota(long dsquota) {
        this.dsquota = dsquota;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public long getAtime() {
        return atime;
    }

    public void setAtime(long atime) {
        this.atime = atime;
    }

    public long getPerferredBlockSize() {
        return perferredBlockSize;
    }

    public void setPerferredBlockSize(long perferredBlockSize) {
        this.perferredBlockSize = perferredBlockSize;
    }

    public Blocks getBlocks() {
        return blocks;
    }

    public void setBlocks(Blocks blocks) {
        this.blocks = blocks;
    }

    public FileUnderConstruction getFile_under_construction() {
        return file_under_construction;
    }

    public void setFile_under_construction(FileUnderConstruction file_under_construction) {
        this.file_under_construction = file_under_construction;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
class INodeSection implements Serializable {
    private long lastInodeId;
    private List<INode> inode;

    public long getLastInodeId() {
        return lastInodeId;
    }

    public void setLastInodeId(long lastInodeId) {
        this.lastInodeId = lastInodeId;
    }

    public List<INode> getInode() {
        return inode;
    }

    public void setInode(List<INode> inode) {
        this.inode = inode;
    }
}
class INodeDirectorySection implements Serializable {
    private List<Directory> directorys;

    public List<Directory> getDirectorys() {
        return directorys;
    }

    public void setDirectorys(List<Directory> directorys) {
        this.directorys = directorys;
    }
}
class Directory implements Serializable {
    private long parent;
    private List<Long> inode;
    private List<Integer> inodereference_index;

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public List<Long> getInode() {
        return inode;
    }

    public void setInode(List<Long> inode) {
        this.inode = inode;
    }

    public List<Integer> getInodereference_index() {
        return inodereference_index;
    }

    public void setInodereference_index(List<Integer> inodereference_index) {
        this.inodereference_index = inodereference_index;
    }
}
class FileUnderConstructionSection implements Serializable {
    private List<INodeUC> inode;

    public List<INodeUC> getInode() {
        return inode;
    }

    public void setInode(List<INodeUC> inode) {
        this.inode = inode;
    }
}
class INodeUC implements Serializable {
    private long id;
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
class SecretManagerSection implements Serializable {
    private long currentId;
    private long tokenSequenceNumber;

    public long getCurrentId() {
        return currentId;
    }

    public void setCurrentId(long currentId) {
        this.currentId = currentId;
    }

    public long getTokenSequenceNumber() {
        return tokenSequenceNumber;
    }

    public void setTokenSequenceNumber(long tokenSequenceNumber) {
        this.tokenSequenceNumber = tokenSequenceNumber;
    }
}
class Pool implements Serializable {
    private String poolName;
    private String ownerName;
    private String groupName;
    private int mode;
    private long limit;
    private long maxRelativeExpiry;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getMaxRelativeExpiry() {
        return maxRelativeExpiry;
    }

    public void setMaxRelativeExpiry(long maxRelativeExpiry) {
        this.maxRelativeExpiry = maxRelativeExpiry;
    }
}
class Directive implements Serializable {
    private long id;
    private String path;
    private int replication;
    private String pool;
    private Expiration expiration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getReplication() {
        return replication;
    }

    public void setReplication(int replication) {
        this.replication = replication;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public Expiration getExpiration() {
        return expiration;
    }

    public void setExpiration(Expiration expiration) {
        this.expiration = expiration;
    }
}
class Expiration implements Serializable {
    private long millis;
    private boolean relatilve;

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public boolean isRelatilve() {
        return relatilve;
    }

    public void setRelatilve(boolean relatilve) {
        this.relatilve = relatilve;
    }
}
class CacheManagerSection implements Serializable {
    private long nextDirectiveId;

    private List<Pool> pools;
    private List<Directive> directives;

    public long getNextDirectiveId() {
        return nextDirectiveId;
    }

    public void setNextDirectiveId(long nextDirectiveId) {
        this.nextDirectiveId = nextDirectiveId;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public void setPools(List<Pool> pools) {
        this.pools = pools;
    }

    public List<Directive> getDirectives() {
        return directives;
    }

    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }
}

class INodeReferenceSection {}

class SnapshotSection {
    private long snapshotCounter;
    private SnapshottableDir snapshottableDir;

    public long getSnapshotCounter() {
        return snapshotCounter;
    }

    public void setSnapshotCounter(long snapshotCounter) {
        this.snapshotCounter = snapshotCounter;
    }

    public SnapshottableDir getSnapshottableDir() {
        return snapshottableDir;
    }

    public void setSnapshottableDir(SnapshottableDir snapshottableDir) {
        this.snapshottableDir = snapshottableDir;
    }
}

class SnapshottableDir {
    private List<Long> dir;

    public List<Long> getDir() {
        return dir;
    }

    public void setDir(List<Long> dir) {
        this.dir = dir;
    }
}

class Fsimage implements Serializable {
    private NameSection nameSection;
    private INodeSection inodeSection;
    private INodeReferenceSection inodeReferenceSection;
    private SnapshotSection snapshotSection;
    private INodeDirectorySection inodeDirectorySection;
    private FileUnderConstructionSection fileUnderConstructionSection;
    private SecretManagerSection secretManagerSection;
    private CacheManagerSection cacheManagerSection;

    public INodeReferenceSection getInodeReferenceSection() {
        return inodeReferenceSection;
    }

    public void setInodeReferenceSection(INodeReferenceSection inodeReferenceSection) {
        this.inodeReferenceSection = inodeReferenceSection;
    }

    public SnapshotSection getSnapshotSection() {
        return snapshotSection;
    }

    public void setSnapshotSection(SnapshotSection snapshotSection) {
        this.snapshotSection = snapshotSection;
    }

    public NameSection getNameSection() {
        return nameSection;
    }

    public void setNameSection(NameSection nameSection) {
        this.nameSection = nameSection;
    }

    public INodeSection getInodeSection() {
        return inodeSection;
    }

    public void setInodeSection(INodeSection inodeSection) {
        this.inodeSection = inodeSection;
    }

    public INodeDirectorySection getInodeDirectorySection() {
        return inodeDirectorySection;
    }

    public void setInodeDirectorySection(INodeDirectorySection inodeDirectorySection) {
        this.inodeDirectorySection = inodeDirectorySection;
    }

    public FileUnderConstructionSection getFileUnderConstructionSection() {
        return fileUnderConstructionSection;
    }

    public void setFileUnderConstructionSection(FileUnderConstructionSection fileUnderConstructionSection) {
        this.fileUnderConstructionSection = fileUnderConstructionSection;
    }

    public SecretManagerSection getSecretManagerSection() {
        return secretManagerSection;
    }

    public void setSecretManagerSection(SecretManagerSection secretManagerSection) {
        this.secretManagerSection = secretManagerSection;
    }

    public CacheManagerSection getCacheManagerSection() {
        return cacheManagerSection;
    }

    public void setCacheManagerSection(CacheManagerSection cacheManagerSection) {
        this.cacheManagerSection = cacheManagerSection;
    }
}
