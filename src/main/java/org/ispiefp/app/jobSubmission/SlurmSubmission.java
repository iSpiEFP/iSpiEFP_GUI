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

import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SlurmSubmission extends Submission {

    public SlurmSubmission(ServerInfo server, String queueName, int numNodes,
                           int numProcessors, String walltime, int mem, String jobName, String submissionType) {
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

    public SlurmSubmission(ServerInfo server, String jobName, String submissionType){
        super(server, jobName.replace(" ", "_"), submissionType);
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

    public String getGAMESSSubmissionScriptText() {
        String efpFilename = FilenameUtils.removeExtension(outputFilename) + ".efp";
        String datFilename = FilenameUtils.removeExtension(outputFilename) + ".dat";
        return String.format(
                "#!/bin/csh\n" +
//                        "#SBATCH --chdir=../input/\n" +
                        "#SBATCH -o ../output/%s\n" +
                        "#SBATCH -A %s\n" +
                        "#SBATCH -N %d\n" +
                        "#SBATCH -n %d\n" +
                        "#SBATCH -t %s\n" +
                        "#SBATCH --mem=%d\n" +
                        "%s %s > ../output/%s\n" +
                        "cp %s ../output/%s\n" +
                        "cp %s ../output/%s\n",
                schedulerOutputName,
                queueName, numNodes, numProcessors, walltime,
                mem, gamessPath,
                inputFilePath,
                outputFilename,
                "$HOME/scr/" + efpFilename, efpFilename,
                "$HOME/scr/" + datFilename, datFilename
        );
    }

    public void prepareJob(String efpmdPath, String inputFilePath, String outputFilename){
        this.inputFilePath = inputFilePath;
    }

    public String submit(String input, String pemKey) {
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

            /* sending the file to server */
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
            String queueCommand = String.format("cd %s && sbatch %s\n", getJobInputDirectory(), remoteFileName);
            s.execCommand(queueCommand);
            StringBuilder outputJobId = new StringBuilder();
            int i;
            char c;
            try {
                InputStream output = s.getStdout();
                while ((i = output.read()) != -1) {
                    c = (char) i;
                    if (Character.isDigit(c)) outputJobId.append(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // check JobId retrieved
//            System.out.println(outputJobId);
//            System.out.println("Executed command: " + queueCommand);
            s.close();
            return outputJobId.toString();
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Error: See stack trace";
    }
}
