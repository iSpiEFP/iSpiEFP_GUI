package org.libefp.app.installer;

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
        //////////////////////
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
