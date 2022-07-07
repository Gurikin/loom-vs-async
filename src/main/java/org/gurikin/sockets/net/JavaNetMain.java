package org.gurikin.sockets.net;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class JavaNetMain {

    public static void main(String[] args) {
        int cnt = 0;
        try (
                ServerSocket serverSocket = new ServerSocket(8080);
        ) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientSocketThread(socket, ++cnt).start();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}