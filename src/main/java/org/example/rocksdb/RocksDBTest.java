package org.example.rocksdb;

import org.rocksdb.*;

import java.util.List;

public class RocksDBTest {
    private static final String rocksdb_path = "/root/rocksdata";
    private static final String rocksdb_backup_path = "/root/backuprocksdata";
    private static final String rocksdb_checkpoint_path = "/root/checkpointrocksdata";

    private static final String rocksdb_read_backup_path = "/root/readbackuprocksdata";

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
//        putData("xuqw","komaqws");
        getData("koma1");
//        backup();
//        checkpoint();
//        readBackup();
//        snapshot();
//        snapshot();
        rocksDB.close();
    }

    private static void repeatableRead(final OptimisticTransactionDB txnDb,
                                       final WriteOptions writeOptions, final ReadOptions readOptions) throws RocksDBException {

        final byte key1[] = "ghi".getBytes();
        final byte value1[] = "jkl".getBytes();

        // Set a snapshot at start of transaction by setting setSnapshot(true)
        try(final OptimisticTransactionOptions txnOptions =
                    new OptimisticTransactionOptions().setSetSnapshot(true);
            final Transaction txn =
                    txnDb.beginTransaction(writeOptions, txnOptions)) {

            final Snapshot snapshot = txn.getSnapshot();

            // Write a key OUTSIDE of transaction
            txnDb.put(writeOptions, key1, value1);

            // Read a key using the snapshot.
            readOptions.setSnapshot(snapshot);
            final byte[] value = txn.getForUpdate(readOptions, key1, true);
            assert (value == null);

            try {
                // Attempt to commit transaction
                txn.commit();
                throw new IllegalStateException();
            } catch(final RocksDBException e) {
                // Transaction could not commit since the write outside of the txn
                // conflicted with the read!
                System.out.println(e.getStatus().getCode());
                assert(e.getStatus().getCode() == Status.Code.Busy);
            }
            txn.rollback();
        } finally {
            // Clear snapshot from read options since it is no longer valid
            readOptions.setSnapshot(null);
        }
    }

    private static void readCommitted_monotonicAtomicViews(
            final OptimisticTransactionDB txnDb, final WriteOptions writeOptions,
            final ReadOptions readOptions) throws RocksDBException {

        final byte keyX[] = "x".getBytes();
        final byte valueX[] = "x".getBytes();

        final byte keyY[] = "y".getBytes();
        final byte valueY[] = "y".getBytes();

        try (final OptimisticTransactionOptions txnOptions =
                     new OptimisticTransactionOptions().setSetSnapshot(true);
             final Transaction txn =
                     txnDb.beginTransaction(writeOptions, txnOptions)) {

            // Do some reads and writes to key "x"
            Snapshot snapshot = txnDb.getSnapshot();
            readOptions.setSnapshot(snapshot);
            byte[] value = txn.get(readOptions, keyX);
            txn.put(keyX, valueX);

            // Do a write outside of the transaction to key "y"
            txnDb.put(writeOptions, keyY, valueY);

            // Set a new snapshot in the transaction
            txn.setSnapshot();
            snapshot = txnDb.getSnapshot();
            readOptions.setSnapshot(snapshot);

            // Do some reads and writes to key "y"
            // Since the snapshot was advanced, the write done outside of the
            // transaction does not conflict.
            value = txn.getForUpdate(readOptions, keyY, true);
            txn.put(keyY, valueY);

            // Commit.  Since the snapshot was advanced, the write done outside of the
            // transaction does not prevent this transaction from Committing.
            txn.commit();

        } finally {
            // Clear snapshot from read options since it is no longer valid
            readOptions.setSnapshot(null);
        }
    }

    public static void snapshot() throws RocksDBException {
        Snapshot snapshot1 = rocksDB.getSnapshot();

        String key = "snapshotkey";
        String value = "snapshotvalue";
        rocksDB.put(key.getBytes(), value.getBytes());

        ReadOptions readOptions1 = new ReadOptions();
        readOptions1.setSnapshot(snapshot1);


        if (rocksDB.get(readOptions1, key.getBytes()) != null) {
            System.out.println(new String(rocksDB.get(readOptions1, key.getBytes())));
        }

        Snapshot snapshot2 = rocksDB.getSnapshot();
        ReadOptions readOptions2 = new ReadOptions();
        readOptions2.setSnapshot(snapshot2);
        System.out.println(new String(rocksDB.get(readOptions2, key.getBytes())));

    }

    public static void checkpoint() throws RocksDBException {
        Checkpoint checkpoint = Checkpoint.create(rocksDB);
        checkpoint.createCheckpoint(rocksdb_checkpoint_path);
        checkpoint.close();

    }

    public static void readBackup() throws RocksDBException {
        BackupableDBOptions backupableDBOptions = new BackupableDBOptions(rocksdb_backup_path);
        BackupEngine backupEngine = BackupEngine.open(rocksDB.getEnv(), backupableDBOptions);
        RestoreOptions restoreOptions = new RestoreOptions(true);
        backupEngine.restoreDbFromLatestBackup(rocksdb_read_backup_path, rocksdb_read_backup_path, restoreOptions);
        backupEngine.close();
    }

    public static void backup() throws RocksDBException {
        BackupableDBOptions backupableDBOptions = new BackupableDBOptions(rocksdb_backup_path);
        BackupEngine backupEngine = BackupEngine.open(rocksDB.getEnv(), backupableDBOptions);

        backupEngine.createNewBackup(rocksDB);
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

    public static void getData(String key) throws RocksDBException {
        byte[] bytes = rocksDB.get(key.getBytes());
        System.out.println(new String(bytes));
    }

    /*public static void printAllData() {
        RocksIterator rocksIterator = rocksDB.newIterator();
        rocksIterator.next();
    }*/

    public static void putData(String key,String value) throws RocksDBException {
        String k = key;
        String v = value;
        for (int i = 0; i < 100000; i++) {
            k = key + i;
            v = value + i;
            rocksDB.put(k.getBytes(), v.getBytes());
        }
    }
}
