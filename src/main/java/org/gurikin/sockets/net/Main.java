package org.gurikin.sockets.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        while (true) {
            try (
                    ServerSocket serverSocket = new ServerSocket(8080);
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    PrintWriter out =
                            new PrintWriter(socket.getOutputStream(), false)
            ) {
                String bodyLine;
                System.out.println("Headers:");
                while (true) {
                    bodyLine = in.readLine();
                    if (bodyLine == null || bodyLine.equals("")) break;
                    System.out.println(bodyLine);
                }
                StringBuilder payload = new StringBuilder();
                while (in.ready()) {
                    payload.append((char) in.read());
                }
                System.out.println("Payload data is: " + payload);
                System.out.println("===============");
                System.out.println("===============");
                System.out.println("===============");

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println(System.lineSeparator());
                out.println("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "\n" +
                        "<h1>My First Heading</h1>\n" +
                        "<p>My first paragraph.</p>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>");
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}