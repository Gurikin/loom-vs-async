package org.gurikin.sockets.net;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

@Slf4j
@RequiredArgsConstructor
public class ClientSocketThread extends Thread {
    private final Socket clientSocket;
    private final int clientNum;

    /**
     * This method is run by the thread when it executes. Subclasses of {@code
     * Thread} may override this method.
     *
     * <p> This method is not intended to be invoked directly. If this thread is a
     * platform thread created with a {@link Runnable} task then invoking this method
     * will invoke the task's {@code run} method. If this thread is a virtual thread
     * then invoking this method directly does nothing.
     *
     * @implSpec The default implementation executes the {@link Runnable} task that
     * the {@code Thread} was created with. If the thread was created without a task
     * then this method does nothing.
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             OutputStreamWriter out =
                     new OutputStreamWriter(clientSocket.getOutputStream())) {
            log.info("ClientSocketRunnable with number = {} has started.", clientNum);

            String bodyLine;
            log.info("Request Headers:");
            while (true) {
                bodyLine = in.readLine();
                if (bodyLine == null || bodyLine.equals("")) break;
                log.info(bodyLine);
            }
            StringBuilder payload = new StringBuilder();
            while (in.ready()) {
                payload.append((char) in.read());
            }
            log.info("Payload data is: " + payload);
            log.info("===============");
            log.info("===============");
            log.info("===============");

            out.append("HTTP/1.1 200 OK\n");
            out.append("Content-Type: text/html\n");
            out.append(System.lineSeparator());
            out.append("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "<h1>My First Heading</h1>\n" +
                    "<p>My first paragraph.</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>");
            out.flush();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Client with number = {} is closed.", clientNum);
        }
    }
}
