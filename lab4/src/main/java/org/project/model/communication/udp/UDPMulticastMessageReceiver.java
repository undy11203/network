package org.project.model.communication.udp;

import org.project.model.communication.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import static org.project.model.communication.udp.MulticastConfig.MULTICAST_ADDRESS;
import static org.project.model.communication.udp.MulticastConfig.MULTICAST_PORT;

public class UDPMulticastMessageReceiver {
    private static final String NETWORK_INTERFACE_NAME = "wlan0";
    private final MulticastSocket socket;
    private static UDPMulticastMessageReceiver instance;

    private UDPMulticastMessageReceiver(MulticastSocket socket) {
        this.socket = socket;
    }

    public static UDPMulticastMessageReceiver getInstance() throws IOException {
        if (instance == null) {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, 0);
            NetworkInterface networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE_NAME);
            socket.joinGroup(group, networkInterface);
            instance = new UDPMulticastMessageReceiver(socket);
        }
        return instance;
    }

    public Message receive() throws IOException {
        return UDPSocketManager.getInstance().receive(socket);
    }

    public void close() {
        instance = null;
        socket.close();
    }
}
