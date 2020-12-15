package org.ispiefp.app.jobSubmission;

import com.google.gson.Gson;
import org.ispiefp.app.server.JobManager;

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
        return job_id;
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
}
