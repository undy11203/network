package org.project.model.communication.udp;

import org.project.model.communication.Message;

import java.io.IOException;
import java.net.DatagramSocket;

public class UDPMessageReceiverAndSender {
    private final DatagramSocket socket;
    private static UDPMessageReceiverAndSender instance;

    private UDPMessageReceiverAndSender(DatagramSocket socket) {
        this.socket = socket;
    }

    public static UDPMessageReceiverAndSender getInstance() throws IOException {
        if (instance == null) {
            DatagramSocket socket = new DatagramSocket();
            instance = new UDPMessageReceiverAndSender(socket);
        }
        return instance;
    }

    public void send(Message message) throws IOException {
        UDPSocketManager.getInstance().send(socket, message);
    }

    public Message receive() throws IOException {
        return UDPSocketManager.getInstance().receive(socket);
    }

    public void close() {
        instance = null;
        socket.close();
    }
}
