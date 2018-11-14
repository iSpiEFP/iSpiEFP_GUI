package org.vmol.app.gamessSubmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
	private TableView<gamessSubmissionRecord> tableView;
	
	@FXML
	public void initialize() throws BackingStoreException, IOException, SAXException, SQLException, ParseException, URISyntaxException {
		ObservableList<gamessSubmissionRecord> data = tableView.getItems();
		//userPrefs.clear();
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
		}
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

}
