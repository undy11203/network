package org.project.model.communication;

import org.project.SnakesProto;
import org.project.model.communication.udp.Socket;

public class Message {
    private final SnakesProto.GameMessage message;
    private final Socket socket;
    private long sentAt;

    public Message(SnakesProto.GameMessage message, Socket socket){
        this.message = message;
        this.socket = socket;
        sentAt = -1;
    }

    public Message(SnakesProto.GameMessage message, Socket socket, long sentAt){
        this.message = message;
        this.socket = socket;
        this.sentAt = sentAt;
    }

    public SnakesProto.GameMessage getMessage() {
        return message;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }
}