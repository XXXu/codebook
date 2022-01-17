package org.example.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSTest {
    public static void main(String[] args) {
        String ip1 = args[0];
        String ip2 = args[1];

        boolean fag = true;

        while (true) {
            String name = null;
            if (fag) {
                name = getHostNameByIP(ip1);
                fag = false;
            } else {
                name = getHostNameByIP(ip2);
                fag = true;
            }
            System.out.println("name: " + name);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getHostNameByIP(String ip) {
        InetAddress address=null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address.getHostName();
    }
}
