package org.project.events.message;

public record HandleAckMsgEvent(int senderID, long msgSeq) {
}
