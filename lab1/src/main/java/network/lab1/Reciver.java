package network.lab1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class Reciver {
    private final MulticastSocket socket;
    private MessageType lastMessage;
    private InetAddress lastIP;
    private String name;

    public Reciver(MulticastSocket socket) {
        this.socket = socket;
    }

    public void reciv(){
        try {
            byte[] buffer = new byte[100];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            byte code = buffer[0];
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            stream.skip(1);
            this.name = new String(stream.readAllBytes());
            lastMessage = MessageType.fromCode(code);
            lastIP = packet.getAddress();
            System.out.println("Message received: {" + lastMessage.getMessage() + "} from " + name + " " + packet.getAddress().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageType getLastMessage() {
        return lastMessage;
    }

    public InetAddress getLastIP() {
        return lastIP;
    }

    public String getName() {
        return name;
    }
}
