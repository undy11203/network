package network.lab1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

class MulticastDiscovery {
    private final int PORT = 8081;
    private final MulticastSocket socket;
    private final HashMap<String, InetAddress> activeMemebers = new HashMap<>();
    private final HashMap<String, Long> lastResponseTimes = new HashMap<>();
    private final InetAddress multicastGroupAddress;
    private Sender sender;
    private Reciver reciver;
    private ScheduledExecutorService sheduler;
    private Thread handlerThread;
    private static final long RESPONSE_TIMEOUT = 11000;
    private String name;

    public MulticastDiscovery(String ip, String name) throws IOException {
        this.name = name;
        multicastGroupAddress = InetAddress.getByName(ip);

        socket = new MulticastSocket(PORT);
        sender = new Sender(socket, name);
        reciver = new Reciver(socket);
    }

    public void start() throws IOException {
        socket.joinGroup(multicastGroupAddress);
        handlerThread = new Thread(() -> {
            try {
                handler();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        handlerThread.start();
        sheduler = Executors.newScheduledThreadPool(1);
        if(InetAddress.getLocalHost().getHostAddress().endsWith("4")){
            sender.sendMessage(MessageType.CHECKIN, multicastGroupAddress, PORT);
            sheduler.schedule(() -> {
                try {
                    stop();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 10, TimeUnit.SECONDS);
        }else {
            sheduler.scheduleAtFixedRate(this::sendCheckMessage, 0, 10, TimeUnit.SECONDS);
        }
    }

    private void stop() throws IOException {
        sender.sendMessage(MessageType.LEAVE, multicastGroupAddress, PORT);
        handlerThread.interrupt();
        socket.leaveGroup(multicastGroupAddress);
        socket.close();
        sheduler.shutdown();
    }

    private void handler() throws IOException {
        while(true) {
            reciver.reciv();
            if(reciver.getLastMessage() == MessageType.CHECKIN){
                lastResponseTimes.put(reciver.getName(), System.currentTimeMillis());
                if(!activeMemebers.containsKey(reciver.getName())){
                    activeMemebers.put(reciver.getName(), reciver.getLastIP());
                    displayActiveMember();
                    sender.sendMessage(MessageType.CHECKIN, multicastGroupAddress, PORT);

                }
                Set<String> inactiveMembers = new HashSet<>();
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<String, InetAddress> entry : activeMemebers.entrySet()) {
                    if (currentTime - lastResponseTimes.get(entry.getKey()) > RESPONSE_TIMEOUT) {
                        inactiveMembers.add(entry.getKey());
                    }
                }
                for (String member : inactiveMembers) {
                    lastResponseTimes.remove(member);
                    activeMemebers.remove(member);
                }
                displayActiveMember();
            } else if(reciver.getLastMessage() == MessageType.LEAVE &&
                      InetAddress.getLocalHost().getHostAddress().equals(reciver.getLastIP().getHostAddress())){
                break;
            } else if(reciver.getLastMessage() == MessageType.LEAVE) {
                lastResponseTimes.remove(reciver.getName());
                activeMemebers.remove(reciver.getName());
                displayActiveMember();
            }
        }
    }

    private void sendCheckMessage() {
        sender.sendMessage(MessageType.CHECKIN, multicastGroupAddress, PORT);
    }

    private void displayActiveMember(){
        System.out.println("-----Active copy-----");
        System.out.println("Total copy: " + activeMemebers.size());
        for (Map.Entry<String, InetAddress> entry : activeMemebers.entrySet()){
            System.out.println("Copy: " + entry.getKey() + " " + entry.getValue().toString());
        }
        System.out.println("---------- ----------");
    }
}


public class Main {
    public static void main(String[] args) throws IOException {
        StringBuilder name = new StringBuilder("COPY-");
        name.append(ProcessHandle.current().pid());
        MulticastDiscovery multicastDiscovery = new MulticastDiscovery(args[0], name.toString());
        multicastDiscovery.start();
    }
}