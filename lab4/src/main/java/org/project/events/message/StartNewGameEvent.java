package org.project.events.message;

import org.project.model.GameState;
import org.project.model.communication.gameplayers.PlayerInfo;

public record StartNewGameEvent(GameState gameState, PlayerInfo playerInfo, String gameName) {
}
