package org.vmol.app.gamessSubmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.vmol.app.server.JobManager;
import org.vmol.app.submission.OutputController;
import org.vmol.app.submission.SubmissionRecord;
import org.vmol.app.util.UnrecognizedAtomException;
import org.xml.sax.SAXException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;


public class gamessSubmissionHistoryController {
	private static Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
	
	@FXML
	private Parent root;
	@FXML
	private TableView<SubmissionRecord> tableView;
	//private TableView<gamessSubmissionRecord> tableView;
	
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
		//ObservableList<gamessSubmissionRecord> data = tableView.getItems();
		//userPrefs.clear();
	    ObservableList<SubmissionRecord> data = tableView.getItems();


		
		
        JobManager jobManager = new JobManager(this.username, this.password, this.hostname, "GAMESS");
        ArrayList<String []> jobHistory = jobManager.queryDatabaseforJobHistory("GAMESS");
        jobHistory = jobManager.checkJobStatus(jobHistory);
        
        
        loadData(jobHistory, data);
		/*
		String[] keys = userPrefs.keys();

		for (int i = 0; i < keys.length; i++) {
			String[] records = userPrefs.get(keys[i], null).split("\\r?\\n");
			String hostname = records[2];
			String username = records[3];
			String password = records[4];
			Connection conn = new Connection(hostname);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(username, password);
			if (!isAuthenticated)
				throw new IOException("Authentication failed.");
			Session sess = conn.openSession();
			sess.execCommand("source /etc/profile; qstat " + keys[i]);
			
			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
			String status = "";
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (Character.isDigit(line.charAt(0)) == true) {
					String[] status_tokens = line.split("\\s+");
					status = status_tokens[status_tokens.length - 2];
				}
				// System.out.println(line);
			}

			br.close();
			sess.close();
			if (status.toUpperCase().equals("R")) {
				status = "Running";
			} else if (status.toUpperCase().equals("Q")) {
				status = "Queuing";
			} else if (status.toUpperCase().equals("C")) {
				status = "Completed..Wrapping Up";
			} else if (status.isEmpty()) {
				status = "Ready to use";
			}
			
			gamessSubmissionRecord r = new gamessSubmissionRecord(records[1], status, records[0]);
			data.add(r);
			conn.close();
		}*/
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
        for (String [] line : jobHistory) {
            String job_id = line[0];
            String title = line[1];
            String date = line[2];
            String status = line[3];           

            String statement = new String();
            if(status.equals("QUEUE")) {
                statement = "Queuing";
            } else {
                statement = "Ready to open";
            }
            SubmissionRecord record = new SubmissionRecord(title, statement, date, job_id);
            data.add(record);
        }
    }
	
	public void visualize() throws IOException, ParseException, UnrecognizedAtomException {
	    
        SubmissionRecord record = tableView.getSelectionModel().getSelectedItem();
        
        if(record.getStatus().equalsIgnoreCase("READY TO OPEN")){
            System.out.println("opening record");
            JobManager jobManager = new JobManager(this.username, this.password, this.hostname);
            String output = jobManager.getRemoteVmolOutput(record.getJob_id(), "GAMESS"); 
            
            OutputController outputController = new OutputController();
            outputController.initialize(output, "GAMESS");
        } 
    }

}
