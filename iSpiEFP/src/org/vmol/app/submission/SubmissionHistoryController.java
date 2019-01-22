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
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
	private boolean firstStart = true;

	private String username;
	private String password;
	
	public SubmissionHistoryController(String username, String password) {
	    this.username = username;
	    this.password = password;
	}
	
	@FXML
	public void initialize()
			throws IOException, SAXException, SQLException, ParseException, URISyntaxException, BackingStoreException {
	    
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
        /*
		// userPrefs.clear();
		if (firstStart == true) {
			firstStart = false;
			tableView.setRowFactory(tv -> {
				TableRow<SubmissionRecord> row = new TableRow<>();
				row.setOnMouseClicked(event -> {
					if (event.getClickCount() == 2 && (!row.isEmpty())) {
						// SubmissionRecord rowData = row.getItem();
						try {
						
							System.out.println(row.getItem().getName());
							SubmissionRecord record = row.getItem();
							if(record.getStatus().equalsIgnoreCase("READY TO OPEN")){
							    System.out.println("opening record");
							    getRemoteVmolOutput(record.getTime());
							}
							//Stage currStage = (Stage) root.getScene().getWindow();
							//new JmolVisualization(currStage, false).show(new File("output_" + date_extension + ".xyz"));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				return row;
			});
		}*/

		ObservableList<SubmissionRecord> data = tableView.getItems();
//		String hostname = "halstead.rcac.purdue.edu";
//		Connection conn = new Connection(hostname);
//		conn.connect();
//		String username = "xu675";
//		String password = "He00719614";
//		boolean isAuthenticated = conn.authenticateWithPassword(username, password);
//		if (!isAuthenticated)
//			throw new IOException("Authentication failed.");
//
//		String[] keys = userPrefs.keys();
//
//		for (int i = 0; i < keys.length; i++) {
//			String value = userPrefs.get(keys[i], null);
//			String[] tokens = value.split("\\n");
//			Session sess = conn.openSession();
//			sess.execCommand("source /etc/profile; qstat " + tokens[0]);
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//			String status = "";
//			while (true) {
//				String line = br.readLine();
//				if (line == null)
//					break;
//				if (Character.isDigit(line.charAt(0)) == true) {
//					String[] status_tokens = line.split("\\s+");
//					status = status_tokens[status_tokens.length - 2];
//				}
//				// System.out.println(line);
//			}
//
//			br.close();
//			sess.close();
//			if (status.toUpperCase().equals("R")) {
//				status = "Running";
//			} else if (status.toUpperCase().equals("Q")) {
//				status = "Queuing";
//			} else if (status.toUpperCase().equals("C")) {
//				status = "Completed..Wrapping Up";
//			} else if (status.isEmpty()) {
//				status = "Ready to open";
//			}
//			SubmissionRecord sr = new SubmissionRecord(tokens[0], status, tokens[1]);
//			data.add(sr);
//
//		}
		/*
		SubmissionRecord sr = new SubmissionRecord("lysine", "Ready to open", "Wed Mar 14 15:18:34 EDT 2018");
		data.add(sr);
		sr = new SubmissionRecord("benzene_na_h2o", "Ready to open", "Wed Mar 14 15:18:34 EDT 2018");
		data.add(sr);
		String[] keys = userPrefs.keys();
		System.out.println(keys.length);
		for (int i = 0; i < keys.length; i++) {
			SubmissionRecord r = new SubmissionRecord(keys[i], "Queuing", userPrefs.get(keys[i], null));
			data.add(r);
		} */
        String hostname = "halstead.rcac.purdue.edu";
	//	LoginForm loginForm = new LoginForm(hostname);
	//	boolean authorized = loginForm.authenticate();
//		if(authorized) {
		    String username = "apolcyn";
		    String type = "LIBEFP";
		
		    ArrayList<String []> jobHistory = queryDatabaseforJobHistory(username, hostname, type);
		    //conn.close();
		
		    jobHistory = checkJobStatus(jobHistory);
		    for (String [] line : jobHistory) {
		        for(String str: line) {
		            System.out.println(str);
		        }
		    }
		    loadData(jobHistory, data);
	//	}
	}
	
	private String getRemoteVmolOutput(String job_date) throws IOException {
	    String hostname = "halstead.rcac.purdue.edu";
        Connection conn = new Connection(hostname);
        conn.connect();
      
        String username = "apolcyn";
        String password = "P15mac&new";
        
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");
        
        SCPClient scp = conn.createSCPClient();
        //System.out.println("current dir:"+System.getProperty("user.dir"));
        
        SCPInputStream scpos = scp.get("./vmol/output_"+job_date);
        
        InputStream stdout = new StreamGobbler(scpos);
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        //ArrayList<String> output = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.out.println("gobbler");
            System.out.println(line);
            sb.append(line+"\n");
            //output.add(line);
            //System.out.println(line);
        }
//           FileInputStream in = new FileInputStream(new File(this.QChemInputsDirectory + "/md_1.in"));
       // FileInputStream in = new FileInputStream(new File(this.QChemInputsDirectory + "/md_1.in"));
       
       // IOUtils.copy(in, scpos);
       // in.close();
        scpos.close();
        conn.close();
        //br.close(); //OVIEN STRANGE RESULTS HERE 
        /*
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");
        
        Session sess = conn.openSession();
        String outputName = "output_"+job_date;
        
        sess.execCommand("cd vmol; ls | grep -w "+outputName);
        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        ArrayList<String> output = new ArrayList<String>();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.out.println("gobbler");
            System.out.println(line);
            output.add(line);
            //System.out.println(line);
        }
        br.close();
        sess.close();
        
*/      return sb.toString();  
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
            SubmissionRecord record = new SubmissionRecord(title, statement, date);
            data.add(record);
        }
    }

    private ArrayList<String []> checkJobStatus(ArrayList<String []> jobHistory) throws IOException {
	    for (String [] line : jobHistory) {
	        String job_id = line[0];
	        String title = line[1];
	        String date = line[2];
	        String status = line[3];
	       
            if(status.equals("QUEUE")) {
                boolean done = askRemoteServerJobStatus(job_id, date);
                if(done) {
                    System.out.println("Job:"+job_id + " done. updating database");
                    updateDBStatus(job_id, title, date);
                    line[3] = "DONE";
                }
            }
        }
	    return jobHistory;
	}
	
	private void updateDBStatus(String job_id, String title, String date) throws UnknownHostException, IOException {
	    String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
        int port = 8080;
        
        //send over job data to database
        String query = "Update_Status";
        query += "$END$";
        query += job_id + "  " + title + "  " + date + "  " + "DONE" + "  " + "LIBEFP";
        query+= "$ENDALL$";
        
        Socket client = new Socket(serverName, port);
        OutputStream outToServer = client.getOutputStream();
        //DataOutputStream out = new DataOutputStream(outToServer);
        
        System.out.println(query);
        outToServer.write(query.getBytes("UTF-8"));
        client.close();
    }

    private boolean askRemoteServerJobStatus(String job_id, String job_date) throws IOException {
	    String hostname = "halstead.rcac.purdue.edu";
        Connection conn = new Connection(hostname);
        conn.connect();
      
        String username = "apolcyn";
        String password = "P15mac&new";
    
        boolean isAuthenticated = conn.authenticateWithPassword(username, password);
        if (!isAuthenticated)
            throw new IOException("Authentication failed.");
        
        Session sess = conn.openSession();
        String outputName = "output_"+job_date;
        
        sess.execCommand("cd vmol; ls | grep -w "+outputName);
        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        ArrayList<String> output = new ArrayList<String>();
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.out.println("gobbler");
            System.out.println(line);
            output.add(line);
            //System.out.println(line);
        }
        br.close();
        sess.close();
        
        if(output.isEmpty()){
            return false; //output file not found, job not finished
        } else {
            return true; //output file found, job finished
        }
	}
	
	private ArrayList<String []> queryDatabaseforJobHistory(String username, String hostname, String type) throws UnknownHostException, IOException {
	    String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
        int port = 8080;
        
        //send over job data to database
        String query = "Check2";
        query += "$END$";
        query += username + "  " + hostname + "  " + type;
        query+= "$ENDALL$";
        
        Socket client;
        client = new Socket(serverName, port);
        OutputStream outToServer = client.getOutputStream();
         //DataOutputStream out = new DataOutputStream(outToServer);
            
        System.out.println(query);
        outToServer.write(query.getBytes("UTF-8"));
            
        InputStream inFromServer = client.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        StringBuilder sb = new StringBuilder();
        int i;
        char c;
        boolean start = false;
        while (( i = in.read())!= -1) {
            c = (char)i;
            sb.append(c);
        }
            
        String reply = sb.toString();
        System.out.println("Database Response:" + reply);
        
        client.close();
	    
        ArrayList<String []> jobHistory = parseDBJobResponse(reply);
        return jobHistory;
	}
	
	private ArrayList<String []> parseDBJobResponse(String reply) {
	    ArrayList<String []> result = new ArrayList<String []>();
	    
        String[] content = reply.split("\\$NEXT\\$"); 
        int n = content.length;
        for(String record : content) {
            String [] fields = new String [n];
            fields = record.split("\\s+");
            if(fields[0].length() != 0)
                result.add(fields);
        }
        return result;
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
            String output = getRemoteVmolOutput(record.getTime());
            
            Stage newStage = new Stage();
            /*VBox comp = new VBox();
            TextField nameField = new TextField("Name");
            TextField phoneNumber = new TextField("Phone Number");
            phoneNumber.setText(output);
            comp.getChildren().add(nameField);
            comp.getChildren().add(phoneNumber);

            Scene stageScene = new Scene(comp, 500, 500);*/
           // newStage.setScene(stageScene);
           // newStage.show();
            
            newStage.setTitle("TextArea Experiment 1");

            TextArea textArea = new TextArea();
            textArea.setText(output);

            VBox vbox = new VBox(textArea);

            Scene scene = new Scene(vbox, 600, 600);
            newStage.setScene(scene);
            newStage.show();
        }
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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
        new JmolVisualization(currStage, false).show(new File("output_" + date_extension + ".xyz"));*/
    }
}
