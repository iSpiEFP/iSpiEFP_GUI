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

import org.ispiefp.app.server.JobManager;

import java.util.ArrayList;

public class SubmissionRecord {
    /* Mandatory Fields */
    private String name;
    private String status;
    private String time;
    private String job_id;
    private JobManager jobManager;

    /* Optional Fields */
    private String outputFilePath;
    private String localOutputFilePath;
    private String stdoutputFilePath;
    private String localStdoutputFilePath;
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
        return job_id;
    }

    public JobManager getJobManager() {
        return jobManager;
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

    public String getLocalOutputFilePath() { return localOutputFilePath; }

    public String getLocalStdoutputFilePath() { return localStdoutputFilePath; }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
        /* Characters hard coded because server side is Linux */
        localOutputFilePath = jobManager.getLocalWorkingDirectory() +
                jobManager.getOutputFilename().substring(jobManager.getOutputFilename().lastIndexOf('/'));
        localStdoutputFilePath = jobManager.getLocalWorkingDirectory() +
                jobManager.getStdoutputFilename().substring(jobManager.getStdoutputFilename().lastIndexOf('/'));
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
