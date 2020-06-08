package org.ispiefp.app.libEFP;

import com.google.gson.Gson;
import org.ispiefp.app.server.JobManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class JobsMonitor implements Runnable {
    private ArrayList<JobManager> jobs;

    public JobsMonitor(JobManager[] jobs){
        this.jobs = new ArrayList<>(Arrays.asList(jobs));
    }

    public JobsMonitor(String jobsJson){
        Gson gson = new Gson();
        this.jobs = gson.fromJson(jobsJson, JobsMonitor.class).jobs;
    }

    public JobsMonitor(){
        this.jobs = new ArrayList<>();
    }

    public void addJob(JobManager jm){
        this.jobs.add(jm);
        jm.watchJobStatus();
    }

    public void run(){
        while (!jobs.isEmpty()) {
            for (JobManager jm : jobs) {
                jm.watchJobStatus();
                try {
                    if (jm.checkStatus(jm.getJobID())) {
                        jobs.remove(jm);
                    }
                } catch (IOException e) {
                    System.err.printf("Was unable to monitor job: %s", jm.getJobID());
                }
            }
            try{ sleep(30000);} catch (InterruptedException e) { e.printStackTrace();}
        }
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

}
