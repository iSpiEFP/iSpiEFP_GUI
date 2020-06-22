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
    private ConcurrentHashMap<SubmissionRecord, JobManager> record2managerMap;
    private int MAX_RECORDS = 15;
    private int numRecords = 0;

    public JobsMonitor(String jobsJson){
        Gson gson = new Gson();
        jobs = gson.fromJson(jobsJson, JobsMonitor.class).jobs;
        records = gson.fromJson(jobsJson, JobsMonitor.class).records;
        numRecords = records.entrySet().size();
        Enumeration<SubmissionRecord> recordsEnumeration = records.elements();
//        while (recordsEnumeration.hasMoreElements()){
//            SubmissionRecord currentRecord = recordsEnumeration.nextElement();
//            if (jobs.contains(currentRecord)){
//                record2managerMap.put(currentRecord, )
//            }
//        }
    }

    public JobsMonitor(){
        jobs = new CopyOnWriteArrayList<>();
        records = new ConcurrentHashMap<>();
        record2managerMap = new ConcurrentHashMap<>();
    }

    public void addJob(JobManager jm){
        jobs.add(jm);
        SubmissionRecord record = new SubmissionRecord(jm.getTitle(), jm.getStatus(), jm.getDate(), jm.getJobID());
        records.put(jm.getJobID(), record);
        if (numRecords == MAX_RECORDS){
            //todo Add some method of removing the oldest record.

        }
        else numRecords++;
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

    public ConcurrentHashMap<String, SubmissionRecord> getRecords() {
        return records;
    }

    public CopyOnWriteArrayList<JobManager> getJobs() {
        return jobs;
    }

    public void deleteRecord(SubmissionRecord record){
        records.remove(record.getJob_id());
//        jobs.remove()
    }

//    public void connectSubmissionToJobManager(SubmissionRecord record, JobManager jm){
//        record2managerMap.put()
//    }
}
