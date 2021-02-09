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

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.libEFP.LibEFPOutputFile;
import org.ispiefp.app.server.JobManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class JobsMonitor implements Runnable {
    private CopyOnWriteArrayList<JobManager> jobs;
    private ConcurrentHashMap<String, SubmissionRecord> records;
    private int MAX_RECORDS = 15;
    private int numRecords = 0;

    public JobsMonitor(String jobsJson) {
        Gson gson = new Gson();
        jobs = gson.fromJson(jobsJson, JobsMonitor.class).jobs;
        records = gson.fromJson(jobsJson, JobsMonitor.class).records;
        numRecords = records.entrySet().size();
    }

    public JobsMonitor() {
        jobs = new CopyOnWriteArrayList<>();
        records = new ConcurrentHashMap<>();
    }

    public void addJob(JobManager jm) {
        jobs.add(jm);
        System.out.println("Before creating the submission record");
        SubmissionRecord record = new SubmissionRecord(jm);
        System.out.println("After creating the submission record");
        JobHistory jh = new JobHistory();
        System.out.println("After creating the Job History");
        jh.addJob(record);
        System.out.println("After adding the job to jon history");
        records.put(jm.getTitle(), record);
//        record.setJobManager(jm);
        if (numRecords == MAX_RECORDS) {
            //todo Add some method of removing the oldest record.
        } else numRecords++;
        jm.watchJobStatus();
    }


    public void run() {
//        for (JobManager jm : jobs) jm.watchJobStatus();
//        while (true) {
//            System.out.println("JobsMonitor 79: Checking jobs..");
//            ArrayList<JobManager> completedJobs = new ArrayList<>();
//            for (JobManager jm : jobs) {
//                System.out.println("JobsMonitor 82: " + jm.getTitle());
//                try {
//                    if (jm.checkStatus(jm.getJobID())) {
//                        /* Check if the stdout file is empty (success) */
//                        if (checkForError(jm)) {
//                            records.get(jm.getJobID()).setStatus("COMPLETE");
//                        } else records.get(jm.getJobID()).setStatus("ERROR");
//                        retrieveJob(jm);
//                        saveRecord(jm);
//                        completedJobs.add(jm);
//                    }
//                } catch (IOException e) {
//                    System.err.printf("Was unable to monitor job: %s", jm.getJobID());
//                }
//            }
//            jobs.removeAll(completedJobs);
//            try {
//                sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        // loop to monitor job status
        while (true) {
            System.out.println("JobsMonitor 79: Checking jobs..");
            ArrayList<SubmissionRecord> completedJobs = new ArrayList<>();

            // obtain current jobs
            for (SubmissionRecord sr : new JobHistory().getHistory()) {
                System.out.println("JobsMonitor 82: " + sr.getName());
                try {
                    // check status
                    sr.checkStatus();
                    if (sr.getStatus().equals("CD")) {
                        completedJobs.add(sr);
                        saveRecord(sr);
                    }
                } catch (IOException e) {
                    System.err.printf("Was unable to monitor job: %s", sr.getJob_id());
                }
            }
            jobs.removeAll(completedJobs);
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void runOnce() {
//        for (JobManager jm : jobs) jm.watchJobStatus();
//        System.out.println("Rechecking jobs");
//        ArrayList<JobManager> completedJobs = new ArrayList<>();
//        for (JobManager jm : jobs) {
//            try {
//                if (jm.checkStatus(jm.getJobID())) {
//                    /* Check if the stdout file is empty (success) */
//                    if (checkForError(jm)) {
//                        records.get(jm.getJobID()).setStatus("COMPLETE");
//                    } else records.get(jm.getJobID()).setStatus("ERROR");
//                    retrieveJob(jm);
//                    saveRecord(jm);
//                    completedJobs.add(jm);
//                }
//            } catch (IOException e) {
//                System.err.printf("Was unable to monitor job: %s", jm.getJobID());
//            }
//        }
//        jobs.removeAll(completedJobs);
        System.out.println("JobsMonitor 153: Checking jobs..");
        ArrayList<SubmissionRecord> completedJobs = new ArrayList<>();

        // obtain current jobs
        for (SubmissionRecord sr : new JobHistory().getHistory()) {
            System.out.println("JobsMonitor 82: " + sr.getName());
            try {
                // check status
                sr.checkStatus();
                if (sr.getStatus().equals("CD")) {
                    completedJobs.add(sr);
                    saveRecord(sr);
                }
            } catch (IOException e) {
                System.err.printf("Was unable to monitor job: %s", sr.getJob_id());
            }
        }
        jobs.removeAll(completedJobs);
    }

    public void start() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                run();
            }
        };
        timer.schedule(task, 0l, 30000l);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void retrieveJob(SubmissionRecord sr) {
        String outputFileContents = "";
        String errorFileContents = "";
        String outputFilePath = sr.getOutputFilePath();
        String errorFilePath = sr.getStdoutputFilePath();
        String outputFileName = outputFilePath.substring(outputFilePath.lastIndexOf(File.separatorChar) + 1);
        String errorFileName = errorFilePath.substring(errorFilePath.lastIndexOf(File.separatorChar) + 1);
        try {
            outputFileContents = sr.getRemoteFile(sr.getOutputFilePath());
            errorFileContents = sr.getRemoteFile(sr.getStdoutputFilePath());
        } catch (IOException e) {
            System.err.println("Was unable to retrieve the files for the completed job");
        }
        File outputFile = new File(sr.getLocalWorkingDirectory() + File.separator + outputFileName);
        try {
            LibEFPOutputFile out = new LibEFPOutputFile(sr.getLocalWorkingDirectory() + File.separator + outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.writeStringToFile(outputFile, outputFileContents, "UTF-8");
        } catch (IOException e) {
            System.err.println("Was unable to write the output file locally");
        }
        File errorFile = new File(sr.getLocalWorkingDirectory() + File.separator + errorFileName);
        try {
            FileUtils.writeStringToFile(errorFile, errorFileContents, "UTF-8");
        } catch (IOException e) {
            System.err.println("Was unable to write the error file locally");
        }

    }

    public boolean checkForError(JobManager jm) {
        String fileContents = "";
        try {
            fileContents = jm.getRemoteFile(jm.getStdoutputFilename());
        } catch (IOException e) {
            System.err.println("Was unable to retrieve the file for the completed job");
        }
        System.out.println("Attempting to print the retrieved file:");
        System.out.println(fileContents);
        return fileContents.isEmpty();
    }

    public void saveRecord(SubmissionRecord sr) {
        records.get(sr.getJob_id()).setOutputFilePath(sr.getOutputFilePath());
        records.get(sr.getJob_id()).setStdoutputFilePath(sr.getStdoutputFilePath());
    }

    public ConcurrentHashMap<String, SubmissionRecord> getRecords() {
        return records;
    }

    public CopyOnWriteArrayList<JobManager> getJobs() {
        return jobs;
    }

    public void deleteRecord(SubmissionRecord record) {
        records.remove(record.getJob_id());
        JobHistory jh = new JobHistory();
        jh.deleteJob(record);
    }

//    public void connectSubmissionToJobManager(SubmissionRecord record, JobManager jm){
//        record2managerMap.put()
//    }
}