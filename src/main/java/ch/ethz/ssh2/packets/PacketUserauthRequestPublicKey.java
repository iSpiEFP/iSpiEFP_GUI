/*
 * Copyright (c) 2006-2011 Christian Plattner. All rights reserved.
 * Please refer to the LICENSE.txt for licensing details.
 */
package ch.ethz.ssh2.packets;

import java.io.IOException;

/**
 * PacketUserauthRequestPublicKey.
 *
 * @author Christian Plattner
 * @version 2.50, 03/15/10
 */
public class PacketUserauthRequestPublicKey {
    byte[] payload;

    String userName;
    String serviceName;
    String password;
    String pkAlgoName;
    byte[] pk;
    byte[] sig;

    public PacketUserauthRequestPublicKey(String serviceName, String user,
                                          String pkAlgorithmName, byte[] pk, byte[] sig) {
        this.serviceName = serviceName;
        this.userName = user;
        this.pkAlgoName = pkAlgorithmName;
        this.pk = pk;
        this.sig = sig;
    }

    public PacketUserauthRequestPublicKey(byte payload[], int off, int len) throws IOException {
        this.payload = new byte[len];
        System.arraycopy(payload, off, this.payload, 0, len);

        TypesReader tr = new TypesReader(payload, off, len);

        int packet_type = tr.readByte();

        if (packet_type != Packets.SSH_MSG_USERAUTH_REQUEST)
            throw new IOException("This is not a SSH_MSG_USERAUTH_REQUEST! ("
                    + packet_type + ")");

        throw new IOException("Not implemented!");
    }

    public byte[] getPayload() {
        if (payload == null) {
            TypesWriter tw = new TypesWriter();
            tw.writeByte(Packets.SSH_MSG_USERAUTH_REQUEST);
            tw.writeString(userName);
            tw.writeString(serviceName);
            tw.writeString("publickey");
            tw.writeBoolean(true);
            tw.writeString(pkAlgoName);
            tw.writeString(pk, 0, pk.length);
            tw.writeString(sig, 0, sig.length);
            payload = tw.getBytes();
        }
        return payload;
    }
}
