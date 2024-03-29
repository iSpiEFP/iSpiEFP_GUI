/*
 * Copyright (c) 2006-2011 Christian Plattner. All rights reserved.
 * Please refer to the LICENSE.txt for licensing details.
 */
package ch.ethz.ssh2.sftp;

/**
 * Permissions for the 'permissions' field in the SFTP ATTRS data type.
 * <p>
 * "<i>The 'permissions' field contains a bit mask specifying file permissions.
 * These permissions correspond to the st_mode field of the stat structure
 * defined by POSIX [IEEE.1003-1.1996].</i>"
 *
 * @author Christian Plattner
 * @version 2.50, 03/15/10
 */
public class AttribPermissions {
    /* Octal values! */

    public static final int S_IRUSR = 0400;
    public static final int S_IWUSR = 0200;
    public static final int S_IXUSR = 0100;
    public static final int S_IRGRP = 0040;
    public static final int S_IWGRP = 0020;
    public static final int S_IXGRP = 0010;
    public static final int S_IROTH = 0004;
    public static final int S_IWOTH = 0002;
    public static final int S_IXOTH = 0001;
    public static final int S_ISUID = 04000;
    public static final int S_ISGID = 02000;
    public static final int S_ISVTX = 01000;
}
