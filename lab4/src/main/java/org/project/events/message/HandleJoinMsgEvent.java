package org.project.events.message;

import org.project.SnakesProto;
import org.project.model.communication.udp.Socket;

public record HandleJoinMsgEvent(SnakesProto.GameMessage.JoinMsg joinMsg, Socket senderSocket, long msgSeq) {
}
