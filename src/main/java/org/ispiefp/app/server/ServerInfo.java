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

package org.ispiefp.app.server;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerInfo implements Serializable {

    private String entryname;
    private String hostname;
    private String username;
    private String password;
    private String sshKeyLocation;
    private boolean sshFileEncrypted; // true if encrypted
    private boolean sshKeyMethod; // true if user is using ssh key, false if user is using password
    private boolean hasLibEFP;
    private boolean hasGAMESS;
    private String libEFPPath;
    private String gamessPath;
    private String scheduler;
    private ArrayList<String> queues;

    public ServerInfo(String entryname, boolean dummy){
        this.entryname = entryname;
    }

    public ServerInfo(String definedString){ //separates each term with a ;%;
        try {
            // new version
            queues = new ArrayList<>();
            String[] parsedTerms = definedString.split(";%;");
            entryname = parsedTerms[0];
            hostname = parsedTerms[1];
            username = parsedTerms[2];
            sshKeyMethod = parsedTerms[3].equals("true");
            sshFileEncrypted = parsedTerms[4].equals("true");
            if (sshKeyMethod) sshKeyLocation = parsedTerms[5];
            else password = parsedTerms[5];
            hasLibEFP = parsedTerms[6].equals("true");
            hasGAMESS = parsedTerms[7].equals("true");
            libEFPPath = parsedTerms[8];
            gamessPath = parsedTerms[9];
            scheduler = parsedTerms[10];
            if (parsedTerms.length > 11) {
                String queueString = parsedTerms[11];
                String[] parsedQueues = queueString.split("#@#");
                for (int i = 0; i < parsedQueues.length; i++) {
                    queues.add(parsedQueues[i]);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // old version
            queues = new ArrayList<>();
            String[] parsedTerms = definedString.split(";%;");
            entryname = parsedTerms[0];
            hostname = parsedTerms[1];
            username = parsedTerms[2];
            sshKeyMethod = false;
            sshFileEncrypted = false;
            password = parsedTerms[3];
            hasLibEFP = parsedTerms[4].equals("true");
            hasGAMESS = parsedTerms[5].equals("true");
            libEFPPath = parsedTerms[6];
            gamessPath = parsedTerms[7];
            scheduler = parsedTerms[8];
            if (parsedTerms.length > 9) {
                String queueString = parsedTerms[9];
                String[] parsedQueues = queueString.split("#@#");
                for (int i = 0; i < parsedQueues.length; i++) {
                    queues.add(parsedQueues[i]);
                }
            }
        }
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

    public void setPassword(String password) {
        this.password = password;
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

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public void addQueue(String queueName){
        queues.add(queueName);
    }

    public void deleteQueue(String queueName){
        queues.remove(queueName);
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

    public String getPassword() {
        return password;
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

    public String getScheduler() {
        return scheduler;
    }

    public ArrayList<String> getQueues() { return queues; }

    public String getSshKeyLocation() {
        System.out.println(sshKeyLocation);
        return sshKeyLocation;
    }

    public void setSshKeyLocation(String sshKeyLocation) {
        this.sshKeyLocation = sshKeyLocation;
    }

    public boolean isSshFileEncrypted() {
        return sshFileEncrypted;
    }

    public void setSshFileEncrypted(boolean sshFileEncrypted) {
        this.sshFileEncrypted = sshFileEncrypted;
    }

    public boolean isSshKeyMethod() {
        return sshKeyMethod;
    }

    public void setSshKeyMethod(boolean sshKeyMethod) {
        this.sshKeyMethod = sshKeyMethod;
    }

    public String getServerInfoDefinedString(){
        StringBuilder sb = new StringBuilder();
        sb.append(entryname);
        sb.append(";%;");
        sb.append(hostname);
        sb.append(";%;");
        sb.append(username);
        sb.append(";%;");
        sb.append(sshKeyMethod);
        sb.append(";%;");
        sb.append(sshFileEncrypted);
        sb.append(";%;");
        if (sshKeyMethod) {
            sb.append(sshKeyLocation);
            sb.append(";%;");
        } else {
            sb.append(password);
            sb.append(";%;");
        }
        sb.append(hasLibEFP);
        sb.append(";%;");
        sb.append(hasGAMESS);
        sb.append(";%;");
        sb.append(libEFPPath);
        sb.append(";%;");
        sb.append(gamessPath);
        sb.append(";%;");
        sb.append(scheduler);
        sb.append(";%;");
//        for(String queue : queues){
//            sb.append(queue);
//            sb.append("#@#");
//        }
        return sb.toString();
    }
}
