package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.server.ServerInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    File createSubmissionScript() throws IOException {
        String template = String.format(
                "#!/bin/bash\n" +
                        "#SBATCH -o %s\n" +
                        "#SBATCH -A %s\n" +
                        "#SBATCH -N %d\n" +
                        "#SBATCH -n %d\n" +
                        "#SBATCH -t %s\n" +
                        "#SBATCH --mem=%d\n" +
                        "%s %s > %s\n",
                REMOTE_LIBEFP_OUT + schedulerOutputName + ".stdout",
                queueName, numNodes, numProcessors, walltime, mem, efpmdPath,
                REMOTE_LIBEFP_IN + inputFilePath,
                REMOTE_LIBEFP_OUT + outputFilename
        );

        File submissionScript = new File("submission.slurm");
        FileUtils.writeStringToFile(submissionScript, template, Charset.forName("UTF-8"));
        return submissionScript;
    }

    public String getSubmissionScriptText(){
        return String.format(
                "#!/bin/bash\n" +
                        "#SBATCH -o %s\n" +
                        "#SBATCH -A %s\n" +
                        "#SBATCH -N %d\n" +
                        "#SBATCH -n %d\n" +
                        "#SBATCH -t %s\n" +
                        "#SBATCH --mem=%d\n" +
                        "%s %s > %s\n",
                REMOTE_LIBEFP_OUT + schedulerOutputName,
                queueName, numNodes, numProcessors, walltime, mem, efpmdPath,
                REMOTE_LIBEFP_IN + inputFilePath,
                REMOTE_LIBEFP_OUT + outputFilename
        );
    }

    public void prepareJob(String efpmdPath, String inputFilePath, String outputFilename){
        this.efpmdPath = efpmdPath;
        this.inputFilePath = inputFilePath;
        this.outputFilename = outputFilename + ".out";
        schedulerOutputName = outputFilename + ".stdout";
    }

    public String submit() {
        String jobID = UUID.randomUUID().toString();
        try {
            File submissionScript = createSubmissionScript();
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
            String cdCommand = "cd " + REMOTE_LIBEFP_DIR;
            String queueCommand = String.format("sbatch %s", REMOTE_LIBEFP_IN + remoteFileName);
            s.execCommand(String.format("%s; %s", cdCommand, queueCommand));
            return jobID;
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Error: See stack trace";
    }
}
