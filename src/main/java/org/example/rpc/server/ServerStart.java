package org.example.rpc.server;

import org.example.rpc.HelloService;
import org.example.rpc.HelloServiceImpl;

import java.io.IOException;

public class ServerStart {
    public static void main(String[] args) throws IOException {
        Server serviceServer = new ServiceCenter(8088);
        serviceServer.register(HelloService.class, HelloServiceImpl.class);
        serviceServer.start();
    }
}
