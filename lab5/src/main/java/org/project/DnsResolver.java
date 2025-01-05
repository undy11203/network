package org.project;

import lombok.Getter;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

@Getter
public class DnsResolver {
    public static final int DNS_PORT = 53;
    private final DatagramChannel dnsChannel;
    private final HashMap<Integer, Map.Entry<Integer, SelectionKey>> clientMatch;
    private int senderID = 0;

    public DnsResolver(DatagramChannel dnsChannel) {
        this.dnsChannel = dnsChannel;
        this.clientMatch = new HashMap<>();
    }

    public void resolve(byte[] addr, int port, SelectionKey key) throws IOException {
        Message message = new Message();
        Record record = Record.newRecord(Name.fromString(
                new String(addr, StandardCharsets.UTF_8) + '.'), Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        Header header = message.getHeader();
        header.setFlag(Flags.AD);
        header.setFlag(Flags.RD);
        header.setID(senderID);

        clientMatch.put(senderID, new AbstractMap.SimpleEntry<>(port, key));
        senderID++;

        dnsChannel.write(ByteBuffer.wrap(message.toWire()));
    }

}
