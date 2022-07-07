package org.gurikin.sockets.loom;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LoomMain {

    public static void main(String[] args) {
        int cnt = 0;
        try (
                ServerSocket serverSocket = new ServerSocket(8080);
        ) {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread.startVirtualThread(new ClientSocketRunnable(socket, ++cnt));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}