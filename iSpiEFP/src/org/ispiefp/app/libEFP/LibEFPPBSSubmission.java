
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

package org.ispiefp.app.libEFP;

import org.ispiefp.app.jobSubmission.Submission;

/* PBS scripts are executed from the home directory and should therefore use the entire path */
public abstract class LibEFPPBSSubmission extends Submission {

//    public libEFPPBSSubmission(){
//        submitString = "/usr/pbs/bin/qsub ${JOB_NAME}.run";
//        queryString = "/usr/pbs/bin/qstat -f ${JOB_ID}";
//        killString = "/usr/pbs/bin/qdel ${JOB_ID}";
//        jobFileListString = "find ${JOB_DIR} -type f";
//        queueInfoString = "/usr/pbs/bin/qstat -fQ";
//    }
//
//    File createSubmissionScript(String efpmdPath, String inputFilePath, String outputFilename) throws IOException {
//        String template = String.format(
//                "#!/bin/bash\n" +
//                        "# FILENAME: %s\n" +
//                        "#PBS -q %s\n" +
//                        "#PBS -l nodes=%d:ppn=%d\n" +
//                        "#PBS -l walltime=%s\n" +
//                        "%s %s > %s",
//                stdoutFilename, queueName, numNodes, numProcessors, walltime,
//                efpmdPath, inputFilePath, outputFilename
//        );
//
//        File submissionScript = new File("submission.pbs");
//        FileUtils.writeStringToFile(submissionScript, template, Charset.forName("UTF-8"));
//        return submissionScript;
//    }
//
//    public String submit(String efpmdPath, String inputFilePath, String outputFilename) {
//        String jobID = UUID.randomUUID().toString();
//        try {
//            File submissionScript = createSubmissionScript(efpmdPath, inputFilePath, outputFilename);
//            Connection con = new Connection(hostname);
//            boolean isAuthenticated = con.authenticateWithPassword(username, password);
//            if (!isAuthenticated) {
//                System.err.println("Was unable to authenticate user");
//                con.close();
//                return "Error: User could not be authenticated";
//            }
//            SCPClient scp = con.createSCPClient();
//            String remoteFileName = "submission_" + jobID + ".slurm";
//            scp.put(remoteFileName, submissionScript.length(), LocalBundleManager.LIBEFP_INPUTS, "0666");
//            Session s = con.openSession();
//            s.execCommand("cd " + LocalBundleManager.LIBEFP);
//            s.execCommand(String.format("sbatch %s", LocalBundleManager.LIBEFP_INPUTS + remoteFileName));
//            return jobID;
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        return "Error: See stack trace";
//    }
}
