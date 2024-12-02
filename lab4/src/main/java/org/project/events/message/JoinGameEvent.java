package org.project.events.message;


import org.project.model.GameAnnouncement;
import org.project.model.Model;

public record JoinGameEvent(Model model, GameAnnouncement gameAnnouncement, String nickname, boolean isViewer) {
}
