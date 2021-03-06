package org.gurikin.sockets.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NioSocketServerWithCompletionHandler {

    AsynchronousServerSocketChannel serverChannel;
    AsynchronousSocketChannel clientChannel;

    public NioSocketServerWithCompletionHandler() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8080);
            serverChannel.bind(hostAddress);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                serverChannel.accept(
                        "Client connection", new CompletionHandler<AsynchronousSocketChannel, Object>() {

                            @Override
                            public void completed(
                                    AsynchronousSocketChannel result, Object attachment) {
                                try {
                                    if (serverChannel.isOpen()) {
                                        serverChannel.accept("Client connection", this);
                                    }
                                    System.out.println("Connection with client opened.");

                                    clientChannel = result;
                                    if ((clientChannel != null) && (clientChannel.isOpen())) {
                                        ReadWriteHandler handler = new ReadWriteHandler();
                                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                                        Map<String, Object> readInfo = new HashMap<>();
                                        readInfo.put("action", "read");
                                        readInfo.put("buffer", buffer);

                                        clientChannel.read(buffer, readInfo, handler);
                                    }
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void failed(Throwable exc, Object attachment) {
                                System.out.println("Connection with client is closed.");
                                throw new RuntimeException(exc);
                            }
                        });
                System.out.println(System.in.read());
            } catch (RuntimeException | IOException e) {
                System.out.println("Connection with client is closed.");
                throw new RuntimeException(e);
            }
        }
    }

    class ReadWriteHandler implements
            CompletionHandler<Integer, Map<String, Object>> {

        @Override
        public void completed(
                Integer result, Map<String, Object> attachment) {
            try {
                String action = (String) attachment.get("action");

                if ("read".equals(action)) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    ByteBuffer inBuffer = ((ByteBuffer) attachment.get("buffer")).flip();
                    String request = new String(StandardCharsets.UTF_8.decode(inBuffer).array());
                    String agent = Arrays.stream(request.split("\n"))
                            .filter(s -> s.contains("User-Agent"))
                            .findFirst().orElse("Unknown agent");
                    System.out.println("Agent: ".concat(agent));
                    buffer.put("HTTP/1.1 200 OK\n".getBytes(StandardCharsets.UTF_8));
                    buffer.put("Content-Type: text/html\n".getBytes(StandardCharsets.UTF_8));
                    buffer.put("X-Firefox-Spdy: h2\n\n".getBytes(StandardCharsets.UTF_8));
                    buffer.put(("<!DOCTYPE html>\n"
                            .concat("<html>\n")
                            .concat("<body>\n")
                            .concat("<h1>Requested Headers</h1>\n")
                            .concat("<p>")
                            .concat(request.replace(System.lineSeparator(), "</br>"))
                            .concat("</p>\n")
                            .concat("</body>\n")
                            .concat("</html>\n")).getBytes(StandardCharsets.UTF_8));
                    buffer.flip();
                    attachment.put("action", "write");

                    clientChannel.write(buffer, attachment, this);
                    buffer.clear();
                    clientChannel.close();

                } else if ("write".equals(action)) {
                    ByteBuffer buffer = ByteBuffer.allocate(32);

                    attachment.put("action", "read");
                    attachment.put("buffer", buffer);

                    clientChannel.read(buffer, attachment, this);
                }
            } catch (RuntimeException | IOException e) {
                this.failed(e, attachment);
            }
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            String message = (exc instanceof AsynchronousCloseException) ? "Client connection is closed." : "Handle of request is interrupted.";
            System.out.println(message);
        }
    }
}
