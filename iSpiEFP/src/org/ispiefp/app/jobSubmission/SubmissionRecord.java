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

import ch.ethz.ssh2.Session;
import com.google.gson.Gson;
import org.ispiefp.app.server.JobManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SubmissionRecord {
    /* Mandatory Fields */
    private String name;                /* Alias the user gave the job              */
    private String status;              /* Running, Complete, Error, etc.           */
    private String submissionTime;      /* Time of submission                       */
    private String job_id;              /* ID of the job on the server              */
    private String hostname;            /* Server name to which job was submitted   */
    private String type;                /* Either GAMESS or LIBEFP                  */

    /* Optional Fields */
    private String outputFilePath;
    private String localOutputFilePath;
    private String stdoutputFilePath;
    private String localStdoutputFilePath;
    private ArrayList<String> usedEfpFilepaths;

    private JobManager jobManager;


    public SubmissionRecord(String name, String status, String submissionTime) {
        this.name = name;
        this.status = status;
        this.submissionTime = submissionTime;
    }

    public SubmissionRecord(String name, String status, String submissionTime, String job_id) {
        this.name = name;
        this.status = status;
        this.submissionTime = submissionTime;
        this.job_id = job_id;
    }

    public SubmissionRecord(JobManager jm) {
        this.name = jm.getTitle();
        this.status = jm.getStatus();
        this.submissionTime = jm.getDate();
        this.job_id = jm.getJobID();
        this.hostname = jm.getHostname();
        this.type = jm.getType();
        this.jobManager = jm;
    }

    public SubmissionRecord(String jsonString) {
        Gson gson = new Gson();
        SubmissionRecord copy = gson.fromJson(jsonString, SubmissionRecord.class);
        name = copy.name;
        status = copy.status;
        submissionTime = copy.submissionTime;
        job_id = copy.job_id;
        hostname = copy.hostname;
        type = copy.type;
        outputFilePath = copy.outputFilePath;
        localOutputFilePath = copy.localOutputFilePath;
        localStdoutputFilePath = copy.localStdoutputFilePath;
        usedEfpFilepaths = copy.usedEfpFilepaths;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public String getJob_id() {
        return job_id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getType() {
        return type;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getStdoutputFilePath() {
        return stdoutputFilePath;
    }

    public ArrayList<String> getUsedEfpFilepaths() {
        return usedEfpFilepaths;
    }

    public String getLocalOutputFilePath() { return localOutputFilePath; }

    public String getLocalStdoutputFilePath() {
        return localStdoutputFilePath;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void setJobManager(JobManager jobManager) {
//        this.jobManager = jobManager;
//        /* Characters hard coded because server side is Linux */
//        localOutputFilePath = jobManager.getLocalWorkingDirectory() +
//                jobManager.getOutputFilename().substring(jobManager.getOutputFilename().lastIndexOf('/'));
//        localStdoutputFilePath = jobManager.getLocalWorkingDirectory() +
//                jobManager.getStdoutputFilename().substring(jobManager.getStdoutputFilename().lastIndexOf('/'));
//    }

    public String getSerialization() {
        Gson gson = new Gson();
        return String.format("%s:%n%s%n", hostname + '-' + job_id,
                gson.toJson(this));
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void setStdoutputFilePath(String stdoutputFilePath) {
        this.stdoutputFilePath = stdoutputFilePath;
    }

    public void setUsedEfpFilepaths(ArrayList<String> usedEfpFilepaths) {
        this.usedEfpFilepaths = usedEfpFilepaths;
    }

    public String checkStatus() throws IOException {
        boolean jobIsDone = false;
        org.ispiefp.app.util.Connection conn = new org.ispiefp.app.util.Connection(jobManager.getServer(), jobManager.getKeyPassword());
        conn.connect();

        Session s = conn.openSession();
        s.execCommand(String.format("squeue --job=%s\n", job_id));

        // reading result
        StringBuilder outputString = new StringBuilder();
        int i;
        char c;
        try {
            InputStream output = s.getStdout();
            while ((i = output.read()) != -1) outputString.append((char) i);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // extracting status from output
        String extractStatus = outputString.toString();
        System.out.println("JobManager 171: " + extractStatus);
        try {
            // extract status code based on https://slurm.schedmd.com/squeue.html
            // in JOB STATE CODES section
            // TODO: Use -O
            extractStatus = extractStatus.split("\n")[1].split(" +")[5];
            if (extractStatus.equals("CD")) throw new ArrayIndexOutOfBoundsException();
            System.out.println("JobManager 191: " + extractStatus);
        } catch (ArrayIndexOutOfBoundsException e) {
            // should be done, because squeue doesn't have record
            return "CD";
        }
        s.close();
        conn.close();
        return extractStatus;
    }
}
