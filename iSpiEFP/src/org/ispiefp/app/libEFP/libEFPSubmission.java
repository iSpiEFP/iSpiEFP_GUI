package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class libEFPSubmission {

    /* Refers to the type of scheduling system that the server the job was submitted to uses */
    private enum SchedulingType{
        PBS,
        SLURM,
        TORQUE
    }
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
    String inputFilePath;
    String outputFilename;

    public static String REMOTE_LIBEFP_DIR  = "iSpiClient/Libefp/";
    public static String REMOTE_LIBEFP_JOBS = REMOTE_LIBEFP_DIR + "jobs/";
    public String REMOTE_LIBEFP_IN;
    public String REMOTE_LIBEFP_OUT;
    public String REMOTE_LIBEFP_FRAGS;

    public libEFPSubmission(ServerInfo server, String jobName){
        this.server = server;
        hostname = server.getHostname();
        username = server.getUsername();
        password = server.getPassword();
        if (server.hasLibEFP()){
            efpmdPath = server.getLibEFPPath();
        }
        this.jobName = jobName;
        REMOTE_LIBEFP_FRAGS = REMOTE_LIBEFP_JOBS + jobName + "/fraglib/";
        REMOTE_LIBEFP_OUT = REMOTE_LIBEFP_JOBS + jobName +"/output/";
        REMOTE_LIBEFP_IN = REMOTE_LIBEFP_JOBS + jobName +"/input/";
    }

    public libEFPSubmission(){
        super();
    }
    abstract String submit(String input);
    abstract File createSubmissionScript(String input) throws IOException;
    abstract String getSubmissionScriptText();
    abstract void prepareJob(String efpmdPath, String inputFilePath, String outputFilename);

    public boolean createJobWorkspace(String jobID){
        String jobDirectory = getJobDirectory(jobID);
        String command = String.format("mkdir %s; cd %s; mkdir input; mkdir output; mkdir fraglib",
                jobDirectory, jobDirectory);
        try {
            org.ispiefp.app.util.Connection con = new org.ispiefp.app.util.Connection(server);
            con.connect();
            Session s = con.getActiveConnection().openSession();
            /* Check to see if a job directory of this name already exists */
            boolean directoryExists = false;
            try {
                SFTPv3Client sftp = new SFTPv3Client(con.getActiveConnection());
                sftp.ls(jobDirectory);
                System.err.println("This directory already exists");
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

                if (!choice.isPresent() || choice.get().getButtonData().isCancelButton()){
                    System.err.println("User wants to rename the directory so as to not overwrite a job. Returning...");
                    return false;
                }

            } catch(IOException e){
                System.err.println("This directory does not exist");
            }
            s.execCommand(command);
            System.out.println("Executed command: " + command);
            s.close();
            return true;
        } catch (IOException e){
            return false;
        }
    }

    public boolean sendInputFile(File inputFile) throws IOException {
        org.ispiefp.app.util.Connection con = new org.ispiefp.app.util.Connection(server);
        boolean authorized = con.connect();
        if (authorized) {
            /* Copy input file to the server */
            SCPClient scp = con.createSCPClient();
            SCPOutputStream scpos = scp.put(inputFilePath, inputFile.length(), REMOTE_LIBEFP_IN, "0666");
            FileInputStream in = new FileInputStream(inputFile);
            IOUtils.copy(in, scpos);
            //Wait for each file to actually be on the server
            while (true) {
                try {
                    SCPInputStream scpis = scp.get(REMOTE_LIBEFP_IN + inputFile.getName());
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
        }
        else {
            System.err.println("Was unable to authenticate the user");
            return false;
        }
    }

    public boolean sendEFPFiles(ArrayList<File> efpFiles) throws IOException {
        org.ispiefp.app.util.Connection con = new Connection(server);
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
        }
        else {
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

    public void setInputFilename(String filename){ this.inputFilePath = filename + ".in"; }

    public void setOutputFilename(String outputFilename) { this.outputFilename = outputFilename + ".out"; }

    public void setSchedulerOutputName(String filename) { this.schedulerOutputName = filename + ".err"; }

    public String getJobDirectory(String jobName){ return REMOTE_LIBEFP_JOBS + jobName + "/"; }

    public String getUsername() { return username; }

    public String getHostname() { return hostname; }

    public String getPassword() { return password; }

    public String getJobName() { return jobName; }

    public String getOutputFilename() { return outputFilename; }

    public String getSchedulerOutputName() { return schedulerOutputName; }

    public void setQueueName(String name){ queueName = name; }

    public void setNumNodes(int numNodes) { this.numNodes = numNodes; }

    public void setNumProcessors(int numProcessors) { this.numProcessors = numProcessors; }

    public void setWalltime(String walltime) { this.walltime = walltime; }

    public void setMem(int mem) { this.mem = mem; }

    public String getJobInputDirectory() { return REMOTE_LIBEFP_IN; }

    public String getJobOutputDirectory() { return REMOTE_LIBEFP_OUT; }

    public String getJobFragmentDirectory() { return REMOTE_LIBEFP_JOBS; }
}
