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
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.libEFP.LibEFPOutputFile;
import org.ispiefp.app.server.JobManager;
import org.ispiefp.app.util.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
                if (!currentStatus.equals(sr.getStatus())) {
                    jobHistory.updateJob(sr);
                    if (sr.getStatus().equals("COMPLETED") || sr.getStatus().equals("FAILED"))
                        retrieveJob(sr);
                }
            } catch (IOException e) {
                System.err.printf("Was unable to monitor job: %s", sr.getJob_id());
            }
        }
    }


    public void run() {
        // loop to monitor job status
        // should be called by ScheduledExecutorService and use scheduleAtFixedRate
        updateStatus();
    }

    public void runOnce() {
        updateStatus();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void retrieveJob(SubmissionRecord sr) {
        String outputFileContents = "";
        String errorFileContents = "";
        String efpFileContents = "";
        String datFileContents = "";
        String outputFilePath = sr.getOutputFilePath();
        String errorFilePath = sr.getStdoutputFilePath();
        String outputFileName = outputFilePath.substring(outputFilePath.lastIndexOf(File.separatorChar) + 1);
        String errorFileName = errorFilePath.substring(errorFilePath.lastIndexOf(File.separatorChar) + 1);
        System.out.println("JobsMonitor 118: " + outputFileName);
        if (sr.getType().equalsIgnoreCase("GAMESS"))
            sr.moveGamessScratchFiles();
        try {
            outputFileContents = sr.getRemoteFile(sr.getOutputFilePath());
            errorFileContents = sr.getRemoteFile(sr.getStdoutputFilePath());
            if (sr.getType().equalsIgnoreCase("GAMESS")) {
                String fileName = sr.getOutputFilePath();
                fileName = fileName.substring(0, fileName.lastIndexOf('/'));
                fileName += "/" + sr.getName().replace(" ",  "_");
                efpFileContents = sr.getRemoteFile(fileName + ".efp");
                datFileContents = sr.getRemoteFile(fileName + ".dat");
            }
        } catch (IOException e) {
            System.err.println("Was unable to retrieve the files for the completed job");
        }
        File outputFile = new File(sr.getLocalWorkingDirectory() + File.separator + outputFileName);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Creating output file failed.");
            e.printStackTrace();

            // Dialog
            Dialog<ButtonType> fileCreationFailed = new Dialog<>();
            fileCreationFailed.setTitle("Error");

            // Set the button types.
            fileCreationFailed.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            String warningMessage = "Local output File creation failed, please check the server for output file.";
            Text warningText = new Text(warningMessage);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(warningText, 0, 0);
            fileCreationFailed.getDialogPane().setContent(grid);
            Optional<ButtonType> choice = fileCreationFailed.showAndWait();
            return;
        }
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
        if (sr.getType().equalsIgnoreCase("GAMESS")) {
            File efpFile = new File(sr.getLocalWorkingDirectory() + File.separator + outputFileName.substring(0, outputFileName.lastIndexOf('.')) + ".efp");
            try {
                FileUtils.writeStringToFile(efpFile, efpFileContents, "UTF-8");
            } catch (IOException e) {
                System.err.println("Was unable to write the efp file locally");
            }

            File datFile = new File(sr.getLocalWorkingDirectory() + File.separator + outputFileName.substring(0, outputFileName.lastIndexOf('.')) + ".dat");
            try {
                FileUtils.writeStringToFile(datFile, datFileContents, "UTF-8");
            } catch (IOException e) {
                System.err.println("Was unable to write the dat file locally");
            }
        }

    }

    public boolean checkForError(JobManager jm) {
        String fileContents = "";
        try {
            fileContents = jm.getRemoteFile(jm.getErrorOutputFileName());
        } catch (IOException e) {
            System.err.println("Was unable to retrieve the file for the completed job");
        }
        System.out.println("Attempting to print the retrieved file:");
        System.out.println(fileContents);
        return fileContents.isEmpty();
    }
}
