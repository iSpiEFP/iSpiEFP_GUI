/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

package org.ispiefp.app.installer;

import ch.ethz.ssh2.Connection;

import java.util.ArrayList;

/**
 * Manage all Local and Remote bundle Classes
 */
public class BundleManager {

    private String username;
    private String password;
    private String hostname;
    private String bundleType;
    private String workingDirectory;
    private Connection conn;

    private final String LOCAL = "LOCAL";
    private final String GAMESS = "GAMESS";
    private final String LIBEFP = "LIBEFP";

    //constructor for local files
    public BundleManager(String bundleType) {
        this.bundleType = bundleType;
        this.workingDirectory = System.getProperty("user.dir");
    }

    //constructor for remote files
    public BundleManager(String username, String password, String hostname, String bundleType, Connection conn) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.bundleType = bundleType;
        this.conn = conn;
    }

    public void manageLocal() {
        LocalBundleManager localManager = new LocalBundleManager();
        ArrayList<String> missingFiles = localManager.checkIfWorkingDirectoryIsReady();

        if (!missingFiles.isEmpty()) {
            localManager.installMissingFiles(missingFiles, this.bundleType);
        }
    }

    public boolean manageRemote() {
        System.out.println("needs:" + this.bundleType);
        RemoteBundleManager remoteBundleManager = new RemoteBundleManager(this.username, this.password, this.hostname, this.bundleType, this.conn);
        boolean packageReady = remoteBundleManager.checkIfPackageIsReady();
        //////////////////////
        //userIsMissingPackage = true; //tricked it for testing
        ////////////////////
        /*if(this.bundleType.equals(GAMESS)){
            System.out.println("Missing GAMESS Package");
            
            return true;
        } */

        if (!packageReady) {
            System.out.println("User is missing bundle:" + bundleType);
            boolean finishedInstalling = remoteBundleManager.installMissingPackage(bundleType);
            return finishedInstalling;
        } else {
            System.out.println("User already has bundle:" + bundleType);
            return true;
        }
    }

    public boolean uninstallLocal() {
        //TODO remove iSpiPackages from local machine
        return false;
    }

    public boolean uninstallRemote(String hostname) {
        //TODO  remove iSpiPackages from remote host
        return false;
    }

}
