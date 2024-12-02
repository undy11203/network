package org.project.events.message;


import org.project.model.GameState;
import org.project.model.communication.gameplayers.GamePlayer;
import org.project.model.communication.udp.Socket;

import java.util.List;

public record HandleGameStateMsgEvent(GameState newGameState, List<GamePlayer> players, Socket senderSocket,
                                      long msgSeq) {
}
