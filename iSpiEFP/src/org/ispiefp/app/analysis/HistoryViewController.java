package org.ispiefp.app.analysis;

import org.ispiefp.app.jobSubmission.JobHistory;
import org.ispiefp.app.jobSubmission.SubmissionRecord;

import java.util.ArrayList;

public class HistoryViewController {
    private ArrayList<SubmissionRecord> prevJobs;

    public HistoryViewController() {
        JobHistory jh = new JobHistory();
        prevJobs = new JobHistory().getHistory();
    }

}
