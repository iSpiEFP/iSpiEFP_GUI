package org.ispiefp.app.server;

import java.io.Serializable;

public class ServerInfo implements Serializable{
    private String entryname;
    private String hostname;
    private String username;
    private boolean hasLibEFP;
    private boolean hasGAMESS;
    private String libEFPPath;
    private String gamessPath;

    public ServerInfo(String entryname){
        this.entryname = entryname;
    }

    public void setEntryname(String entryname) {
        this.entryname = entryname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHasLibEFP(boolean hasLibEFP) {
        this.hasLibEFP = hasLibEFP;
    }

    public void setHasGAMESS(boolean hasGAMESS) {
        this.hasGAMESS = hasGAMESS;
    }

    public void setLibEFPPath(String libEFPPath) {
        this.libEFPPath = libEFPPath;
    }

    public void setGamessPath(String gamessPath) {
        this.gamessPath = gamessPath;
    }

    public String getEntryname() {
        return entryname;
    }

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getGamessPath() {
        return gamessPath;
    }

    public String getLibEFPPath() {
        return libEFPPath;
    }

    public boolean hasGAMESS() {
        return hasGAMESS;
    }

    public boolean hasLibEFP() {
        return hasLibEFP;
    }
}
