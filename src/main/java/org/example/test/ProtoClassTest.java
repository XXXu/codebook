package org.example.test;

import java.io.IOException;

public class ProtoClassTest {
    public static void main(String[] args) {
        String protoFile = "person-entity.proto";
        String strCmd = "/root/protobuf/bin/protoc -I=./proto --java_out=./src/main/java/org/example/transwarp/protojava ./proto/"+ protoFile;
        try {
            Runtime.getRuntime().exec(strCmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
