package network.lab2.server;

import network.lab2.protocol.TransferHandler;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

public class ConnectionHandler implements Runnable {
    private final Path path;
    private final Socket socket;

    public ConnectionHandler(Socket client, Path path) {
        this.socket = client;
        this.path = path;
    }

    @Override
    public void run() {
        System.out.println("exec in new thread");
        TransferHandler handler = null;
        try {
            handler = new TransferHandler(socket);
            handler.recieve(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
