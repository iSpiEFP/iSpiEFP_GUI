/*
 * Copyright (c) 2006-2011 Christian Plattner. All rights reserved.
 * Please refer to the LICENSE.txt for licensing details.
 */
package ch.ethz.ssh2.packets;

import java.io.IOException;

/**
 * PacketChannelWindowAdjust.
 *
 * @author Christian Plattner
 * @version 2.50, 03/15/10
 */
public class PacketChannelWindowAdjust {
    byte[] payload;

    public int recipientChannelID;
    public int windowChange;

    public PacketChannelWindowAdjust(int recipientChannelID, int windowChange) {
        this.recipientChannelID = recipientChannelID;
        this.windowChange = windowChange;
    }

    public PacketChannelWindowAdjust(byte payload[], int off, int len) throws IOException {
        this.payload = new byte[len];
        System.arraycopy(payload, off, this.payload, 0, len);

        TypesReader tr = new TypesReader(payload, off, len);

        int packet_type = tr.readByte();

        if (packet_type != Packets.SSH_MSG_CHANNEL_WINDOW_ADJUST)
            throw new IOException(
                    "This is not a SSH_MSG_CHANNEL_WINDOW_ADJUST! ("
                            + packet_type + ")");

        recipientChannelID = tr.readUINT32();
        windowChange = tr.readUINT32();

        if (tr.remain() != 0)
            throw new IOException("Padding in SSH_MSG_CHANNEL_WINDOW_ADJUST packet!");
    }

    public byte[] getPayload() {
        if (payload == null) {
            TypesWriter tw = new TypesWriter();
            tw.writeByte(Packets.SSH_MSG_CHANNEL_WINDOW_ADJUST);
            tw.writeUINT32(recipientChannelID);
            tw.writeUINT32(windowChange);
            payload = tw.getBytes();
        }
        return payload;
    }
}
