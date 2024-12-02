package org.project.events.message;

import org.project.model.communication.NodeRole;
import org.project.model.communication.udp.Socket;

public record HandleRoleChangeMsgEvent(NodeRole senderRole, NodeRole receiverRole, int senderId, int receiverId, Socket senderSocket,
                                       long msgSeq) {
}
