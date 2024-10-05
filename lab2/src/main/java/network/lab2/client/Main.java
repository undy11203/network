package network.lab2.client;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("d", "dns", true, "DNS name");
        options.addOption("i", "ip", true, "Ip address");
        options.addOption("f", "filepath", true, "Path to file");
        options.addOption("p", "port", true, "Port which server listen");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (!cmd.hasOption("dns") && !cmd.hasOption("ip")) {
                throw new ParseException("Необходимо указать либо --dns(-d), либо --ip(-i).");
            }
            if(!cmd.hasOption("port")){
                throw new ParseException("Необходим port");
            }
            if(!cmd.hasOption("filepath")){
                throw new ParseException("Необходим filepath");
            }

            Client handler;
            if(cmd.hasOption("dns")){
                handler = new Client(cmd.getOptionValue("dns"), cmd.getOptionValue("port"), cmd.getOptionValue("filepath"));
            }else {
                handler = new Client(cmd.getOptionValue("ip"), cmd.getOptionValue("port"), cmd.getOptionValue("filepath"));
            }

            handler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}