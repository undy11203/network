package org.project.events.message;


import org.project.model.communication.udp.Socket;

public record HandlePingMsgEvent(Socket senderSocket, long msgSeq) {
}
