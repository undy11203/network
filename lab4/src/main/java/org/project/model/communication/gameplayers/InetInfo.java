package org.project.model.communication.gameplayers;

import org.project.model.communication.udp.Socket;

import java.net.InetAddress;
import java.time.Instant;

public class InetInfo {
    private final Socket socket;
    private long lastCommunicationTime;

    public InetInfo(InetAddress inetAddress, int port, long lastCommunicationTime) {
        this.socket = new Socket(inetAddress, port);
        this.lastCommunicationTime = lastCommunicationTime;
    }

    public InetInfo(Socket socket, long lastCommunicationTime) {
        this.socket = socket;
        this.lastCommunicationTime = lastCommunicationTime;
    }

    long getLastCommunicationTime() {
        return lastCommunicationTime;
    }

    public void updateLastCommunication() {
        lastCommunicationTime = Instant.now().toEpochMilli();
    }

    public Socket getSocket() {
        return socket;
    }
}
