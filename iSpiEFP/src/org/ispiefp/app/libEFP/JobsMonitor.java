package org.ispiefp.app.libEFP;

import com.google.gson.Gson;
import org.ispiefp.app.server.JobManager;
import org.ispiefp.app.util.UserPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class JobsMonitor implements Runnable {
    private CopyOnWriteArrayList<JobManager> jobs;

    public JobsMonitor(JobManager[] jobs){
        this.jobs = new CopyOnWriteArrayList<>(Arrays.asList(jobs));
    }

    public JobsMonitor(String jobsJson){
        Gson gson = new Gson();
        this.jobs = gson.fromJson(jobsJson, JobsMonitor.class).jobs;
    }

    public JobsMonitor(){
        this.jobs = new CopyOnWriteArrayList<>();
    }

    public void addJob(JobManager jm){
        this.jobs.add(jm);
        jm.watchJobStatus();
    }

    public void run(){
        for (JobManager jm : jobs) jm.watchJobStatus();
        while (!jobs.isEmpty()) {
            ArrayList<JobManager> completedJobs = new ArrayList<>();
            for (JobManager jm : jobs) {
                try {
                    if (jm.checkStatus(jm.getJobID())) {
                        retrieveJob(jm);
                        completedJobs.add(jm);
                    }
                } catch (IOException e) {
                    System.err.printf("Was unable to monitor job: %s", jm.getJobID());
                }
            }
            jobs.removeAll(completedJobs);
        }
        try{ sleep(30000);} catch (InterruptedException e) { e.printStackTrace();}
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
}
