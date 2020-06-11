package org.ispiefp.app.submission;

import com.google.gson.Gson;
import org.ispiefp.app.server.JobManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class JobsMonitor implements Runnable {
    private CopyOnWriteArrayList<JobManager> jobs;
    private ConcurrentHashMap<String, SubmissionRecord> records;

    public JobsMonitor(String jobsJson){
        Gson gson = new Gson();
        this.jobs = gson.fromJson(jobsJson, JobsMonitor.class).jobs;
        this.records = gson.fromJson(jobsJson, JobsMonitor.class).records;
    }

    public JobsMonitor(){
        this.jobs = new CopyOnWriteArrayList<>();
        this.records = new ConcurrentHashMap<>();
    }

    public void addJob(JobManager jm){
        jobs.add(jm);
        SubmissionRecord record = new SubmissionRecord(jm.getTitle(), jm.getStatus(), jm.getDate(), jm.getJobID());
        records.put(jm.getJobID(), record);
        jm.watchJobStatus();
    }


        public void run() {
            for (JobManager jm : jobs) jm.watchJobStatus();
            while (true) {
                System.out.println("Rechecking jobs");
                ArrayList<JobManager> completedJobs = new ArrayList<>();
                for (JobManager jm : jobs) {
                    try {
                        if (jm.checkStatus(jm.getJobID())) {
                            retrieveJob(jm);
                            saveRecord(jm);
                            completedJobs.add(jm);
                        }
                    } catch (IOException e) {
                        System.err.printf("Was unable to monitor job: %s", jm.getJobID());
                    }
                }
                jobs.removeAll(completedJobs);
                try {
                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    public void start(){
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                run();
            }
        };
        timer.schedule(task, 0l, 30000l);
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public void retrieveJob(JobManager jm){
        String fileContents = "";
        try {
            fileContents = jm.getRemoteFile(jm.getOutputFilename());
        } catch (IOException e){ System.err.println("Was unable to retrieve the file for the completed job"); }
        System.out.println("Attempting to print the retrieved file:");
        System.out.println(fileContents);
    }

    public void saveRecord(JobManager jm){
        records.get(jm.getJobID()).setStatus("COMPLETE");
        records.get(jm.getJobID()).setOutputFilePath(jm.getOutputFilename());
        records.get(jm.getJobID()).setStdoutputFilePath(jm.getStdoutputFilename());
    }
}
