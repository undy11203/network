package org.project.events.message;


import org.project.model.GameAnnouncement;

import java.util.List;

public record UpdateAvailableGamesEvent(List<GameAnnouncement> availableGames) {
}
