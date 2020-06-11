package org.ispiefp.app.submission;

import java.util.ArrayList;

public class SubmissionRecord {
    /* Mandatory Fields */
    private String name;
    private String status;
    private String time;
    private String job_id;

    /* Optional Fields */
    private String outputFilePath;
    private String stdoutputFilePath;
    private ArrayList<String> usedEfpFilepaths;


    public SubmissionRecord(String name, String status, String time) {
        this.name = name;
        this.status = status;
        this.time = time;
    }

    public SubmissionRecord(String name, String status, String time, String job_id) {
        this.name = name;
        this.status = status;
        this.time = time;
        this.job_id = job_id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getJob_id() {
        return job_id;
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

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTime(String time) {
        this.time = time;
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
