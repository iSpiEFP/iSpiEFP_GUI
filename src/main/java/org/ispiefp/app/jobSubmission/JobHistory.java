package org.ispiefp.app.jobSubmission;

import com.google.gson.Gson;
import org.ispiefp.app.installer.LocalBundleManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class JobHistory {
    ArrayList<SubmissionRecord> jobHistory;
    JobHistoryFile masterFile;

    public JobHistory() {
        masterFile = new JobHistoryFile(LocalBundleManager.JOB_HISTORY_FILE);
    }

    public boolean addJob(SubmissionRecord sr) {
        return masterFile.addSubmissionRecord(sr);
    }

    public boolean deleteJob(SubmissionRecord sr) {
        return masterFile.deleteSubmissionRecord(sr);
    }

    public ArrayList<SubmissionRecord> getHistory() {
        return masterFile.getHistory();
    }

    public boolean updateJob(SubmissionRecord sr) {
        return masterFile.updateSubmissionRecord(sr);
    }

    class JobHistoryFile {
        private File masterFile;
        private final String containedJobsDelimiter = "CONTAINS JOBS";
        private final String jobsDelimiter = "BEGIN JOBS";

        public JobHistoryFile(String filePath) {
            masterFile = new File(LocalBundleManager.JOB_HISTORY_FILE);
        }

        boolean addSubmissionRecord(SubmissionRecord sr) {
            File inputFile = masterFile;
            File tempFile = null;
            /* Get the index of insertion into the list of contained jobIDs to keep it alphabetical */
            String srTitle = sr.getHostname() + '-' + sr.getJob_id();
            ArrayList<String> currentlyStoredJobs = getContainedJobs();
            if (currentlyStoredJobs.contains(srTitle)) {
                System.err.println("Entry was already contained in the file. Updating the existing entry instead");
                return updateSubmissionRecord(sr);
            }
            currentlyStoredJobs.add(srTitle);
            Collections.sort(currentlyStoredJobs);
            int alphabeticalIndex = currentlyStoredJobs.indexOf(srTitle);
            try {
                tempFile = File.createTempFile("temp" + LocalBundleManager.JOB_HISTORY_FILE, ".config");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                String currentLine;
                int containedJobsIndex = 0;
                /* Add the line listing in contained jobs */
                while (!(currentLine = reader.readLine().trim()).equals(containedJobsDelimiter)) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                /* Writes the contained jobs delimiter */
                writer.write(currentLine + System.getProperty("line.separator"));
                while (!(currentLine = reader.readLine()).trim().equals(jobsDelimiter)) {
                    /* Code responsible for deleting the line listing in contained jobs*/
                    // trim newline when comparing with lineToRemove
                    String trimmedLine = currentLine.trim();
                    if (containedJobsIndex == alphabeticalIndex) {
                        writer.write(srTitle + System.getProperty("line.separator"));
                    }
                    writer.write(currentLine + System.getProperty("line.separator"));
                    containedJobsIndex++;
                }
                /* Writes the jobs delimiter */
                writer.write(currentLine + System.getProperty("line.separator"));
                int jobsIndex = 0;
                if (alphabeticalIndex == 0) {
                    writer.write(sr.getSerialization());
                }
                /* Add the submission record from the file */
                boolean everyOther = false;
                boolean printed = false;
                while ((currentLine = reader.readLine()) != null) {
                    if (!printed && alphabeticalIndex != 0 && jobsIndex == alphabeticalIndex) {
                        writer.write(sr.getSerialization());
                        printed = true;
                    }
                    if (everyOther) jobsIndex++;
                    writer.write(currentLine + System.getProperty("line.separator"));
                    everyOther = !everyOther;
                }
                if (!printed && alphabeticalIndex == currentlyStoredJobs.size() - 1)
                    writer.write(sr.getSerialization());
                writer.close();
                reader.close();
            } catch (IOException ioe) {
                System.err.println("Was unable to write to the job history file. Job history will not be removed!");
            }
            boolean successful = false;
            try {
                successful = tempFile.renameTo(inputFile);
            } catch (NullPointerException npe) {
                System.err.println("Was unable to create a temporary file");
            }
            return successful;
        }

        boolean updateSubmissionRecord(SubmissionRecord sr) {
            File inputFile = masterFile;
            File tempFile = null;
            /* Get the index of insertion into the list of contained jobIDs to keep it alphabetical */
            String srTitle = sr.getHostname() + '-' + sr.getJob_id();
            if (!getContainedJobs().contains(srTitle)) {
                System.err.println("Attempting to update an entry which does not exist");
                return false;
            }
            try {
                tempFile = File.createTempFile("temp" + LocalBundleManager.JOB_HISTORY_FILE, ".config");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String currentLine;
                boolean found = false;
                String previousLine = null;

                /* Iterate up to the job entry title */
                int containedJobsIndex = 0;
                while (!(currentLine = reader.readLine().trim()).equals(String.format("%s:", srTitle))) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                /* Writes the job entry title */
                writer.write(currentLine + System.getProperty("line.separator"));
                /* Skips old job entry */
                reader.readLine();
                /* Writes new entry */
                writer.write(sr.getSerialization());
                /* Write the rest of the file */
                while ((currentLine = reader.readLine()) != null) {
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                writer.close();
                reader.close();
            } catch (IOException ioe) {
                System.err.println("Was unable to write to the job history file. Job history will not be removed!");
            }
            boolean successful = false;
            try {
                successful = tempFile.renameTo(inputFile);
            } catch (NullPointerException npe) {
                System.err.println("Was unable to create a temporary file");
            }
            return successful;
        }

        boolean deleteSubmissionRecord(SubmissionRecord sr) {
            File inputFile = masterFile;
            File tempFile = null;
            try {
                tempFile = File.createTempFile("temp" + LocalBundleManager.JOB_HISTORY_FILE, ".config");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String srTitle = sr.getHostname() + '-' + sr.getJob_id();
                String currentLine;
                boolean found = false;
                String previousLine = null;

                /* Delete the line listing in contained jobs */
                while (!(currentLine = reader.readLine()).trim().equals(jobsDelimiter)) {
                    /* Code responsible for deleting the line listing in contained jobs*/
                    // trim newline when comparing with lineToRemove
                    String trimmedLine = currentLine.trim();
                    if (trimmedLine.equals(srTitle)) {
                        /* Iterate over the lines until the end of the jobs information, then resume writing */
                        continue;
                    }
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                /* Writes the jobs delimiter */
                writer.write(currentLine + System.getProperty("line.separator"));

                /* Delete the submission record from the file */
                while ((currentLine = reader.readLine()) != null) {
                    String trimmedLine = currentLine.trim();
                    if (trimmedLine.equals(String.format("%s:", srTitle))) {
                        reader.readLine(); //Skip entry
                        continue;
                    }
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
                writer.close();
                reader.close();
            } catch (IOException ioe) {
                System.err.println("Was unable to write to the job history file. Job history will not be removed!");
            }
            boolean successful = tempFile.renameTo(inputFile);
            return successful;
        }

        ArrayList<String> getContainedJobs() {
            ArrayList<String> returnList = new ArrayList<>();
            BufferedReader br = null;
            try {
                String currentLine;
                br = new BufferedReader(new FileReader(masterFile));
                while (!(currentLine = br.readLine().trim()).equals(containedJobsDelimiter)) ;
                while (!(currentLine = br.readLine()).isEmpty()) {
                    returnList.add(currentLine);
                }
            } catch (IOException ioe) {
                System.err.println("Was unable to open the job history file");
            }
            return returnList;
        }

        public ArrayList<SubmissionRecord> getHistory() {
            ArrayList<SubmissionRecord> returnList = new ArrayList<>();
            Gson gson = new Gson();
            BufferedReader br = null;
            try {
                String currentLine;
                br = new BufferedReader(new FileReader(masterFile));
                while (!(currentLine = br.readLine().trim()).equals(jobsDelimiter)) {
                    System.out.println(currentLine);
                }
                while ((currentLine = br.readLine()) != null && !currentLine.isEmpty()) {
                    currentLine = br.readLine();
                    System.out.println(currentLine);
                    returnList.add(gson.fromJson(currentLine, SubmissionRecord.class));
                }
            } catch (IOException ioe) {
                System.err.println("Was unable to open the job history file");
            }
            return returnList;
        }
    }
}
