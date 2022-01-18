package org.example.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class FilePermission {
    public static void io() throws IOException {
        File file = new File("/root/filePermission.txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        System.out.println("File is created!");
        //Runtime.getRuntime().exec("chmod 777 /home/test3.txt");
        System.out.println("is execute allow : " + file.canExecute());
        System.out.println("is read allow : " + file.canRead());
        System.out.println("is write allow : " + file.canWrite());
    }

    public static void nio() throws IOException {
        File file = new File("/root/test");
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Path path = Paths.get(file.getAbsolutePath());
        Files.setPosixFilePermissions(path, perms);

    }

    public static void changePermission(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Path path = Paths.get(file.getAbsolutePath());
        Files.setPosixFilePermissions(path, perms);
    }

    public static void main(String[] args) throws IOException {
//        io();
        nio();
    }


}
