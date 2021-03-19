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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class JobsMonitor implements Runnable {
    private ArrayList<SubmissionRecord> records;
    private JobHistory jobHistory;
    private int MAX_RECORDS = 15;
    private int numRecords = 0;

    public JobsMonitor(String jobsJson) {
        Gson gson = new Gson();
        jobHistory = new JobHistory();
        records = jobHistory.getHistory();
        numRecords = records.size();
    }

    public JobsMonitor() {
        jobHistory = new JobHistory();
        records = new JobHistory().getHistory();
    }

    private void updateStatus() {
        // get new jobs
        records = jobHistory.getHistory();

        // obtain current jobs
        for (SubmissionRecord sr : records) {

            String currentStatus = sr.getStatus();

            // skip if already completed or failed (error)
            if (currentStatus.equals("COMPLETED") || currentStatus.equals("FAILED")) continue;

            try {
                // check/update status
                sr.checkStatus();
                // update job if completed
                if (!currentStatus.equals(sr.getStatus())) jobHistory.updateJob(sr);
            } catch (IOException e) {
                System.err.printf("Was unable to monitor job: %s", sr.getJob_id());
            }
        }
    }


    public void run() {
        // loop to monitor job status
        while (true) {
            updateStatus();
            try {
                sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void runOnce() {
        updateStatus();
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
}
