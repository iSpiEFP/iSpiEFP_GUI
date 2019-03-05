package org.vmol.app.submission;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.vmol.app.server.JobManager;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;
import org.vmol.app.util.UnrecognizedAtomException;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Handle submission History for LIBEFP
 */
public class SubmissionHistoryController {
    @FXML
    private Parent root;
    @FXML
    private TableView<SubmissionRecord> tableView;
    private static Preferences userPrefs = Preferences.userNodeForPackage(SubmissionHistoryController.class);

    private String username;
    private String password;
    private String hostname;


    public SubmissionHistoryController(String username, String password, String hostname) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;

    }

    @FXML
    public void initialize() throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {

        System.out.println("initializing");
        ServerConfigController serverConfig = new ServerConfigController();
        try {
            List<ServerDetails> savedList = serverConfig.getServerDetailsList();
            for (ServerDetails details : savedList) {
                System.out.println(details.getAddress());
                System.out.println(details.getServerName());
                System.out.println(savedList);
            }
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println("Username:" + this.username);
        System.out.println("password:" + this.password);
        System.out.println("hostname:" + this.hostname);

        ObservableList<SubmissionRecord> data = tableView.getItems();

        JobManager jobManager = new JobManager(this.username, this.password, this.hostname, "LIBEFP");
        ArrayList<String[]> jobHistory = jobManager.queryDatabaseforJobHistory("LIBEFP");
        jobHistory = jobManager.checkJobStatus(jobHistory);

        loadData(jobHistory, data);
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

    @FXML
    protected void addRecord() {
        ObservableList<SubmissionRecord> data = tableView.getItems();

    }

    public void clearRecords() throws BackingStoreException {
        userPrefs.clear();
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
    }

    public void refresh()
            throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
        initialize();
    }

    /**
     * User has selected a row, load the efp files and xyz files and send the user the output controller form
     * @throws ParseException
     * @throws UnrecognizedAtomException
     */
    public void visualize() throws ParseException, UnrecognizedAtomException {

        SubmissionRecord record = tableView.getSelectionModel().getSelectedItem();

        if (record.getStatus().equalsIgnoreCase("READY TO OPEN")) {
            System.out.println("opening record");

            //Get output form
            String output = "Error.";
            JobManager jobManager = new JobManager(this.username, this.password, this.hostname);
            try {
                output = jobManager.getRemoteFile("iSpiClient/Libefp/output/output_" + record.getJob_id());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //get error form
            String log = "Error.";
            JobManager jobManager2 = new JobManager(this.username, this.password, this.hostname);
            try {
                log = jobManager2.getRemoteFile("iSpiClient/Libefp/output/error_" + record.getJob_id());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            OutputController outputController = new OutputController();
            outputController.initialize(output, log, "LIBEFP");
        }
    }

}
