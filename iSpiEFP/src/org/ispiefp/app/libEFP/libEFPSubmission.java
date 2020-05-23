package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.server.ServerDetails;

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

    abstract String submit(String efpmdPath, String inputFilePath, String outputFilename);
    abstract File createSubmissionScript(String efpmdPath, String inputFilePath, String outputFilename) throws IOException;

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
/* PBS scripts are executed from the home directory and should therefore use the entire path */
class libEFPPBSSubmission extends libEFPSubmission {

    public libEFPPBSSubmission(){
        submitString = "/usr/pbs/bin/qsub ${JOB_NAME}.run";
        queryString = "/usr/pbs/bin/qstat -f ${JOB_ID}";
        killString = "/usr/pbs/bin/qdel ${JOB_ID}";
        jobFileListString = "find ${JOB_DIR} -type f";
        queueInfoString = "/usr/pbs/bin/qstat -fQ";
    }

    File createSubmissionScript(String efpmdPath, String inputFilePath, String outputFilename) throws IOException {
        String template = String.format(
                "#!/bin/bash\n" +
                "# FILENAME: %s\n" +
                "#PBS -q %s\n" +
                "#PBS -l nodes=%d:ppn=%d\n" +
                "#PBS -l walltime=%s\n" +
                "%s %s > %s",
                stdoutFilename, queueName, numNodes, numProcessors, walltime,
                efpmdPath, inputFilePath, outputFilename
        );

        File submissionScript = new File("submission.pbs");
        FileUtils.writeStringToFile(submissionScript, template, Charset.forName("UTF-8"));
        return submissionScript;
    }

    public String submit(String efpmdPath, String inputFilePath, String outputFilename) {
        String jobID = UUID.randomUUID().toString();
        try {
            File submissionScript = createSubmissionScript(efpmdPath, inputFilePath, outputFilename);
            Connection con = new Connection(hostname);
            boolean isAuthenticated = con.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                System.err.println("Was unable to authenticate user");
                con.close();
                return "Error: User could not be authenticated";
            }
            SCPClient scp = con.createSCPClient();
            String remoteFileName = "submission_" + jobID + ".slurm";
            scp.put(remoteFileName, submissionScript.length(), LocalBundleManager.LIBEFP_INPUTS, "0666");
            Session s = con.openSession();
            s.execCommand("cd " + LocalBundleManager.LIBEFP);
            s.execCommand(String.format("sbatch %s", LocalBundleManager.LIBEFP_INPUTS + remoteFileName));
            return jobID;
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Error: See stack trace";
    }
}
class libEFPSlurmSubmission extends libEFPSubmission {

    public libEFPSlurmSubmission(String stdoutFilename, String queueName, int numNodes, int numProcessors,
                                 String walltime, int mem){

//        submitString = "/usr/pbs/bin/qsub ${JOB_NAME}.run";
//        queryString = "/usr/pbs/bin/qstat -f ${JOB_ID}";
//        killString = "/usr/pbs/bin/qdel ${JOB_ID}";
//        jobFileListString = "find ${JOB_DIR} -type f";
//        queueInfoString = "/usr/pbs/bin/qstat -fQ";
//
        this.stdoutFilename = stdoutFilename;
        this.queueName = queueName;
        this.numNodes = numNodes;
        this.numProcessors = numProcessors;
        this.walltime = walltime;
        this.mem = mem;
    }

    File createSubmissionScript(String efpmdPath, String inputFilePath, String outputFilename) throws IOException {
        String template = String.format(
            "#!/bin/bash\n" +
            "#SBATCH -o %s\n" +
            "#SBATCH -A %s\n" +
            "#SBATCH -N %d\n" +
            "#SBATCH -n %d\n" +
            "#SBATCH -t %s\n" +
            "#SBATCH --mem=%d\n" +
            "%s %s > %s",
            stdoutFilename, queueName, numNodes, numProcessors, walltime,
            mem, efpmdPath, inputFilePath, outputFilename
        );

        File submissionScript = new File("submission.slurm");
        FileUtils.writeStringToFile(submissionScript, template, Charset.forName("UTF-8"));
        return submissionScript;
    }
    public String submit(String efpmdPath, String inputFilePath, String outputFilename) {
        String jobID = UUID.randomUUID().toString();
        try {
        File submissionScript = createSubmissionScript(efpmdPath, inputFilePath, outputFilename);
        Connection con = new Connection(hostname);
            boolean isAuthenticated = con.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                System.err.println("Was unable to authenticate user");
                con.close();
                return "Error: User could not be authenticated";
            }
            SCPClient scp = con.createSCPClient();
            String remoteFileName = "submission_" + jobID + ".slurm";
            scp.put(remoteFileName, submissionScript.length(), LocalBundleManager.LIBEFP_INPUTS, "0666");
            Session s = con.openSession();
            s.execCommand("cd " + LocalBundleManager.LIBEFP);
            s.execCommand(String.format("sbatch %s", LocalBundleManager.LIBEFP_INPUTS + remoteFileName));
            return jobID;
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Error: See stack trace";
    }
}

