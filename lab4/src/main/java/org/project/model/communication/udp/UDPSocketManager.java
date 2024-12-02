package org.project.model.communication.udp;

import org.project.SnakesProto;
import org.project.model.communication.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

class UDPSocketManager {
    private static final UDPSocketManager INSTANCE = new UDPSocketManager();
    private static final int BUF_SIZE = 1460;
    private UDPSocketManager(){}
    static UDPSocketManager getInstance(){
        return INSTANCE;
    }
    void send(DatagramSocket socket, Message message) throws IOException {
        byte[] buff = message.getMessage().toByteArray();
        DatagramPacket msgPacket = new DatagramPacket(buff, buff.length, message.getSocket().address(), message.getSocket().port());
        socket.send(msgPacket);
    }

    Message receive(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[BUF_SIZE];
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagram);
        byte[] data = Arrays.copyOf(datagram.getData(), datagram.getLength());
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(data);
        return new Message(gameMessage, new Socket(datagram.getAddress(), datagram.getPort()), 0);
    }
}
