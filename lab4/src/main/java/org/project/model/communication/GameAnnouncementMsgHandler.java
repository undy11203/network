package org.project.model.communication;

import com.google.common.eventbus.EventBus;
import org.project.SnakesProto;
import org.project.events.message.HandleDiscoverMsgEvent;
import org.project.events.message.HandleGameAnnouncementMsgEvent;
import org.project.model.communication.converters.GameAnnouncementConverter;
import org.project.model.communication.udp.Socket;
import org.project.model.communication.udp.UDPMulticastMessageReceiver;

import java.io.IOException;

public class GameAnnouncementMsgHandler extends Thread {
    private final EventBus modelEventBus;

    public GameAnnouncementMsgHandler(EventBus modelEventBus) {
        this.modelEventBus = modelEventBus;
    }

    @Override
    public void run() {
        try {
            UDPMulticastMessageReceiver multicastMessageReceiver = UDPMulticastMessageReceiver.getInstance();
            while (!this.isInterrupted()) {

                Message message = multicastMessageReceiver.receive();
                Socket senderSocket = message.getSocket();
                SnakesProto.GameMessage msg = message.getMessage();

                if (msg.hasAnnouncement() && !this.isInterrupted()) {
                    msg.getAnnouncement().getGamesList().forEach(game ->
                            modelEventBus.post(new HandleGameAnnouncementMsgEvent(GameAnnouncementConverter.getInstance().snakesProtoToGameAnnouncement(game, senderSocket))));
                    continue;
                }

                if (msg.hasDiscover() && !this.isInterrupted()) {
                    modelEventBus.post(new HandleDiscoverMsgEvent(senderSocket));
                }

            }
            multicastMessageReceiver.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
