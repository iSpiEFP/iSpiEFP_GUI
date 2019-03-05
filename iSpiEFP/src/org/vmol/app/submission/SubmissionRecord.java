package org.vmol.app.submission;

public class SubmissionRecord {
    private String name;
    private String status;
    private String time;
    private String job_id;

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
}
