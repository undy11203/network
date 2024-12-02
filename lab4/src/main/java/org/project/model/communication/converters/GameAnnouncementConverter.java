package org.project.model.communication.converters;

import org.project.SnakesProto;
import org.project.model.GameAnnouncement;
import org.project.model.communication.udp.Socket;

public class GameAnnouncementConverter {
    private static final GameAnnouncementConverter INSTANCE = new GameAnnouncementConverter();

    private GameAnnouncementConverter() {
    }

    public static GameAnnouncementConverter getInstance() {
        return INSTANCE;
    }

    public GameAnnouncement snakesProtoToGameAnnouncement(SnakesProto.GameAnnouncement gameAnnouncement,
                                                          Socket senderSocket) {
        return new GameAnnouncement(senderSocket, gameAnnouncement.getGameName(),
                gameAnnouncement.getPlayers().getPlayersCount(),
                GameConfigConverter.getInstance().snakesProtoToGameConfig(gameAnnouncement.getConfig()),
                gameAnnouncement.getCanJoin());
    }
}
