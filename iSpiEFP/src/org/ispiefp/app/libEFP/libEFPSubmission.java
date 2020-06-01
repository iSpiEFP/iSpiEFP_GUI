package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.server.ServerDetails;
import org.ispiefp.app.server.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.UUID;

public abstract class libEFPSubmission {

    /* Refers to the type of scheduling system that the server the job was submitted to uses */
    private enum SchedulingType{
        PBS,
        SLURM,
        TORQUE
    }
    /* Server commands and fields */
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
    int updateIntervalSecs;

    /* Submission Script Fields */
    String stdoutFilename;
    String queueName;
    int numNodes;
    int numProcessors;
    String walltime;
    int mem;
    String efpmdPath;
    String inputFilePath;
    String outputFilename;

    public static String REMOTE_LIBEFP_DIR = "iSpiClient/Libefp";
    public static String REMOTE_LIBEFP_OUT = REMOTE_LIBEFP_DIR + "/output/";
    public static String REMOTE_LIBEFP_IN = REMOTE_LIBEFP_DIR + "/input";

    public libEFPSubmission(ServerInfo server){
        hostname = server.getHostname();
        username = server.getUsername();
        password = server.getPassword();
    }
    abstract String submit(String efpmdPath, String inputFilePath, String outputFilename);
    abstract File createSubmissionScript(String efpmdPath, String inputFilePath, String outputFilename, String schedulerOutputName) throws IOException;

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
}
