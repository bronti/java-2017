package ru.spbau.yaveyn.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServer implements AutoCloseable {
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public SimpleServer(int port) throws SimpleServerException {
        if (serverSocket != null) {
            throw new IllegalStateException("Server is already started.");
        }
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new SimpleServerException("Cannot connect to given port.", e);
        }

        new Thread(this::listenForClients).start();
    }

    @Override
    public void close() throws SimpleServerException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new SimpleServerException("Cannot close socket properly.", e);
        }
    }

    private void listenForClients() {
        try {
            while (true) {
                Socket client = serverSocket.accept();
                executorService.submit(new SimpleWireFormat(client));
            }
        } catch (SocketException e) {
            // server socket is closed.
        } catch (IOException e) {
            e.printStackTrace(); // There should be proper logging.
        }
    }
}
