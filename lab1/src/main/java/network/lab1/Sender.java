package network.lab1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

public class Sender {
    private final MulticastSocket socket;
    private final String name;
    public Sender(MulticastSocket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public void sendMessage(MessageType message, InetAddress myaddress, int port) {
        try {
            byte[] code = {(byte)message.getCode()};
            byte[] name = this.name.getBytes();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(code);
            stream.write(name);
            byte[] buffer = stream.toByteArray();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, myaddress, port);
            socket.send(packet);
            System.out.println("Message sent: " + message.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
