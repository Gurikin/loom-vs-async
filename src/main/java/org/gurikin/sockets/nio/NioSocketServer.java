package org.gurikin.sockets.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NioSocketServer {

    AsynchronousServerSocketChannel server;
    Future<AsynchronousSocketChannel> socketChannelFuture;
    AsynchronousSocketChannel worker;
    boolean completed = false;

    public NioSocketServer() {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8080));
            socketChannelFuture = server.accept();
            worker = socketChannelFuture.get(10, TimeUnit.SECONDS);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            while ((worker != null) && (worker.isOpen()) /* && !completed */) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future<Integer> readResult = worker.read(buffer);

                // perform other computations
                readResult.get();

                buffer.flip();
                char[] headers = StandardCharsets.US_ASCII.decode(buffer).array();
                System.out.println(headers);
                Future<Integer> writeResult = worker.write(buffer);

                // perform other computations
                if (headers[headers.length - 1] == 10 && headers[headers.length - 3] == 10) {
                    writeResult.get(1L, TimeUnit.MILLISECONDS);
                    buffer.clear();
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            stopServer(server);
            closeWorker(worker);
        }
    }

    private void stopServer(AsynchronousServerSocketChannel server) {
        try {
            Objects.requireNonNull(server).close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void closeWorker(AsynchronousSocketChannel worker) {
        try {
            Objects.requireNonNull(worker).close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
