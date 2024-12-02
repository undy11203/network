package org.project.events.message;

import org.project.model.communication.udp.Socket;

public record HandleErrorMsgEvent(Socket senderSocket, long msgSeq) {
}
