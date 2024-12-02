package org.project.events.message;


import org.project.model.GameState;

public record RenderGameFieldEvent(GameState gameState) {
}
