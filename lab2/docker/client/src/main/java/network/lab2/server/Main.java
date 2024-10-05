package network.lab2.server;

import org.apache.commons.cli.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("p", "port", true, "Port which server listen");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if(!cmd.hasOption("port")){
                throw new ParseException("Необходим port");
            }

            String savePath = "./uploads";
            Path directoryPath = Paths.get(savePath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            ServerSocket socket = new ServerSocket(Integer.parseInt(cmd.getOptionValue("port")));

            while(!socket.isClosed()) {
                Socket client = socket.accept();
                System.out.println("123-1");
                ConnectionHandler handler = new ConnectionHandler(client, directoryPath);
                System.out.println("123-2");
                Thread thread = new Thread(handler);
                System.out.println("123-3");
                handler.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
