package org.project.events.message;


import org.project.model.communication.udp.Socket;
import org.project.model.snake.Direction;

public record HandleSteerMsgEvent(Direction newDirection, Socket senderSocket, long msgSeq) {
}
