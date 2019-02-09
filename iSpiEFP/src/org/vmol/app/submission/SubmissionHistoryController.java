package org.vmol.app.submission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.io.IOUtils;
import org.vmol.app.loginPack.LoginForm;
import org.vmol.app.server.JobManager;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;
import org.vmol.app.util.UnrecognizedAtomException;
import org.vmol.app.visualization.JmolVisualization;
import org.xml.sax.SAXException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

//
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
            for(ServerDetails details: savedList){
                System.out.println(details.getAddress());
                System.out.println(details.getServerName());
                System.out.println(savedList);
            }
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        System.out.println("Username:"+this.username);
        System.out.println("password:"+this.password);
        System.out.println("hostname:"+this.hostname);

		ObservableList<SubmissionRecord> data = tableView.getItems();
		
	    JobManager jobManager = new JobManager(this.username, this.password, this.hostname, "LIBEFP");
	    ArrayList<String []> jobHistory = jobManager.queryDatabaseforJobHistory("LIBEFP");
	    jobHistory = jobManager.checkJobStatus(jobHistory);
	    
		loadData(jobHistory, data);
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

	public void visualize342() throws IOException, ParseException, UnrecognizedAtomException {
		String hostname = "halstead.rcac.purdue.edu";
		Connection conn = new Connection(hostname);
		conn.connect();
		//String username = "xu675";
		//String password = "He00719614";
		String username = "apolcyn";
	    String password = "P15mac&new";
		boolean isAuthenticated = conn.authenticateWithPassword(username, password);
		if (!isAuthenticated)
			throw new IOException("Authentication failed.");

		SubmissionRecord sr = tableView.getSelectionModel().getSelectedItem();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date dateValue = sdf.parse(sr.getTime());
		sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		String date_extension = sdf.format(dateValue);

		SCPClient scp = conn.createSCPClient();
		SCPInputStream scpin = scp.get("vmol/output_" + date_extension);
		FileOutputStream out = new FileOutputStream(new File("output_" + date_extension));
		IOUtils.copy(scpin, out);
		out.close();
		scpin.close();
		conn.close();

		toXYZ("output_" + date_extension, date_extension);
		Stage currStage = (Stage) root.getScene().getWindow();
		new JmolVisualization(currStage, false).show(new File("output_" + date_extension + ".xyz"));
	}

	public void refresh()
			throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
		for (int i = 0; i < tableView.getItems().size(); i++) {
			tableView.getItems().clear();
		}
		initialize();
	}

	private int getAtomNumber(String filename) {
		int atom_num = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("    GEOMETRY (ANGSTROMS)")) {
					line = br.readLine();
					line = br.readLine();
					while (line != null && !line.isEmpty()) {
						atom_num++;
						line = br.readLine();
					}
					break;
				}
			}
			br.close();
			return atom_num;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	private void toXYZ(String filename, String date_extension) {

		try {
			int atom_num = getAtomNumber(filename);
			BufferedWriter bw = new BufferedWriter(new FileWriter("output_" + date_extension + ".xyz"));
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("    GEOMETRY (ANGSTROMS)")) {
					bw.append(Integer.toString(atom_num));
					bw.append("\n \n");
					line = br.readLine();
					line = br.readLine();
					while (line != null && !line.isEmpty()) {
						String[] tokens = line.split("\\s+");
						bw.append(tokens[0].charAt(tokens[0].length() - 2));
						bw.append("  " + tokens[1] + "  " + tokens[2] + "  " + tokens[3] + "\n");
						line = br.readLine();
					}
					bw.append("\n");
				}
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void visualize() throws IOException, ParseException, UnrecognizedAtomException {
    
        SubmissionRecord record = tableView.getSelectionModel().getSelectedItem();
        
        if(record.getStatus().equalsIgnoreCase("READY TO OPEN")){
            System.out.println("opening record");
            JobManager jobManager = new JobManager(this.username, this.password, this.hostname);
            String output = jobManager.getRemoteVmolOutput(record.getJob_id(), "LIBEFP"); 
            
            OutputController outputController = new OutputController();
            outputController.initialize(output, "LIBEFP");
        } 
    }
	
}
