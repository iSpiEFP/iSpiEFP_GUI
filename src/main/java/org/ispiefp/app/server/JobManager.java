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

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Job Manager watches jobs, updates job status, inputs job data to database, and notifys user of job completion.
 * Job Manager also fetches finished job data from remote
 */
public class JobManager implements Runnable {
    private String username;
    private String password;
    private String hostname;
    private String jobID;
    private String title;
    private String date;
    private String status;
    private String type;
    private String outputFilename;
    private String errorOutputFileName;
    private String localWorkingDirectory;
    private String remoteWorkingDirectory;
    private ServerInfo server;
    private transient String keyPassword;
    private transient org.ispiefp.app.util.Connection conn;

    public JobManager(String username, String password, String hostname, String localWorkingDirectory, String jobID, String title,
                      String date, String status, String type) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.title = title;
        this.localWorkingDirectory = localWorkingDirectory;
        this.jobID = jobID;
        this.date = date;
        this.status = status;
        this.type = type;
        if (type.equalsIgnoreCase("LIBEFP")){
            remoteWorkingDirectory = "~/iSpiClient/Libefp/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            errorOutputFileName = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        } else if (type.equalsIgnoreCase("GAMESS")) {
            remoteWorkingDirectory = "~/iSpiClient/Gamess/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            errorOutputFileName = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        }
    }

    public JobManager(ServerInfo si, String localWorkingDirectory, String jobID, String title,
                      String date, String status, String type, String keyPassword){
        server = si;
        hostname = si.getHostname();
        this.localWorkingDirectory = localWorkingDirectory;
        this.jobID = jobID;
        this.title = title;
        this.date = date;
        this.status = status;
        this.type = type;
        if (type.equalsIgnoreCase("LIBEFP")){
            remoteWorkingDirectory = "~/iSpiClient/Libefp/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            errorOutputFileName = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        } else if (type.equalsIgnoreCase("GAMESS")) {
            remoteWorkingDirectory = "~/iSpiClient/Gamess/jobs/" + this.title.replace(" ", "_") + "/";
            outputFilename = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".out";
            errorOutputFileName = remoteWorkingDirectory + "output/" + this.title.replace(" ", "_") + ".err";
        }
        this.keyPassword = keyPassword;
    }

    public JobManager(String username, String password, String hostname, String type) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.type = type;
    }

    public JobManager(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
    }

    public JobManager() {

    }

    /*
     * Check whether the job is DONE or still in QUEUE
     */
    public boolean checkStatus(String jobID) throws IOException {
        boolean jobIsDone = false;
        org.ispiefp.app.util.Connection conn = new org.ispiefp.app.util.Connection(server, keyPassword);
        conn.connect();

        Session s = conn.openSession();
        s.execCommand(String.format("squeue --job=%s\n", jobID));

        // reading result
        StringBuilder outputString = new StringBuilder();
        int i;
        char c;
        try {
            InputStream output = s.getStdout();
            while ((i = output.read()) != -1) outputString.append((char) i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // extracting status from output
        String extractStatus = outputString.toString();
        System.out.println("JobManager 171: " + extractStatus);
        try {
            System.out.println(extractStatus.split("\n")[1].split(" +")[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            // should be done, because squeue doesn't have record
            return false;
        }
        s.close();
        conn.close();
        return jobIsDone;
    }

    public String generateJobID() {
        UUID jobID = UUID.randomUUID();
        /*
         * Should maybe check whether this ID exists already or not in the DATABASE
         * However the chance it does is REALLY REALLY REALLY low, also considering the
         * query has a time stamp as well, this is virtually unneccessary
         */
        return jobID.toString();
    }


    /*
     * Start a thread that watches a specific job and send a notification when the jobs completes
     */
    public void watchJobStatus() {
        (new Thread(new JobManager(this.server, this.localWorkingDirectory,
                this.jobID, this.title, this.date, this.status, this.type, this.keyPassword))).start();
    }

    /*
     * run thread for watch job status
     */
    public void run() {
        try {
            boolean jobIsDone = false;
            do {
                Thread.sleep(30000);
                System.out.println("polling");
                jobIsDone = checkStatus(this.jobID);
                if (jobIsDone) {
                    //update database
                    System.out.println("job finished...");
//                    updateDBStatus(this.jobID, this.title, this.date, "DONE", this.type);
                    notify(this.title, this.type);
                    if (this.type.equals("GAMESS")) {
                        //update the database with this efp file
                        String efp_file = getRemoteVmolOutput(this.jobID, this.type);
//                        sendEFPFile(efp_file);
                    }
                }
            } while (!jobIsDone);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Job: " + this.jobID + " is still running");
        }
    }


    /*
     * Send notification when job finishes
     */
    private void notify(String title, String type) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Alert alert = new Alert(AlertType.CONFIRMATION);
                String msg = "Your job: " + jobID + " has finished.";

                alert.setTitle(type + " Job Submission");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                Optional<ButtonType> result = alert.showAndWait();

            }
        });
    }

    /*
     * check status for an multitude of jobs, if status is changed, update database
     */
    public ArrayList<String[]> checkJobStatus(ArrayList<String[]> jobHistory) throws IOException {
        for (String[] line : jobHistory) {
            String job_id = line[0];
            String title = line[1];
            String date = line[2];
            String status = line[3];

            if (status.equals("QUEUE")) {
                // boolean done = askRemoteServerJobStatus(job_id, date);
                boolean done = checkStatus(job_id);
                //boolean done = askR
                if (done) {
                    System.out.println("Job:" + job_id + " done. updating database");
                    //updateDBStatus(job_id, title, date);
//                    updateDBStatus(job_id, title, date, status, type);
                    line[3] = "DONE";
                }
            }
        }
        return jobHistory;
    }

    /*
     * get output file from a lib efp job
     */
    public String getRemoteVmolOutput(String job_stamp, String type) throws IOException {
        org.ispiefp.app.util.Connection conn = new org.ispiefp.app.util.Connection(this.server, keyPassword);
        conn.connect();
        String path = new String();
        if (type.equals("LIBEFP")) {
            path = "iSpiClient/Libefp/output/output_" + job_stamp;
        } else if (type.equals("GAMESS")) {
            path = "iSpiClient/Gamess/output/gamess_" + job_stamp + ".efp";
        }

        SCPClient scp = conn.createSCPClient();
        SCPInputStream scpos = scp.get(path);
        InputStream stdout = new StreamGobbler(scpos);
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            //System.out.println("gobbler");
            System.out.println(line);
            sb.append(line + "\n");
            //output.add(line);
            //System.out.println(line);
        }
        // in.close();
        //br.close();
        scpos.close();
        conn.close();
        //br.close(); //OVIEN STRANGE RESULTS HERE 

        return sb.toString();
    }

    /*
     * get output file from a lib efp job
     */
    public String getRemoteFile(String filename) throws IOException {
        SCPInputStream scpos = null;
        InputStream stdout = null;
        BufferedReader br = null;
        org.ispiefp.app.util.Connection conn = null;
        StringBuilder sb = new StringBuilder();

        try {
            conn = new org.ispiefp.app.util.Connection(server, keyPassword);
            conn.connect();

            SCPClient scp = conn.createSCPClient();
            scpos = scp.get(filename);
            stdout = new StreamGobbler(scpos);
            br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                //System.out.println("gobbler");
                System.out.println(line);
                sb.append(line + "\n");
            }
            return sb.toString();

        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            conn.close();
        } finally {
            if (scpos != null) {
                //scpos.close();
            }
            if (stdout != null) {
                stdout.close();
            }
            if (br != null) {
                br.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return sb.toString();
    }


    public org.ispiefp.app.util.Connection getConn() {
        return conn;
    }

    public ServerInfo getServer() {return server;}

    public String getDate() {
        return date;
    }

    public String getHostname() {
        return hostname;
    }

    public String getLocalWorkingDirectory() {
        return localWorkingDirectory;
    }

    public String getJobID() {
        return jobID;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getOutputFilename(){
        return outputFilename;
    }

    public String getErrorOutputFileName() {
        return errorOutputFileName;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setConn(org.ispiefp.app.util.Connection conn) {
        this.conn = conn;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setLocalWorkingDirectory(String localWorkingDirectory) {
        this.localWorkingDirectory = localWorkingDirectory;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOutputFilename(String outputFilename){
        this.outputFilename = outputFilename;
    }

    public void setErrorOutputFileName(String errorOutputFileName) {
        this.errorOutputFileName = errorOutputFileName;
    }

    public String toJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

}
