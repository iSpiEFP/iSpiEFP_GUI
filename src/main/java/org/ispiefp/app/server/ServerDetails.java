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

public class ServerDetails implements Serializable {

    private static final long serialVersionUID = -8906882033167247261L; // Check the importance of this again!

    private String serverName = "";

    private String address = "";


    private String serverType = "";

    private String workingDirectory = "";

    private String queueSystemType;

    private int port;

    private QueueOptions queueOptions;

    public ServerDetails() {
    }

    public ServerDetails(String serverName, String address, String serverType, int port) {
        this.serverName = serverName;
        this.address = address;
        this.serverType = serverType;
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getQueueSystemType() {
        return queueSystemType;
    }

    public void setQueueSystemType(String queueSystemType) {
        this.queueSystemType = queueSystemType;
    }

    public QueueOptions getQueueOptions() {
        return queueOptions;
    }

    public void setQueueOptions(QueueOptions queueOptions) {
        this.queueOptions = queueOptions;
    }

    public class QueueOptions implements Serializable {

        private static final long serialVersionUID = 8020104900103328295L;
        private String submit;
        private String query;
        private String kill;
        private String jobFileList;
        private String queueInfo;
        private String runFileTemplate;
        private int updateIntervalSecs;

        public QueueOptions() {

        }

        public String getSubmit() {
            return submit;
        }

        public void setSubmit(String submit) {
            this.submit = submit;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getKill() {
            return kill;
        }

        public void setKill(String kill) {
            this.kill = kill;
        }

        public String getJobFileList() {
            return jobFileList;
        }

        public void setJobFileList(String jobFileList) {
            this.jobFileList = jobFileList;
        }

        public String getQueueInfo() {
            return queueInfo;
        }

        public void setQueueInfo(String queueInfo) {
            this.queueInfo = queueInfo;
        }

        public String getRunFileTemplate() {
            return runFileTemplate;
        }

        public void setRunFileTemplate(String runFileTemplate) {
            this.runFileTemplate = runFileTemplate;
        }

        public int getUpdateIntervalSecs() {
            return updateIntervalSecs;
        }

        public void setUpdateIntervalSecs(int updateIntervalSecs) {
            this.updateIntervalSecs = updateIntervalSecs;
        }
    }

}
