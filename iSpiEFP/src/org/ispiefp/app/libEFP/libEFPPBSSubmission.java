//package org.ispiefp.app.libEFP;
//
//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.SCPClient;
//import ch.ethz.ssh2.Session;
//import org.apache.commons.io.FileUtils;
//import org.ispiefp.app.installer.LocalBundleManager;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.UUID;
//
///* PBS scripts are executed from the home directory and should therefore use the entire path */
//public class libEFPPBSSubmission extends libEFPSubmission {
//
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
//}
