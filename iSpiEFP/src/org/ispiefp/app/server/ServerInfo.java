package org.ispiefp.app.server;

import java.io.Serializable;

public class ServerInfo implements Serializable{
    private String entryname;
    private String hostname;
    private String username;
    private String password;
    private boolean hasLibEFP;
    private boolean hasGAMESS;
    private String libEFPPath;
    private String gamessPath;

    public ServerInfo(String entryname, boolean dummy){
        this.entryname = entryname;
    }

    public ServerInfo(String definedString){ //separates each term with a ;%;
        String[] parsedTerms = definedString.split(";%;");
        entryname = parsedTerms[0];
        hostname = parsedTerms[1];
        username = parsedTerms[2];
        password = parsedTerms[3];
        hasLibEFP = parsedTerms[4].equals("true");
        hasGAMESS = parsedTerms[5].equals("true");
        libEFPPath = parsedTerms[6];
        gamessPath = parsedTerms[7];
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

    public String getServerInfoDefinedString(){
        StringBuilder sb = new StringBuilder();
        sb.append(entryname);
        sb.append(";%;");
        sb.append(hostname);
        sb.append(";%;");
        sb.append(username);
        sb.append(";%;");
        sb.append(password);
        sb.append(";%;");
        sb.append(hasLibEFP);
        sb.append(";%;");
        sb.append(hasGAMESS);
        sb.append(";%;");
        sb.append(libEFPPath);
        sb.append(";%;");
        sb.append(gamessPath);
        return sb.toString();
    }
}
