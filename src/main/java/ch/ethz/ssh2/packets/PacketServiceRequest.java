/*
 * Copyright (c) 2006-2011 Christian Plattner. All rights reserved.
 * Please refer to the LICENSE.txt for licensing details.
 */
package ch.ethz.ssh2.packets;

import java.io.IOException;

/**
 * PacketServiceRequest.
 *
 * @author Christian Plattner
 * @version 2.50, 03/15/10
 */
public class PacketServiceRequest {
    byte[] payload;

    String serviceName;

    public PacketServiceRequest(String serviceName) {
        this.serviceName = serviceName;
    }

    public PacketServiceRequest(byte payload[], int off, int len) throws IOException {
        this.payload = new byte[len];
        System.arraycopy(payload, off, this.payload, 0, len);

        TypesReader tr = new TypesReader(payload, off, len);

        int packet_type = tr.readByte();

        if (packet_type != Packets.SSH_MSG_SERVICE_REQUEST)
            throw new IOException("This is not a SSH_MSG_SERVICE_REQUEST! ("
                    + packet_type + ")");

        serviceName = tr.readString();

        if (tr.remain() != 0)
            throw new IOException("Padding in SSH_MSG_SERVICE_REQUEST packet!");
    }

    public byte[] getPayload() {
        if (payload == null) {
            TypesWriter tw = new TypesWriter();
            tw.writeByte(Packets.SSH_MSG_SERVICE_REQUEST);
            tw.writeString(serviceName);
            payload = tw.getBytes();
        }
        return payload;
    }
}
