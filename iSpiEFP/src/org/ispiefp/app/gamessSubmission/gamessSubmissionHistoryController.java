package org.ispiefp.app.gamessSubmission;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import org.ispiefp.app.server.JobManager;
import org.ispiefp.app.submission.OutputController;
import org.ispiefp.app.submission.SubmissionRecord;
import org.ispiefp.app.util.UnrecognizedAtomException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Handle submission history for Gamess
 */
public class gamessSubmissionHistoryController {
    private static Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);

    @FXML
    private Parent root;
    @FXML
    private TableView<SubmissionRecord> tableView;

    private String username;
    private String password;
    private String hostname;


    public gamessSubmissionHistoryController(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;

    }

    @FXML
    public void initialize() throws BackingStoreException, IOException, SAXException, SQLException, ParseException, URISyntaxException {
        ObservableList<SubmissionRecord> data = tableView.getItems();

        JobManager jobManager = new JobManager(this.username, this.password, this.hostname, "GAMESS");
        ArrayList<String[]> jobHistory = jobManager.queryDatabaseforJobHistory("GAMESS");
        jobHistory = jobManager.checkJobStatus(jobHistory);


        loadData(jobHistory, data);
    }

    @FXML
    public void clearRecords() throws BackingStoreException {
        userPrefs.clear();
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
    }

    @FXML
    public void refresh() throws BackingStoreException, IOException, SAXException, SQLException, ParseException, URISyntaxException {
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
        initialize();
    }

    private void loadData(ArrayList<String[]> jobHistory, ObservableList<SubmissionRecord> data) {
        for (String[] line : jobHistory) {
            String job_id = line[0];
            String title = line[1];
            String date = line[2];
            String status = line[3];

            String statement = new String();
            if (status.equals("QUEUE")) {
                statement = "Queuing";
            } else {
                statement = "Ready to open";
            }
            SubmissionRecord record = new SubmissionRecord(title, statement, date, job_id);
            data.add(record);
        }
    }

    /**
     * User Selected a history row, load the proper data and run the output controller
     *
     * @throws ParseException
     * @throws UnrecognizedAtomException
     */
    public void visualize() throws ParseException, UnrecognizedAtomException {

        SubmissionRecord record = tableView.getSelectionModel().getSelectedItem();

        if (record.getStatus().equalsIgnoreCase("READY TO OPEN")) {
            System.out.println("opening record");

            String output = "Error.";
            //load efp data
            JobManager jobManager = new JobManager(this.username, this.password, this.hostname);
            try {
                output = jobManager.getRemoteFile("iSpiClient/Gamess/output/gamess_" + record.getJob_id() + ".efp");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            String log = "Error.";
            //load log data
            JobManager jobManager2 = new JobManager(this.username, this.password, this.hostname);
            try {
                log = jobManager2.getRemoteFile("iSpiClient/Gamess/src/gamess_" + record.getJob_id() + ".log");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //load output controller with output and log content for Gamess
            OutputController outputController = new OutputController();
            outputController.initialize(output, log, "GAMESS");
        }
    }

}
