/*
 * Copyright (c) 2006-2011 Christian Plattner. All rights reserved.
 * Please see the LICENSE.txt for licensing details.
 */
package ch.ethz.ssh2;

import ch.ethz.ssh2.sftp.ErrorCodes;

import java.io.IOException;

/**
 * Used in combination with the SFTPv3Client. This exception wraps
 * error messages sent by the SFTP server.
 *
 * @author Christian Plattner
 * @version 2.50, 03/15/10
 */

public class SFTPException extends IOException {
    private static final long serialVersionUID = 578654644222421811L;

    private final String sftpErrorMessage;
    private final int sftpErrorCode;

    private static String constructMessage(String s, int errorCode) {
        String[] detail = ErrorCodes.getDescription(errorCode);

        if (detail == null)
            return s + " (UNKNOWN SFTP ERROR CODE)";

        return s + " (" + detail[0] + ": " + detail[1] + ")";
    }

    SFTPException(String msg, int errorCode) {
        super(constructMessage(msg, errorCode));
        sftpErrorMessage = msg;
        sftpErrorCode = errorCode;
    }

    /**
     * Get the error message sent by the server. Often, this
     * message does not help a lot (e.g., "failure").
     *
     * @return the plain string as sent by the server.
     */
    public String getServerErrorMessage() {
        return sftpErrorMessage;
    }

    /**
     * Get the error code sent by the server.
     *
     * @return an error code as defined in the SFTP specs.
     */
    public int getServerErrorCode() {
        return sftpErrorCode;
    }

    /**
     * Get the symbolic name of the error code as given in the SFTP specs.
     *
     * @return e.g., "SSH_FX_INVALID_FILENAME".
     */
    public String getServerErrorCodeSymbol() {
        String[] detail = ErrorCodes.getDescription(sftpErrorCode);

        if (detail == null)
            return "UNKNOWN SFTP ERROR CODE " + sftpErrorCode;

        return detail[0];
    }

    /**
     * Get the description of the error code as given in the SFTP specs.
     *
     * @return e.g., "The filename is not valid."
     */
    public String getServerErrorCodeVerbose() {
        String[] detail = ErrorCodes.getDescription(sftpErrorCode);

        if (detail == null)
            return "The error code " + sftpErrorCode + " is unknown.";

        return detail[1];
    }
}
