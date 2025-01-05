package org.project;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try (ProxyServer proxyServer = new ProxyServer(port)) {
            proxyServer.run();
        } catch (IOException e) {
        }
        System.out.println("Hello world!");
    }
}