package org.project;

import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ProxyServer implements AutoCloseable{
    static final byte SOCKS_5 = 0x05;
    static final byte NO_AUTH_REQUIRED = 0x00;
    static final byte IPV4 = 0x01;
    static final byte DNS = 0x03;
    static final byte IPV6 = 0x04;
    static final byte RSV = 0x00;
    static final byte CONNECT = 0x01;
    static final byte SUCCESS = 0x00;
    static final byte SERVER_FAIL = 0x01;
    static final byte CONNECTION_NOT_ALLOWED = 0x02;
    static final byte HOST_UNREACHABLE = 0x04;
    static final byte COMMAND_NOT_SUP = 0x07;
    static final byte ADDR_TYPE_NOT_SUP = 0x08;
    static final byte NO_ACCEPT_METHOD = (byte) 0xFF;
    static final int IPV4_LENGTH = 4;
    static final int IPV6_LENGTH = 16;

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final DnsResolver dnsResolver;
    private final int port;

    public ProxyServer(int port) throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress("localhost", port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        DatagramChannel dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        dnsChannel.register(selector, SelectionKey.OP_READ);
        dnsChannel.connect(new InetSocketAddress(ResolverConfig.getCurrentConfig().servers().get(0).getAddress(), DnsResolver.DNS_PORT));

        dnsResolver = new DnsResolver(dnsChannel);
        this.port = port;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                int readyChannels = selector.select();
                if (readyChannels >= 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        try {
                            if (key.isValid()) {
                                if (key.isAcceptable()) {
//                                    log.info("Accept client");
                                    acceptClient();
                                } else if (key.isConnectable()) {
//                                    log.info("Connect");
                                    connect(key);
                                } else if (key.isReadable()) {
//                                    log.info("read: ");
                                    if (key.channel() instanceof DatagramChannel) {
//                                        log.info("read dns answer");
                                        readDnsAnswer(key);
                                    } else {
//                                        log.info("read other");
                                        readData(key);
                                    }
                                } else if (key.isWritable()) {
//                                    log.info("write");
                                    writeData(key);
                                }
                            }
                        } catch (IOException e) {
                            closeConnection(key);
                        } catch (IllegalArgumentException e) {
                            closeConnection(key);
                        }catch (RuntimeException e){
                        }
                    }
                    selector.selectedKeys().clear();
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void acceptClient() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Attachment attachment = ((Attachment) key.attachment());
        channel.finishConnect();

        sendSuccessAnswer((SocketChannel) attachment.getDstKey().channel());

        attachment.getDstKey().interestOps(SelectionKey.OP_READ);
        key.interestOps(0);
    }

    private void writeData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        if (!attachment.getOutputBuffer().hasRemaining()) {
            return;
        }

        int bytesWrite = channel.write(attachment.getOutputBuffer());

        if (bytesWrite == -1) {
            throw new IllegalArgumentException("Bytes write = -1");
        } else {
            attachment.getOutputBuffer().flip();
            attachment.getOutputBuffer().clear();
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_READ);
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void readData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        if (attachment == null) {
            attachment = new Attachment();
            key.attach(attachment);
        }

        int bytesRead = channel.read(attachment.getInputBuffer());

        if (bytesRead == 0 || bytesRead == -1) {

        } else if (attachment.getDstKey() == null) {
            readHeader(key, bytesRead);
        } else {
            key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
            attachment.getDstKey().interestOps(attachment.getDstKey().interestOps() | SelectionKey.OP_WRITE);
            attachment.getInputBuffer().flip();
        }
    }

    private void readHeader(SelectionKey key, int length) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();
        byte[] header = attachment.getInputBuffer().array();
        if (header.length < 3) {
            sendFailAnswer(clientChannel, SERVER_FAIL);
            throw new IllegalArgumentException("Incorrect header");
        }
        switch (attachment.getStatus()) {
            case Attachment.AUTH -> {
                if (header[0] != SOCKS_5) {
                    sendFailAnswer(clientChannel, NO_ACCEPT_METHOD);
                    throw new IllegalArgumentException("Incorrect SOCKS version");
                }
                authenticationHandler(header, clientChannel, attachment);
                attachment.getInputBuffer().flip();
                attachment.getInputBuffer().clear();
            }
            case Attachment.REQUEST -> {
                requestHandler(header, key, clientChannel, length);
                attachment.getInputBuffer().flip();
                attachment.getInputBuffer().clear();
            }
        }
    }

    private void sendFailAnswer(SocketChannel channel, byte flag) throws IOException {
        byte[] ans = new byte[]{SOCKS_5, flag, RSV};
        channel.write(ByteBuffer.wrap(ans));
    }

    private void sendSuccessAnswer(SocketChannel channel) throws IOException {
        ByteBuffer responseBuffer = ByteBuffer.allocate(6 + InetAddress.getLoopbackAddress().getAddress().length);
        responseBuffer.put(SOCKS_5);
        responseBuffer.put(SUCCESS);
        responseBuffer.put(RSV);
        responseBuffer.put(IPV4);
        responseBuffer.put(InetAddress.getLoopbackAddress().getAddress());

        responseBuffer.putShort((short) port);

        responseBuffer.flip();
        channel.write(responseBuffer);
    }

    private void requestHandler(byte[] header, SelectionKey key, SocketChannel clientChannel, int length) throws IOException {
        if (header[1] == CONNECT) {
            byte[] addr;
            int port = (header[length - 2] & 0xFF) << 8 | (header[length - 1] & 0xFF);
            switch (header[3]) {
                case IPV4 -> {
                    addr = Arrays.copyOfRange(header, 4, 4 + IPV4_LENGTH);
                    connectToSite(addr, port, key);
                }
                case DNS -> {
                    int domainLength = header[4] & 0xFF;
                    addr = Arrays.copyOfRange(header, 5, 5 + domainLength);
                    dnsResolver.resolve(addr, port, key);
                }
                case IPV6 -> {
                    addr = Arrays.copyOfRange(header, 4, 4 + IPV6_LENGTH);
                    connectToSite(addr, port, key);
                }
                default -> {
                    sendFailAnswer(clientChannel, ADDR_TYPE_NOT_SUP);
                    throw new IllegalArgumentException("Bad Ip address type = " + header[3]);
                }
            }
        } else {
            sendFailAnswer(clientChannel, COMMAND_NOT_SUP);
            throw new IllegalArgumentException("Bad CMD in second header");
        }
    }

    private void authenticationHandler(byte[] header, SocketChannel clientChannel, Attachment attachment) throws IOException {
        if (checkAuthMethod(header)) {
            clientChannel.write(ByteBuffer.wrap(new byte[]{SOCKS_5, NO_AUTH_REQUIRED}));
            attachment.setStatus(Attachment.REQUEST);
        } else {
            sendFailAnswer(clientChannel, CONNECTION_NOT_ALLOWED);
            throw new IllegalArgumentException("Client has not NoAuth method");
        }
    }

    private boolean checkAuthMethod(byte [] header) {
        int NMethods = header[1];
        for (int i = 0; i < NMethods; i++) {
            if (header[i + 2] == NO_AUTH_REQUIRED) {
                return true;
            }
        }
        return false;
    }

    private void readDnsAnswer(SelectionKey key) throws IOException {
        ByteBuffer ans = ByteBuffer.allocate(8192);
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        int bytesRead = datagramChannel.read(ans);
        ans.flip();
        if(bytesRead > 0) {
            Message message = new Message(ans);
            int senderId = message.getHeader().getID();
            List<Record> answerRecords = message.getSection(Section.ANSWER);
            InetAddress ipAddress = answerRecords.stream().
                    filter(it -> it instanceof ARecord).
                    limit(1).
                    map(it -> (ARecord) it).
                    findAny().
                    orElseThrow(() -> new RuntimeException("No dns resolve")).
                    getAddress();

            connectToSite(ipAddress.getAddress(),
                    dnsResolver.getClientMatch().get(senderId).getKey(),
                    dnsResolver.getClientMatch().get(senderId).getValue());
        }
    }

    private void connectToSite(byte[] addr, int port, SelectionKey key) throws IOException {
        try {
            SocketChannel siteChanel = SocketChannel.open();
            siteChanel.configureBlocking(false);
            siteChanel.connect(new InetSocketAddress(InetAddress.getByAddress(addr), port));

            SelectionKey dstKey = siteChanel.register(key.selector(), SelectionKey.OP_CONNECT);
            key.interestOps(0);

            Attachment attachment = (Attachment) key.attachment();
            attachment.setDstKey(dstKey);

            Attachment dstAttachment = new Attachment();
            dstAttachment.setDstKey(key);
            dstKey.attach(dstAttachment);

            attachment.setOutputBuffer(dstAttachment.getInputBuffer());
            dstAttachment.setOutputBuffer(attachment.getInputBuffer());


        } catch (UnknownHostException exc) {
            sendFailAnswer((SocketChannel) key.channel(), HOST_UNREACHABLE);
            throw new IllegalArgumentException("Unknown Host Exception");
        }
    }

    private void closeConnection(SelectionKey key) {
//        SocketChannel channel = (SocketChannel) key.channel();
//        try {
//            channel.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        key.cancel();
    }

    @Override
    public void close() throws IOException {
//        serverChannel.close();
//        dnsResolver.close();
//        selector.close();
    }
}
