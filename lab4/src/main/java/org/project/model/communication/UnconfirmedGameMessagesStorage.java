package org.project.model.communication;

import org.project.model.communication.udp.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnconfirmedGameMessagesStorage {
    private final List<Message> unconfirmedMessages = new ArrayList<>();

    public void add(Message message){
        unconfirmedMessages.add(message);
    }

    public void confirm(long msgSeq){
        unconfirmedMessages.removeIf(message -> message.getMessage().getMsgSeq() == msgSeq);
    }

    public void cancelConfirmation(int playerId){
        unconfirmedMessages.removeIf(message -> message.getMessage().getReceiverId() == playerId);
    }

    public void replaceDestination(int oldReceiverId, Socket newSenderSocket){
        unconfirmedMessages.replaceAll(message ->
                message.getMessage().getReceiverId() == oldReceiverId ?
                        new Message(message.getMessage(), newSenderSocket) : message);
    }

    public List<Message> getUnconfirmedMessages() {
        return Collections.unmodifiableList(unconfirmedMessages);
    }
}