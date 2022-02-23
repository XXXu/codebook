# RocksDB backup、checkpoint、snapshot区别
## backup：
BackupEngine是一个管理备份数据库目录的对象，下面使用java api演示一下怎么backup，备份得现有一个rocksdb数据库目录，才能实现备份。
```
public class RocksDBTest {
    // rocksdb 数据库目录
    private static final String rocksdb_path = "/root/rocksdata";
    // 备份的数据目录
    private static final String rocksdb_backup_path = "/root/backuprocksdata";
    private static RocksDB rocksDB = null;
    static {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setWriteBufferSize(1024 * 1024 * 1024);
        try {
            rocksDB = RocksDB.open(options, rocksdb_path);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws RocksDBException {
        backup();
        rocksDB.close();
    }
    
    public static void backup() throws RocksDBException {
        BackupableDBOptions backupableDBOptions = new BackupableDBOptions(rocksdb_backup_path);
        BackupEngine backupEngine = BackupEngine.open(rocksDB.getEnv(), backupableDBOptions);
        backupEngine.createNewBackup(rocksDB);
        // 每次备份的信息
        List<BackupInfo> backupInfos = backupEngine.getBackupInfo();
        for (BackupInfo backupInfo : backupInfos) {
            long size = backupInfo.size();
            String appMetadata = backupInfo.appMetadata();
            int backupId = backupInfo.backupId();
            int numberFiles = backupInfo.numberFiles();
            long timestamp = backupInfo.timestamp();
            System.out.println("---------------------------");
            System.out.println("size: " + size + ", appMetadata: " + appMetadata + ", backupId: " + backupId + ", numberFiles: " + numberFiles + ", timestamp: " + timestamp);
        }
        backupEngine.close();
    }
}
```
备份通常是增量的,BackupEngine::CreateNewBackup()并且只有新数据才会被复制到备份目录,一旦保存了一些备份，可以通过BackupEngine::GetBackupInfo()调用来获取所有备份的列表以及
每个备份的时间戳和逻辑大小的信息。可选择返回文件详细信息，从中可以确定共享详细信息。GetBackupInfo()甚至提供了一种将备份就地打开为只读数据库的方法，
这对于检查确切状态等很有用，备份由简单的递增整数 ID 标识。也就是说以上的backup方法可以多次运行，每次运行都有一个backupId记录标识。
备份的目的是恢复，可以通过backupEngine.restoreDbFromLatestBackup方法来恢复到哪一个备份的时间点。下面运行了四次backup方法备份数据的目录结构：
```
/root/backuprocksdata
├── meta
│   ├── 1
│   ├── 2
│   ├── 3
│   └── 4
├── private
│   ├── 1
│   │   ├── CURRENT
│   │   ├── MANIFEST-000020
│   │   └── OPTIONS-000023
│   ├── 2
│   │   ├── CURRENT
│   │   ├── MANIFEST-000035
│   │   └── OPTIONS-000038
│   ├── 3
│   │   ├── CURRENT
│   │   ├── MANIFEST-000038
│   │   └── OPTIONS-000041
│   └── 4
│   ├── CURRENT
│   ├── MANIFEST-000041
│   └── OPTIONS-000044
└── shared
├── 000004.sst
├── 000007.sst
└── 000025.sst
```
meta目录包含描述每个备份的“元文件”，其名称是备份 ID。例如，元文件包含属于该备份的所有文件的列表。
private目录始终包含非 SST 文件（选项、当前、清单和 WAL）
shared目录保存数据SST文件

备份是建立再checkpoint之上的，挡调用BackupEngine::CreateNewBackup方法时，会执行如下操作：
1. 禁用文件删除
2. 获取实时文件（包括表文件、当前文件、选项和清单文件）。
3. 将实时文件复制到备份目录。由于表文件是不可变的且文件名是唯一的，因此我们不会复制备份目录中已经存在的表文件。从 6.12 版开始，我们基本上拥有 SST 文件的唯一标识符，
使用 SST 文件属性中的文件编号和 DB 会话 ID。选项、清单和当前文件总是被复制到私有目录中，因为它们不是一成不变的。如果flush_before_backup设置为false，
我们还需要将日志文件复制到备份目录中。我们调用GetSortedWalFiles()并将所有活动文件复制到备份目录。
4. 重新启用文件删除

## checkpoint：
Checkpoint在rocksdb中提供了一种给运行中的数据库在一个独立的文件夹中生成快照的能力。checkpoint可以当成一个特定时间点的快照来使用，可以用只读模式打开，用于查询该时间点的数据，
或者以读写模式打开，作为一个可写的快照使用。checkpoint可以用于全量和增量备份（暂时不知道怎么用checkpoint增量备份）。

```
public static void checkpoint() throws RocksDBException {
        Checkpoint checkpoint = Checkpoint.create(rocksDB);
        // checkpoint的目录必须是不存在的，不能已经存在的目录，否则会抛异常org.rocksdb.RocksDBException: Directory exists
        checkpoint.createCheckpoint(rocksdb_checkpoint_path);
        checkpoint.close();
    }
```
rust api 使用checkpoint：
```
fn create_rocksdb_snapshot() -> Result<(), Error> {
        let rocksdb_data_path = Path::new(ROCKSDB_DATA_DIR);
        let rocksdb_checkpoint_path = Path::new(ROCKSDB_CHECKPOINT_DATA_DIR);
        if rocksdb_checkpoint_path.exists() {
            fs::remove_dir_all(rocksdb_checkpoint_path).unwrap();
        };
        let db = DB::open_default(rocksdb_data_path).unwrap();
        let check = Checkpoint::new(&db).unwrap();
        check.create_checkpoint(rocksdb_checkpoint_path)
    }
```
checkpoint功能使得Rocksdb有能力为给定的Rocksdb数据库在一个特定的文件夹创建一个一致性的快照。如果快照跟原始数据在同一个文件系统，SST文件会以硬链接形式生成，
否则，SST文件会被拷贝。MAINFEST文件和CURRENT文件会被拷贝。调用CreateCheckpoint方法时，这个目录不应该是已经存在的，他会被这个API创建，目录需要绝对路径。
checkpoint可以被当做一个只读的DB备份，或者可以被当做一个独立的DB实例打开。当以读写模式打开，SST文件会继续以硬链接存在，只有当这些文件被淘汰的时候，这些链接才会被删除。
当用户用完这个快照，用户可以删除这个目录，以删除这个快照。
checkpoint目录结构：
```
checkpointrocksdata/
├── 000004.sst
├── 000007.sst
├── 000025.sst
├── 000069.sst
├── 000071.sst
├── CURRENT
├── MANIFEST-000072
└── OPTIONS-000075
```
这个目录是可以直接当作rocksdb的数据目录使用的，而backup的目录是不可以直接使用的，需要调用一下restore方法才可以使用。

## snapshot：
一个快照会捕获在创建的时间点的DB的一致性视图，快照在DB重启之后将消失。
```
public static void snapshot() throws RocksDBException {
        Snapshot snapshot1 = rocksDB.getSnapshot();
        String key = "snapshotkey";
        String value = "snapshotvalue";
        rocksDB.put(key.getBytes(), value.getBytes());
        
        ReadOptions readOptions1 = new ReadOptions();
        readOptions1.setSnapshot(snapshot1);
        if (rocksDB.get(readOptions1, key.getBytes()) != null) {
			// 用的snapshot1没有新添加的数据，所以为null
            System.out.println(new String(rocksDB.get(readOptions1, key.getBytes())));
        }
        Snapshot snapshot2 = rocksDB.getSnapshot();
        ReadOptions readOptions2 = new ReadOptions();
        readOptions2.setSnapshot(snapshot2);
        // 数据已经添加进去，所以不为null
        System.out.println(new String(rocksDB.get(readOptions2, key.getBytes())));
    }
```

**总结：**
> backup既可以全量备份，也可以增量备份数据。checkpoint全量备份数据，文档上说可以增量备份，目前不知道如何实现。snapshot并不用于备份数据，而是捕获当前时间点的视图，视图是不可以更改的。