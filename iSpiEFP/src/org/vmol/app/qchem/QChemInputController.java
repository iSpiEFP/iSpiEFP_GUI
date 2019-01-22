package org.vmol.app.qchem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.io.IOUtils;
import org.vmol.app.Main;
import org.vmol.app.MainViewController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.loginPack.LoginForm;
import org.vmol.app.server.JobManager;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;
import org.vmol.app.submission.SubmissionHistoryController;
import org.vmol.app.util.Atom;
import org.vmol.app.util.UnrecognizedAtomException;
import org.vmol.app.visualization.JmolVisualization;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import org.controlsfx.control.CheckComboBox;

public class QChemInputController implements Initializable{
	
	@FXML
	private TabPane root;
	
    @FXML
    private TextField title;
    private static Preferences userPrefs_libefp = Preferences.userNodeForPackage(SubmissionHistoryController.class);
    @FXML
    private ComboBox<String> calculation;
    private static Preferences userPrefs = Preferences.userNodeForPackage(SubmissionHistoryController.class);
    private Map<String, String> calculationMap = new HashMap<String, String>() {{
    	put("Single Point Energy", "SP");
    	put("Geometry Optimization", "Opt");
    	put("Frequencies", "Freq");
    }};
    @FXML
    private CheckComboBox<String> terms;
    
    @FXML
    private ComboBox<String> run_type;
    
    @FXML
    private ComboBox<String> format;
    
    
    @FXML
    private ComboBox<String> elec_damp;
    
    @FXML
    private ComboBox<String> pol_damp;
    
    @FXML
    private ComboBox<String> disp_damp;
    
    @FXML
    private ComboBox<String> pol_solver;

    @FXML
    private TextArea qChemInputTextArea;
    
    @FXML
    private TextArea qChemInputTextArea2;
    
    @FXML
    private ComboBox<String> serversList;
    
    @FXML
    private ComboBox<String> need_fragment;
    
    @FXML
    private Tab md_pane;
    
    @FXML
    private Tab hess_pane;
    
    @FXML
    private ComboBox<String> ensemble;
    
    @FXML
    private TextField time_step;
    
    @FXML
    private ComboBox<String> auto_sub;
    
    @FXML 
    TextField print_step;
    
    @FXML
    ComboBox<String> velocitize;
    
    @FXML
    TextField temperature;
    
    @FXML
    TextField pressure;
    
    @FXML
    TextField thermostat_tau;
    
    @FXML
    TextField barostat_tau;
    
    @FXML 
    private TextArea qChemInputTextArea3;
    
    @FXML
    private ComboBox hess_central;
    
    @FXML
    private TextField num_step_dist;
    
    @FXML
    private TextField num_step_angle;
    
    
    String coordinates;
    
    ArrayList jobids;
    
    private ArrayList<String> efpFilenames;
    
    private String workingDirectoryPath;
    private String QChemInputsDirectory;
    private String efpFileDirectoryPath;
    
    List<ServerDetails> serverDetailsList;
    
    public QChemInputController(String coord) {
    	this.coordinates = coord;
    	this.jobids = null;
    } 
    
    public QChemInputController(String coord, ArrayList jobids) {
    	this.coordinates = coord;
    	this.jobids = jobids;
    }
    
    //current constructor
    public QChemInputController(String coord, ArrayList jobids, ArrayList<String> efpFilenames) {
        this.coordinates = coord;
        this.jobids = jobids;
        this.efpFilenames = efpFilenames;
        this.workingDirectoryPath = System.getProperty("user.dir");
        this.efpFileDirectoryPath = workingDirectoryPath + "/dbController/efp_files/";  //storage for db incoming efp files
        initWorkingDir();
    }
    
    private void initWorkingDir() {
        this.QChemInputsDirectory = this.workingDirectoryPath + "/QchemInputs";         //needed for db file storage
        new File(this.QChemInputsDirectory).mkdirs();
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Adding listener to title
	    
		
		title.setText("Title");
		
		
		ObservableList<String> terms_adding = FXCollections.observableArrayList();
		terms_adding.add("elec");
		terms_adding.add("pol");
		terms_adding.add("disp");
		terms_adding.add("xr");
		terms.getItems().addAll(terms_adding);
		terms.getCheckModel().checkAll();
		terms.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			public void onChanged(ListChangeListener.Change<? extends String> c) {
				System.out.println(terms.getCheckModel().getCheckedItems());
				try {
					updateQChemInputText();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		// Initializing calculation ComboBox
		List<String> run_type_strings = new ArrayList<String>();
		run_type_strings.add("sp");
		run_type_strings.add("grad");
		run_type_strings.add("hess");
		run_type_strings.add("opt");
		run_type_strings.add("md");
		run_type_strings.add("efield");
		run_type_strings.add("gtest");
		run_type.setItems(FXCollections.observableList(run_type_strings));
		run_type.setValue("sp");
		
		// Initializing theory ComboBox
		List<String> format_strings = new ArrayList<String>();
		format_strings.add("xyzabc");
		format_strings.add("points");
		format_strings.add("rotmat");
		
		format.setItems(FXCollections.observableList(format_strings));
		format.setValue("points");
		
		// Initializing basis ComboBox
		List<String> elec_damp_string = new ArrayList<String>();
		elec_damp_string.add("screen");
		elec_damp_string.add("overlap");
		elec_damp_string.add("off");
		elec_damp.setItems(FXCollections.observableList(elec_damp_string));
		elec_damp.setValue("screen");
		
		List<String> disp_damp_string = new ArrayList<String>();
		disp_damp_string.add("tt");
		disp_damp_string.add("overlap");
		disp_damp_string.add("off");
		disp_damp.setItems(FXCollections.observableList(disp_damp_string));
		disp_damp.setValue("overlap");
		
		List<String> pol_damp_string = new ArrayList<String>();
		pol_damp_string.add("tt");
		pol_damp_string.add("off");
		pol_damp.setItems(FXCollections.observableList(pol_damp_string));
		pol_damp.setValue("tt");
		
		List<String> pol_solver_string = new ArrayList<String>();
		pol_solver_string.add("iterative");
		pol_solver_string.add("direct");
		pol_solver.setItems(FXCollections.observableList(pol_damp_string));
		pol_solver.setValue("iterative");
		
		List<String> ensemble_string = new ArrayList<String>();
		ensemble_string.add("nve");
		ensemble_string.add("nvt");
		ensemble_string.add("npt");
		ensemble.setItems(FXCollections.observableList(ensemble_string));
		ensemble.setValue("nve");
		
		time_step.setText("1.0");
		print_step.setText("1");
		
		List<String> velocitize_string = new ArrayList<String>();
		velocitize_string.add("true");
		velocitize_string.add("false");
		velocitize.setItems(FXCollections.observableList(ensemble_string));
		velocitize.setValue("false");
		
		temperature.setText("300.0");
		pressure.setText("1.0");
		thermostat_tau.setText("1000.0");
		barostat_tau.setText("10000.0");
		
		List<String> hess_string = new ArrayList<String>();
		hess_string.add("true");
		hess_string.add("false");
		hess_central.setItems(FXCollections.observableList(hess_string));
		hess_central.setValue("false");
		
		num_step_dist.setText("0.001");
		
		num_step_angle.setText("0.01");
		
		if (!run_type.getValue().equals("md")) {
			root.getTabs().remove(md_pane);
		}
		
		if (!run_type.getValue().equals("hess")) {
			root.getTabs().remove(hess_pane);
		}
		
		if (jobids != null) {
			List<String>auto_sub_string = new ArrayList<String>();
			auto_sub_string.add("On");
			auto_sub_string.add("Off");
			auto_sub.setItems(FXCollections.observableArrayList(auto_sub_string));
			auto_sub.setValue("On");
		} else {
			auto_sub.setDisable(true);
			
		}
		
		
		
		
//		basis.setItems(FXCollections.observableList(basisTypes));
//		basis.setValue("6-31G(d)");
		
		// TODO : Make both charge and multiplicity fields accept only Numbers 
		// Initializing Charge textField
		
		
		// Initializing Multiplicity textField
//		multiplicity.setText("1");
//		multiplicity.textProperty().addListener((observable, oldValue, newValue) -> {
//			// force the field to be numeric only
//            if (!newValue.matches("^[1-9]\\d*$")) {
//                multiplicity.setText("");
//            }
//		    try {
//				updateQChemInputText();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
		
		// Initializing Format ComboBox

		
		// Initializing qChemInputTextArea
		try {
			qChemInputTextArea.setText(getQChemInputText() + "\n" + coordinates);
			qChemInputTextArea2.setText(getQChemInputText() + "\n" + coordinates);
			qChemInputTextArea3.setText(getQChemInputText() + "\n" + coordinates);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Initializing serversList
		serverDetailsList = new ArrayList<>();
		try {
			serverDetailsList = ServerConfigController.getServerDetailsList();
		} catch (ClassNotFoundException | BackingStoreException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> serverNames = new ArrayList<>();
		for (ServerDetails server : serverDetailsList) {
			serverNames.add(server.getServerName());
		}
		serversList.setItems(FXCollections.observableList(serverNames));
		if (serverNames.size() > 0) serversList.setValue(serverNames.get(0));
	}

	private String getQChemInputText() throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		// Appending the rem part
		sb.append("run_type " + run_type.getValue() + "\n");
		sb.append("coord  " + format.getValue() + "\n");
		sb.append("terms ");
		for (int i = 0; i < terms.getCheckModel().getCheckedItems().size(); i++) {
			sb.append(terms.getCheckModel().getCheckedItems().get(i) + " ");
		}
		sb.append("\n");
		
		sb.append("elec_damp " + elec_damp.getValue() + "\n");
		//sb.append("   BASIS " + basis.getValue() + "\n");
		sb.append("pol_damp " + pol_damp.getValue() + "\n");
		sb.append("disp_damp " + disp_damp.getValue() + "\n");
		sb.append("pol_driver " + pol_solver.getValue() + "\n" );
		sb.append("fraglib_path ./fraglib\n");
		
		if (run_type.getValue().equals("md")) {
			sb.append("ensemble " + ensemble.getValue() + "\n");
			sb.append("time_step " + time_step.getText() + "\n");
			sb.append("print_step" + print_step.getText() + "\n");
			sb.append("velocitize " + velocitize.getValue() + "\n");
			sb.append("temperature " + temperature.getText() + "\n");
			sb.append("pressure " + pressure.getText() + "\n");
			sb.append("thermostat_tau " + thermostat_tau.getText() + "\n");
			sb.append("barostat_tau " + barostat_tau.getText() + "\n");
		}
		
		if (run_type.getValue().equals("hess")) {
			sb.append("hess_central " + hess_central.getValue() + "\n");
			sb.append("num_step_dist " + num_step_dist.getText() + "\n");
			sb.append("num_step_angle " + num_step_angle.getText() + "\n");
		}
		
		return sb.toString();
	}
	
	// This method will be called on update of any of the input fields
	public void updateQChemInputText() throws FileNotFoundException {
		if (run_type.getValue().equals("md")) {
			root.getTabs().add(1,md_pane);
		} else {
			
			root.getTabs().remove(md_pane);
			
		}
		qChemInputTextArea.setText(getQChemInputText() + "\n" + coordinates);
		qChemInputTextArea2.setText(getQChemInputText() + "\n" + coordinates);
		qChemInputTextArea3.setText(getQChemInputText() + "\n" + coordinates);
	}

	// Generate Q-Chem Input file
	public void generateQChemInputFile() {
		String qChemText = qChemInputTextArea.getText();
		FileChooser fileChooser = new FileChooser();
		
		//Set extension filter
        ExtensionFilter extFilter = new ExtensionFilter("Libefp Input (*.in)", "*.in");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File currentOpenFile = null ;
        
        if (MainViewController.getJmolVisualization() != null) 
        	currentOpenFile = MainViewController.getJmolVisualization().getCurrentOpenFile(); 
        
        if (currentOpenFile != null) {
        	String fileName = currentOpenFile.getName();
        	int dotIndex = fileName.indexOf('.');
        	if (dotIndex != -1) fileName = fileName.substring(0, dotIndex);
        	fileChooser.setInitialFileName(fileName);
        }
        
        //Show save file dialog
        Stage currStage = (Stage) root.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currStage);
        
        if(file != null){
            saveFile(qChemText, file);
        }
	}

	private void saveFile(String content, File file) {
        try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			// Handle the exception appropriately!
			e.printStackTrace();
		}
	}
	
	
	public void handleSubmit897534() throws IOException, UnrecognizedAtomException {
		
		String qChemText = qChemInputTextArea.getText();
				
			//if (auto_sub != null && auto_sub.getValue().equals("On")) {
				String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";

				//String serverName = "ec2-18-219-71-66.us-east-2.compute.amazonaws.com";
				System.out.println("running submit");
				int port = 8080;
				try {
					Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
					
					String[] records = userPrefs.get((String) jobids.get(0), null).split("\\r?\\n");
					System.out.println("Records:" + records);
			        Socket client = new Socket(serverName, port);
			        OutputStream outToServer = client.getOutputStream();
			        //DataOutputStream out = new DataOutputStream(outToServer);
			        StringBuilder sb = new StringBuilder();
			        sb.append("Submit" + "$END$");
			        sb.append(records[2] + "$END$");
			        sb.append(records[3] + "$END$");
			        sb.append(records[4] + "$END$");
			        for (int i = 0; i < jobids.size(); i ++) {
			        	sb.append((String) jobids.get(i) + "$END$");
			        }
			        sb.append(MainViewController.getLastOpenedFileName() + "$ENDALL$");
			        
			        outToServer.write(sb.toString().getBytes("UTF-8"));
			       
			        InputStream inFromServer = client.getInputStream();
			        DataInputStream in = new DataInputStream(inFromServer);
			        String reply = "";
			        int i;
			        char c;
			        while (( i = in.read())!= -1) {
			        	c = (char)i;
			        	reply += c;
			        }
			        //System.out.println(reply);
			        
			        Connection conn = new Connection(records[2]);
			        conn.connect();
			        boolean isAuthenticated = conn.authenticateWithPassword(records[3], records[4]);
			        if (!isAuthenticated) {
						throw new IOException("Authentication failed");
					}
			        SCPClient scp = conn.createSCPClient();
			        SCPOutputStream scpos = scp.put( "libefp_job.in",qChemText.length(),"./ispiefp","0666" );
			        InputStream istream = IOUtils.toInputStream(qChemText, "UTF-8");
			        IOUtils.copy(istream, scpos);
			        istream.close();
			        scpos.close();
			        conn.close();
			        
			        
			        //System.out.println("Server says " + in.readUTF());
			        client.close();
			     } catch (IOException e) {
			        e.printStackTrace();
			     }
				
			//}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			Date date = new Date();
			userPrefs_libefp.put(title.getText(), date.toString());
//			Stage currStage = (Stage) root.getScene().getWindow();
//			JmolVisualization jv = new JmolVisualization(currStage, true);
//			MainViewController.getJmolVisualization().close();
//			MainViewController.setJmolVisualization(jv);
//			jv.show(new File(MainViewController.getLastOpenedFile()));
//			ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
//			String hostname = selectedServer.getAddress();
//			String username = selectedServer.getUserName();
//			String password = "";
//			TextInputDialog dialog = new TextInputDialog("Password?");
//			dialog.setTitle("Password?");
//			dialog.setHeaderText(null);
//			dialog.setContentText("Please enter your password: ");
//			
//			Optional<String> result = dialog.showAndWait();
//			if (result.isPresent()) {
//				password = result.get();
//			}
//			Connection conn = new Connection(hostname);
//			conn.connect();
			// Create the custom dialog.
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Login Dialog");
			dialog.setHeaderText("Please type your username and password for the selected host");
			// Set the icon (must be included in the project).
			//dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

			// Set the button types.
			
			    	
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Libefp Submission");
			alert.setHeaderText(null);
			alert.setContentText("Job submitted to cluster successfully.");
			alert.showAndWait();
			        
			
			
			
			
			
		
		
	}
	// Method to handle the submit action to selected server
	public void handleSubmit070() throws IOException, InterruptedException {
		ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
		if (selectedServer.getServerType().equalsIgnoreCase("local"))
			submitJobToLocalServer(selectedServer);
		else {
			
		    createInputFile("md_in", this.QChemInputsDirectory);
		    
		    System.out.println("sending these efp files:");
		    for(String filename : this.efpFilenames) {
		        System.out.println(filename);
		    }
		    
//			toXYZ("md_1.out");
//			Stage currStage = (Stage) root.getScene().getWindow();
//			new JmolVisualization(currStage).show(new File("output.xyz"));
			
			String hostname = "halstead.rcac.purdue.edu";
			Connection conn = new Connection(hostname);
			conn.connect();
			/*String username = "xu675";
			String password = "He00719614";
		*/
			String username = "apolcyn";
			String password = "P15mac&new";
		
			boolean isAuthenticated = conn.authenticateWithPassword(username, password);
			if (!isAuthenticated)
				throw new IOException("Authentication failed.");
			
			SCPClient scp = conn.createSCPClient();
			//System.out.println("current dir:"+System.getProperty("user.dir"));
			SCPOutputStream scpos = scp.put("md_1.in",new File("./md_test/md_1.in").length(),"./vmol","0666");
			FileInputStream in = new FileInputStream(new File("./md_test/md_1.in"));
			IOUtils.copy(in, scpos);
			in.close();
			scpos.close();
			Session sess = conn.openSession();
			sess.execCommand("cd vmol; mkdir fraglib");
			sess.close();
			scpos = scp.put("h2o.efp",new File("./md_test/fraglib/h2o.efp").length(),"./vmol/fraglib","0666");
			in = new FileInputStream(new File("./md_test/fraglib/h2o.efp"));
			IOUtils.copy(in, scpos);
			in.close();
			scpos.close();
			scpos = scp.put("nh3.efp",new File("./md_test/fraglib/nh3.efp").length(),"./vmol/fraglib","0666");
			in = new FileInputStream(new File("./md_test/fraglib/nh3.efp"));
			IOUtils.copy(in, scpos);
			in.close();
			scpos.close();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
			Date date = new Date();
			String currentTime = dateFormat.format(date).toString();
			//String pbs_script = "cd vmol;\nmodule load intel;\n/group/lslipche/apps/libefp/libefp_09012017/libefp/bin/efpmd md_1.in > output_" + currentTime;
			///String pbs_script = "cd vmol;\nmodule load intel;\n./efpmd md_1.in > output_" + currentTime;
			String pbs_script = "cd vmol;\nmodule load intel;\n/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src/efpmd md_1.in > output_" + currentTime;

			scpos = scp.put("vmol_"+ currentTime,pbs_script.length(),"./vmol","0666");
			InputStream istream = IOUtils.toInputStream(pbs_script,"UTF-8");
			IOUtils.copy(istream, scpos);
			istream.close();
			scpos.close();
			
			
			sess = conn.openSession();
			//sess.execCommand("source /etc/profile; cd vmol; /group/lslipche/apps/libefp/libefp_09012017/libefp/bin/efpmd md_1.in > output.efpout");
			//sess.waitUntilDataAvailable(0);
			
			sess.execCommand("source /etc/profile; cd vmol; qsub -l walltime=00:30:00 -l nodes=1:ppn=1 -q standby vmol_" + currentTime);
			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
			String jobID = "";
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				System.out.println(line);
				String[] tokens = line.split("\\.");
				if (tokens[0].matches("\\d+")) {
					jobID = tokens[0];
				}
				//System.out.println(line);
			}
			System.out.println(jobID);
			br.close();
			sess.close();
			
			dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			currentTime = dateFormat.format(date).toString();
			
			
			userPrefs.put(jobID,jobID+"\n"+currentTime+"\n");
			
			String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";

            //String serverName = "ec2-18-219-71-66.us-east-2.compute.amazonaws.com";
            
            int port = 8080;
            try {
                //Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
                
                //String[] records = userPrefs.get((String) jobids.get(0), null).split("\\r?\\n");
                //System.out.println("Records:" + records);
                Socket client = new Socket(serverName, port);
                OutputStream outToServer = client.getOutputStream();
                //DataOutputStream out = new DataOutputStream(outToServer);
                StringBuilder sb = new StringBuilder();
                hostname = "halstead.rcac.purdue.edu";
                username = "apolcyn";
                password = "P15mac&new";
                sb.append("Submit" + "$END$");
                sb.append(hostname + "$END$");
                sb.append(username + "$END$");
                sb.append(password + "$END$");
                
                /*
                for (int i = 0; i < jobids.size(); i ++) {
                    sb.append((String) jobids.get(i) + "$END$");
                }*/
                
                sb.append(jobID + "$END$");
                sb.append(MainViewController.getLastOpenedFileName() + "$ENDALL$");
                
                outToServer.write(sb.toString().getBytes("UTF-8"));
               
                /*InputStream inFromServer = client.getInputStream();
                DataInputStream in = new DataInputStream(inFromServer);
                String reply = "";
                int i;
                char c;
                while (( i = in.read())!= -1) {
                    c = (char)i;
                    reply += c;
                }*/
                //System.out.println(reply);
                client.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
//			SCPInputStream scpin = scp.get("vmol/output.efpout");
//			FileOutputStream out = new FileOutputStream(new File("output.efpout"));
//			IOUtils.copy(scpin, out);
//			out.close();
//			scpin.close();
//			
//			toXYZ("output.efpout");
//			Stage currStage = (Stage) root.getScene().getWindow();
//			new JmolVisualization(currStage).show(new File("output.xyz"));		
			
			
//			sess = conn.openSession();
//			sess.execCommand("ls");
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//			while (true) {
//				String line = br.readLine();
//				if (line == null)
//					break;
//				System.out.println(line);
//			}
//			System.out.println("ExitCode: " + sess.getExitStatus());
//			br.close();
//			sess.close();
			conn.close();
			
		}
		// Handle SSH case later
	}

	private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
		// Printing the complete absolute path from where the application was initialized
		System.out.println("Your working Directory is " + System.getProperty("user.dir"));
	    String path = System.getProperty("user.dir");
	    String executablePath = selectedServer.getWorkingDirectory();
	    System.out.println("Executable path is: " + executablePath);
	    String inputFileName = path + "/vmolAppJob_" + title.getText();
	    String outputFileName = path += "/vmolAppJob_" + title.getText() + "_output";

	    // First create the input File at that location with the content in qchemInputTextArea
	    boolean inputFileCreated = createInputFile(inputFileName, null);
	    if (!inputFileCreated) return; // Can probably return some error here
	    List<String> command = new ArrayList<String>();
	    command.add(executablePath);
	    command.add(inputFileName);
	    ProcessBuilder builder = new ProcessBuilder(command);
	    builder.redirectOutput(new File(outputFileName));

	    final Process process = builder.start();
	    
	    int errorCode = process.waitFor(); // 0 means everything went well!
	    System.out.println("Process execution completed with errorCode : " + String.valueOf(errorCode)); 
	    
	    if (outputFileName.length() != 0) {
	    	System.out.println("Printing output File contents: ");
	    	Scanner s = null;
		    try {
		    	s = new Scanner(new File(outputFileName));
		    	while (s.hasNextLine()) {
		    		System.out.println(s.nextLine());
		    	}
		    } catch (Exception e){
		    	e.printStackTrace(System.out);
		    } finally {
		    	if (s != null) s.close();
		    }
	    }
	}

	// Creates an input file at this location 
	private boolean createInputFile(String inputFileName, String path) {
		BufferedWriter output = null;
        try {
            File file = new File(path + "\\" + inputFileName);
            output = new BufferedWriter(new FileWriter(file));
            output.write(qChemInputTextArea.getText());
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	public void handleSubmit() throws IOException, InterruptedException {
        ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
        if (selectedServer.getServerType().equalsIgnoreCase("local"))
            submitJobToLocalServer(selectedServer);
        else {
            String hostname = "halstead.rcac.purdue.edu";
            LoginForm loginForm = new LoginForm(hostname);
            boolean authorized = loginForm.authenticate();
            if(authorized) {
                createInputFile("md_1.in", this.QChemInputsDirectory);
                Thread.sleep(100);
                System.out.println("sending these efp files:");
                for(String filename : this.efpFilenames) {
                    System.out.println(filename);
                }
                
                Connection conn = new Connection(hostname);
                conn.connect();
              
                String username = loginForm.getUsername();
                String password = loginForm.getPassword();
            
                boolean isAuthenticated = conn.authenticateWithPassword(username, password);
                if (!isAuthenticated)
                    throw new IOException("Authentication failed.");
                
                SCPClient scp = conn.createSCPClient();
                //System.out.println("current dir:"+System.getProperty("user.dir"));
                
                SCPOutputStream scpos = scp.put("md_1.in",new File(this.QChemInputsDirectory + "/md_1.in").length(),"./vmol","0666");
     //           FileInputStream in = new FileInputStream(new File(this.QChemInputsDirectory + "/md_1.in"));
                FileInputStream in = new FileInputStream(new File(this.QChemInputsDirectory + "/md_1.in"));
               
                IOUtils.copy(in, scpos);
                in.close();
                scpos.close();
                System.out.println("sent config file");

                
                Session sess = conn.openSession();
                sess.execCommand("cd vmol; mkdir fraglib");
                sess.close();
                
                for(String filename : this.efpFilenames) {
                    System.out.println(filename);
                    scpos = scp.put(filename,new File(this.efpFileDirectoryPath+filename).length(),"./vmol/fraglib","0666");
                    in = new FileInputStream(new File(this.efpFileDirectoryPath + filename));
                    IOUtils.copy(in, scpos);
                    in.close();
                    scpos.close();
                }
                
               /*
                scpos = scp.put("nh3.efp",new File("./md_test/fraglib/nh3.efp").length(),"./vmol/fraglib","0666");
                in = new FileInputStream(new File("./md_test/fraglib/nh3.efp"));
                IOUtils.copy(in, scpos);
                in.close();
                scpos.close();*/
                
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                Date date = new Date();
                String currentTime = dateFormat.format(date).toString();
                String pbs_script = "cd vmol;\nmodule load intel;\n/depot/lslipche/apps/libefp/libefp_yen_pairwise_july_2018_v5/efpmd/src/efpmd md_1.in > output_" + currentTime;

                scpos = scp.put("vmol_"+ currentTime,pbs_script.length(),"./vmol","0666");
                InputStream istream = IOUtils.toInputStream(pbs_script,"UTF-8");
                IOUtils.copy(istream, scpos);
                istream.close();
                scpos.close();
                
                
                sess = conn.openSession();
                //sess.execCommand("source /etc/profile; cd vmol; /group/lslipche/apps/libefp/libefp_09012017/libefp/bin/efpmd md_1.in > output.efpout");
                //sess.waitUntilDataAvailable(0);
                
                sess.execCommand("source /etc/profile; cd vmol; qsub -l walltime=00:30:00 -l nodes=1:ppn=1 -q standby vmol_" + currentTime);
                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                String jobID = "";
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    System.out.println(line);
                    String[] tokens = line.split("\\.");
                    if (tokens[0].matches("\\d+")) {
                        jobID = tokens[0];
                    }
                    //System.out.println(line);
                }
                System.out.println(jobID);
                br.close();
                sess.close();
                
                String time = currentTime; //equivalent but in different formats
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                currentTime = dateFormat.format(date).toString();
                
                
                userPrefs.put(jobID,jobID+"\n"+currentTime+"\n");
                
                   
                conn.close();
                
                
                String serverName = "ec2-3-16-11-177.us-east-2.compute.amazonaws.com";
                int port = 8080;
                
                //send over job data to database
                String query = "Submit2";
                query += "$END$";
                query += username + "  " + hostname + "  " + jobID + "  " + title.getText() + "  " + time + "  " + "QUEUE" + "  " + "LIBEFP";
                query+= "$ENDALL$";
                
                Socket client = new Socket(serverName, port);
                OutputStream outToServer = client.getOutputStream();
                //DataOutputStream out = new DataOutputStream(outToServer);
                
                System.out.println(query);
                outToServer.write(query.getBytes("UTF-8"));
                
                JobManager jobManager = new JobManager(username, password, hostname, jobID, title.getText(), time, "QUEUE", "LIBEFP");
                jobManager.watchJobStatus();
            }
            
            
        }
        // Handle SSH case later
    }
	
	
	
	

	
	
}
