package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.Connection;
import org.ispiefp.app.util.UserPreferences;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

public class slurmSubmission extends Submission {

    public slurmSubmission(ServerInfo server, String queueName, int numNodes,
                           int numProcessors, String walltime, int mem, String jobName, String submissionType){
        super(server, jobName, submissionType);
//        submitString = "/usr/pbs/bin/qsub ${JOB_NAME}.run";
//        queryString = "/usr/pbs/bin/qstat -f ${JOB_ID}";
//        killString = "/usr/pbs/bin/qdel ${JOB_ID}";
//        jobFileListString = "find ${JOB_DIR} -type f";
//        queueInfoString = "/usr/pbs/bin/qstat -fQ";
//
        this.queueName = queueName;
        this.numNodes = numNodes;
        this.numProcessors = numProcessors;
        this.walltime = walltime;
        this.mem = mem;
    }

    public slurmSubmission(ServerInfo server, String jobName, String submissionType){
        super(server, jobName, submissionType);
    }

    File createSubmissionScript(String input) throws IOException {
        File submissionScript = new File("submission.slurm");
        FileUtils.writeStringToFile(submissionScript, input, Charset.forName("UTF-8"));
        submissionScript.deleteOnExit();
        return submissionScript;
    }

    public String getLibEFPSubmissionScriptText(){
        return String.format(
                "#!/bin/csh\n" +
                        "#SBATCH -o %s\n" +
                        "#SBATCH -A %s\n" +
                        "#SBATCH -N %d\n" +
                        "#SBATCH -n %d\n" +
                        "#SBATCH -t %s\n" +
                        "#SBATCH --mem=%d\n" +
                        "%s %s > %s\n",
                REMOTE_LIBEFP_OUT + schedulerOutputName,
                queueName, numNodes, numProcessors, walltime,
                mem, efpmdPath,
                REMOTE_LIBEFP_IN  + inputFilePath,
                REMOTE_LIBEFP_OUT + outputFilename
        );
    }

    public String getGAMESSSubmissionScriptText(){
        return String.format(
                        "#!/bin/csh\n" +
                        "#SBATCH -D %s\n" +
                        "#SBATCH -o ../output/%s\n" +
                        "#SBATCH -A %s\n" +
                        "#SBATCH -N %d\n" +
                        "#SBATCH -n %d\n" +
                        "#SBATCH -t %s\n" +
                        "#SBATCH --mem=%d\n" +
                        "%s %s > ../output/%s\n",
                getJobInputDirectory(),
                schedulerOutputName,
                queueName, numNodes, numProcessors, walltime,
                mem, gamessPath,
                inputFilePath,
                outputFilename
        );
    }

    public void prepareJob(String efpmdPath, String inputFilePath, String outputFilename){
        this.inputFilePath = inputFilePath;
    }

    public String submit(String input, String pemKey) {
        String jobID = UUID.randomUUID().toString();
        try {
            /* Write the submission script to the server */
            File submissionScript = createSubmissionScript(input);
            submissionScript.deleteOnExit();
            org.ispiefp.app.util.Connection con = new Connection(server, pemKey);
            boolean isAuthenticated = con.connect();
            if (!isAuthenticated) {
                System.err.println("Was unable to authenticate user");
                con.close();
                return "Error: User could not be authenticated";
            }
            SCPClient scp = con.createSCPClient();
            String remoteFileName = jobName + ".slurm";
            SCPOutputStream scpos = scp.put(remoteFileName, submissionScript.length(), getJobInputDirectory(), "0666");
            FileInputStream in = new FileInputStream(submissionScript);
            IOUtils.copy(in, scpos);
            in.close();
            scpos.close();
            /* Reopen the session and call sbatch on the submission script */
            Session s = con.openSession();
            //todo: Eventually someone will need to remove the hard coded "\n" below and replace it with the line delimiter of the SERVER not the user's computer. Look at uname command.
            String queueCommand = String.format("sbatch %s\n", getJobInputDirectory() + remoteFileName);
            s.execCommand(queueCommand);
            System.out.println("Executed command: " + queueCommand);
            s.close();
            return jobID;
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Error: See stack trace";
    }
}
