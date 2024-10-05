package network.lab2.protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class TransferHandler {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private String filename;

    public TransferHandler(Socket socket) throws IOException {
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }

    public void send(File filepath) throws IOException {
        String fileName = filepath.getName();
        long fileSize = filepath.length();

        outputStream.writeInt(fileName.length());
        outputStream.write(fileName.getBytes(StandardCharsets.UTF_8));
        outputStream.writeLong(fileSize);

        try (FileInputStream fileInputStream = new FileInputStream(filepath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        boolean isSuccess = inputStream.readBoolean();
        if (isSuccess) {
            System.out.println("Прием файла завершился успешно.");
        } else {
            System.out.println("Прием файла завершился неудачно.");
        }
    }

    public void recieve(Path path) throws IOException {
        int titleLength = inputStream.readInt();
        byte[] fileName = new byte[titleLength];
        int read = inputStream.read(fileName, 0, titleLength);
        this.filename = new String(fileName);

        long fileSize = inputStream.readLong();

        File outputFile = new File(String.valueOf(path), new String(fileName, StandardCharsets.UTF_8));
        try(FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            long bytesRecieve = 0;
            long bytesInstantRecieve = 0;
            int bytesRead;
            long timeA = System.currentTimeMillis();
            double totalTime = 0;

            while(bytesRecieve < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesRecieve += bytesRead;
                bytesInstantRecieve += bytesRead;

                long timeB = System.currentTimeMillis();
                if(timeB - timeA >= 3000 || bytesRecieve == fileSize) {
                    totalTime += timeB - timeA;
                    displayTime(timeA, timeB, bytesRecieve, bytesInstantRecieve, totalTime);
                    timeA = timeB;
                    bytesInstantRecieve = 0;
                }
            }
            fileOutputStream.flush();
            outputStream.writeBoolean( bytesRecieve == fileSize);
            System.out.println(bytesRecieve == fileSize ? "Файл принят успешно" : "Файл сломался");
            outputStream.flush();
        } catch (IOException e) {
            throw new IOException("Проблема во время приёма файлаы");
        }
    }

    private void displayTime(long pastTime, long nowTime, long bytesRecieveAll, long bytesReadNow, double totalTime) {
        long deltaTime = nowTime - pastTime;
        double instantSpeed = (double) bytesReadNow / (double) deltaTime * 1000;
        double avaregeTime = (double) bytesRecieveAll / (double) totalTime * 1000;

        System.out.printf("Текущая скорость (байт/сек) %.2f, (MB/s) %.2f | Файл: %s\n", instantSpeed, instantSpeed/ (1024 * 1024), this.filename);
        System.out.printf("Средняя скорость (байт/сек) %.2f, (MB/s) %.2f | Файл: %s\n", avaregeTime, avaregeTime/ (1024 * 1024), this.filename);

    }

}
