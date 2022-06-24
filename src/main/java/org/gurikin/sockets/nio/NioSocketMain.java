package org.gurikin.sockets.nio;

public class NioSocketMain {
    public static void main(String[] args) {
        NioSocketServer server = new NioSocketServer();
        server.start();
    }
}
