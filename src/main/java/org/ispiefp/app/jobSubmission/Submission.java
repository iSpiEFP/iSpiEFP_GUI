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

package org.ispiefp.app.jobSubmission;

import ch.ethz.ssh2.*;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.Connection;

import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public abstract class Submission {

    /* Refers to the type of scheduling system that the server the job was submitted to uses */
    private enum SchedulingType {
        PBS,
        SLURM,
        TORQUE
    }

    /* Class specific fields */
    String submissionType;
    /* Server commands and fields */
    ServerInfo server;
    SchedulingType type;
    String schedulingScript;
    String hostname;
    String username;
    String password;
    String submitString;
    String queryString;
    String killString;
    String jobFileListString;
    String queueInfoString;
    String jobName;
    int updateIntervalSecs;

    /* Submission Script Fields */
    String schedulerOutputName;
    String queueName;
    int numNodes;
    int numProcessors;
    String walltime;
    int mem;
    String efpmdPath;
    String gamessPath;
    String inputFilePath;
    String outputFilename;

    public static String REMOTE_LIBEFP_DIR = "~/iSpiClient/Libefp/";
    public static String REMOTE_LIBEFP_JOBS = REMOTE_LIBEFP_DIR + "jobs/";
    public String REMOTE_LIBEFP_IN;
    public String REMOTE_LIBEFP_OUT;
    public String REMOTE_LIBEFP_FRAGS;
    public static String REMOTE_GAMESS_DIR = "~/iSpiClient/Gamess/";
    public static String REMOTE_GAMESS_JOBS = REMOTE_GAMESS_DIR + "jobs/";
    public String REMOTE_GAMESS_IN;
    public String REMOTE_GAMESS_OUT;

    public Submission(ServerInfo server, String jobName, String submissionType) {
        assert (submissionType.equalsIgnoreCase("LIBEFP") || submissionType.equalsIgnoreCase("GAMESS"));
        this.submissionType = submissionType;
        this.server = server;
        hostname = server.getHostname();
        username = server.getUsername();
        password = server.getPassword();
        if (server.hasLibEFP()) {
            efpmdPath = server.getLibEFPPath();
        }
        if (server.hasGAMESS()) {
            gamessPath = server.getGamessPath();
        }
        this.jobName = jobName;
        REMOTE_LIBEFP_FRAGS = REMOTE_LIBEFP_JOBS + jobName.replace(" ", "_") + "/fraglib/";
        REMOTE_LIBEFP_OUT = REMOTE_LIBEFP_JOBS + jobName.replace(" ", "_") + "/output/";
        REMOTE_LIBEFP_IN = REMOTE_LIBEFP_JOBS + jobName.replace(" ", "_") + "/input/";
        REMOTE_GAMESS_OUT = REMOTE_GAMESS_JOBS + jobName.replace(" ", "_") + "/output/";
        REMOTE_GAMESS_IN = REMOTE_GAMESS_JOBS + jobName.replace(" ", "_") + "/input/";
        setInputFilename(jobName);
        setOutputFilename(jobName);
        setSchedulerOutputName(jobName);
    }

    public Submission() {
        super();
    }

    public abstract String submit(String input, String pemKey);

    abstract File createSubmissionScript(String input) throws IOException;

    public abstract String getLibEFPSubmissionScriptText();

    public abstract String getGAMESSSubmissionScriptText();

    abstract void prepareJob(String efpmdPath, String inputFilePath, String outputFilename);

    public boolean createJobWorkspace(String inputJobName, Connection con) {
        String jobName = inputJobName.replace(" ", "_");
        String jobDirectory = getJobDirectory(jobName);
        String command = submissionType.equalsIgnoreCase("LIBEFP") ?
                String.format("mkdir %s; cd %s; mkdir input; mkdir output; mkdir fraglib;", jobDirectory, jobDirectory) :
                String.format("mkdir %s; cd %s; mkdir input; mkdir output;", jobDirectory, jobDirectory);

        setInputFilename(jobName);
        setOutputFilename(jobName);
        setSchedulerOutputName(jobName);

        boolean directoryExists = false;
        try {
            boolean isAuthenticated = con.connect();
            if (!isAuthenticated) {
                System.err.println("Was unable to authenticate user");
                con.close();
                return false;
            }

            Session s = con.openSession();
            s.startShell();
            /* Check to see if a job directory of this name already exists */

            String cmd = "ls " + jobDirectory + " && exit\n";
            System.out.println("Submission 184: " + cmd);

            PrintWriter writer = new PrintWriter(s.getStdin());
            writer.println(cmd);
            writer.close();

            // reading stdout
            String out = ReadStream(s.getStdout());

            // reading stderr
            String err = ReadStream(s.getStderr());

            System.out.println("Submission 198, StdOut: " + out);
            System.out.println("Submission 199, StdErr: " + err);

            // Check if directory exists
            if (err.length() > 0 && err.contains("cannot access")) {
                System.out.println("No directory exists");
                directoryExists = false;
            }
            if (out.length() > 0 && out.contains("input")) {
                System.out.println("Directory Exists");
                directoryExists = true;
            }

            if (directoryExists) {
                // Dialog
                Dialog<ButtonType> directoryAlreadyExistsDialog = new Dialog<>();
                directoryAlreadyExistsDialog.setTitle("Warning: Directory Already Exists on Server");

                // Set the button types.
                ButtonType loginButtonType = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
                directoryAlreadyExistsDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
                String warningMessage = "A job with this title already exists on the server. If you press continue, that\n" +
                        "job will be overwritten by the job you are now creating. Press cancel to go\nback and choose a " +
                        "different name or continue to overwrite that job.";
                Text warningText = new Text(warningMessage);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                grid.add(warningText, 0, 0);
                directoryAlreadyExistsDialog.getDialogPane().setContent(grid);
                Optional<ButtonType> choice = directoryAlreadyExistsDialog.showAndWait();

                if (!choice.isPresent() || choice.get().getButtonData().isCancelButton()) {
                    System.err.println("User wants to rename the directory so as to not overwrite a job. Returning...");
                    return false;
                }
            }


            // execute create directory command, recreate session, because previous exited
            s.close();
            s = con.openSession();
            s.startShell();
            writer = new PrintWriter(s.getStdin());
            writer.println(command);
            writer.close();

            s.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String ReadStream(InputStream stream) throws IOException {
        StringBuilder outputString = new StringBuilder();
        InputStream inputStream = new StreamGobbler(stream);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            outputString.append(line);
            outputString.append('\n');
        }
        br.close();
        return outputString.toString();
    }

    public boolean sendInputFile(File inputFile, String pemKey) throws IOException {
        org.ispiefp.app.util.Connection con = new org.ispiefp.app.util.Connection(server, pemKey);
        boolean authorized = con.connect();
        if (authorized) {
            /* Copy input file to the server */
            SCPClient scp = con.createSCPClient();
            SCPOutputStream scpos = scp.put(inputFilePath, inputFile.length(), getJobInputDirectory(), "0666");
            FileInputStream in = new FileInputStream(inputFile);
            IOUtils.copy(in, scpos);
            //Wait for each file to actually be on the server
            while (true) {
                try {
                    SCPInputStream scpis = scp.get(getJobInputDirectory() + inputFile.getName());
                    scpis.close();
                } catch (IOException e) {
                    continue;
                }
                break;
            }

            /* Close resources */
            in.close();
            scpos.close();
            Session sess = con.openSession();
            sess.close();
            return true;
        } else {
            System.err.println("Was unable to authenticate the user");
            return false;
        }
    }

    public boolean sendEFPFiles(ArrayList<File> efpFiles, String pemKey) throws IOException {
        org.ispiefp.app.util.Connection con = new Connection(server, pemKey);
        boolean authorized = con.connect();
        if (authorized) {
            for (File file : efpFiles) {
                SCPClient scpClient = con.createSCPClient();
                String filename = file.getName().substring(0, file.getName().indexOf('.') + 4);
                filename = filename.toLowerCase();
                SCPOutputStream scpos = scpClient.put(filename, file.length(), REMOTE_LIBEFP_FRAGS, "0666");
                System.out.printf("Creating new FIS: %s%n", file.getPath());
                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, scpos);
                in.close();
                scpos.close();
                //Wait for each file to actually be on the server
                while (true) {
                    try {
                        SCPInputStream scpis = scpClient.get(REMOTE_LIBEFP_FRAGS + filename);
                        scpis.close();
                    } catch (IOException e) {
                        continue;
                    }
                    break;
                }
            }
            return true;
        } else {
            System.err.println("Was unable to authenticate the user");
            return false;
        }
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setJobFileListString(String jobFileListString) {
        this.jobFileListString = jobFileListString;
    }

    public void setKillString(String killString) {
        this.killString = killString;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setQueueInfoString(String queueInfoString) {
        this.queueInfoString = queueInfoString;
    }

    public void setSchedulingScript(String schedulingScript) {
        this.schedulingScript = schedulingScript;
    }

    public void setSubmitString(String submitString) {
        this.submitString = submitString;
    }

    public void setType(SchedulingType type) {
        this.type = type;
    }

    public void setUpdateIntervalSecs(int updateIntervalSecs) {
        this.updateIntervalSecs = updateIntervalSecs;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setInputFilename(String filename) {
        this.inputFilePath = submissionType.equalsIgnoreCase("LIBEFP") ?
                filename + ".in" : filename + ".inp";
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename + ".out";
    }

    public void setSchedulerOutputName(String filename) {
        this.schedulerOutputName = filename + ".err";
    }

    public String getJobDirectory(String jobName) {
        return submissionType.equalsIgnoreCase("LIBEFP") ?
                REMOTE_LIBEFP_JOBS + jobName + "/" :
                REMOTE_GAMESS_JOBS + jobName + "/";
    }

    public String getUsername() {
        return username;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPassword() {
        return password;
    }

    public String getJobName() {
        return jobName;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public String getSchedulerOutputName() {
        return schedulerOutputName;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setQueueName(String name) {
        queueName = name;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public void setNumProcessors(int numProcessors) {
        this.numProcessors = numProcessors;
    }

    public void setWalltime(String walltime) {
        this.walltime = walltime;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public String getJobInputDirectory() {
        return submissionType.equalsIgnoreCase("LIBEFP") ?
                REMOTE_LIBEFP_IN : REMOTE_GAMESS_IN;
    }

    public String getJobOutputDirectory() {
        return submissionType.equalsIgnoreCase("LIBEFP") ?
                REMOTE_LIBEFP_OUT : REMOTE_GAMESS_OUT;
    }

    public String getJobFragmentDirectory() {
        return REMOTE_LIBEFP_JOBS;
    }
}
