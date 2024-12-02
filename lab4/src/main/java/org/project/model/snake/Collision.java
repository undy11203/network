package org.project.model.snake;

public record Collision(int killerId, int victimId) {
    public boolean isSuicide() {
        return killerId == victimId;
    }
}
