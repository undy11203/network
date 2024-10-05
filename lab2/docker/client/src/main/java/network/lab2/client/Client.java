package network.lab2.client;

import network.lab2.protocol.TransferHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private InetAddress ip;
    private int port;
    private File filepath;
    public Client(String address, String port, String filepath) throws Exception {
        checkCorrectAndSet(address, port, filepath);
    }

    public void start() {
        try(Socket socket = new Socket(ip, port)) {
            TransferHandler handler = new TransferHandler(socket);
            handler.send(filepath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCorrectAndSet(String address, String port, String filepath) throws FileNotFoundException {
        this.filepath = new File(filepath);
        if(!this.filepath.exists()){
            throw new FileNotFoundException("File not exists");
        }
        try {
            this.ip = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try{
            this.port = Integer.parseInt(port);
        } catch (ClassCastException e){
            throw new ClassCastException("это не порт");
        }
    }

}
