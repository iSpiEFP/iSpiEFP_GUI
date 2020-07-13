package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.UserPreferences;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

public class libEFPSlurmSubmission extends libEFPSubmission {

    public libEFPSlurmSubmission(ServerInfo server, String queueName, int numNodes,
                                 int numProcessors, String walltime, int mem){
        super(server);
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

    public libEFPSlurmSubmission(ServerInfo server){
        super(server);
    }

    File createSubmissionScript(String input) throws IOException {
        File submissionScript = new File("submission.slurm");
        FileUtils.writeStringToFile(submissionScript, input, Charset.forName("UTF-8"));
        return submissionScript;
    }

    public String getSubmissionScriptText(){
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

    public void prepareJob(String efpmdPath, String inputFilePath, String outputFilename){
        this.inputFilePath = inputFilePath;

    }

    public String submit(String input) {
        String jobID = UUID.randomUUID().toString();
        try {
            File submissionScript = createSubmissionScript(input);
            Connection con = new Connection(hostname);
            con.connect();
            boolean isAuthenticated = con.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                System.err.println("Was unable to authenticate user");
                con.close();
                return "Error: User could not be authenticated";
            }
            SCPClient scp = con.createSCPClient();
            String remoteFileName = "submission_" + jobID + ".slurm";
            SCPOutputStream scpos = scp.put(remoteFileName, submissionScript.length(), REMOTE_LIBEFP_IN, "0666");
            FileInputStream in = new FileInputStream(submissionScript);
            IOUtils.copy(in, scpos);
            in.close();
            scpos.close();
            Session s = con.openSession();
            //todo: Eventually someone will need to remove the hard coded "\n" below and replace it with the line delimiter of the SERVER not the user's computer. Look at uname command.
            String queueCommand = String.format("sbatch %s\n", REMOTE_LIBEFP_IN + remoteFileName);
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
