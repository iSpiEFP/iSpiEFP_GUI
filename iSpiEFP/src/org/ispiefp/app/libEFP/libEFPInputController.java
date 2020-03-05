package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.CheckComboBox;
import org.ispiefp.app.MainViewController;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.loginPack.LoginForm;
import org.ispiefp.app.server.JobManager;
import org.ispiefp.app.server.ServerConfigController;
import org.ispiefp.app.server.ServerDetails;
import org.ispiefp.app.server.iSpiEFPServer;
import org.ispiefp.app.submission.SubmissionHistoryController;
import org.ispiefp.app.Main;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.io.File;

/**
 * Handle all job submission for libEFP package submission
 */
public class libEFPInputController implements Initializable {

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
    private TextArea libEFPInputTextArea;

    @FXML
    private TextArea libEFPInputTextArea2;

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
    private TextArea libEFPInputTextArea3;

    @FXML
    private ComboBox hess_central;

    @FXML
    private TextField num_step_dist;

    @FXML
    private TextField num_step_angle;


    String coordinates;

    ArrayList jobids;

    private ArrayList<String> efpFilenames;
    private ArrayList<File> efpFiles;
    private String workingDirectoryPath;
    private String libEFPInputsDirectory;
    private String efpFileDirectoryPath;

    List<ServerDetails> serverDetailsList;
    private String hostname;

    public libEFPInputController(String coord) {
        this.coordinates = coord;
        this.jobids = null;
    }

    public libEFPInputController(String coord, ArrayList jobids) {
        this.coordinates = coord;
        this.jobids = jobids;
    }

    //current constructor
    public libEFPInputController(String coord, ArrayList jobids, ArrayList<String> efpFilenames) {
        this.coordinates = coord;
        this.jobids = jobids;
        this.efpFilenames = efpFilenames;
        this.workingDirectoryPath = LocalBundleManager.workingDirectory;
        this.efpFileDirectoryPath = LocalBundleManager.LIBEFP_PARAMETERS + "/";  //storage for db incoming efp files
        this.libEFPInputsDirectory = LocalBundleManager.LIBEFP_INPUTS;         //needed for db file storage
        // initWorkingDir();
    }

    public libEFPInputController(){
        super();
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
                    updatelibEFPInputText();
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
            List<String> auto_sub_string = new ArrayList<String>();
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


        // Initializing libEFPInputTextArea
        try {
            libEFPInputTextArea.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea2.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea3.setText(getlibEFPInputText() + "\n" + coordinates);
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
            serverNames.add(server.getAddress());
        }
        serversList.setItems(FXCollections.observableList(serverNames));
        if (serverNames.size() > 0) {
            serversList.setValue(serverNames.get(0));
            setHostname(serverNames.get(0));
        }
        serversList.setEditable(true);
        serversList.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
                String address = t1;
                System.out.println("Selected:" + address);
                setHostname(address);
            }
        });
    }

    /**
     * Generate libEFP input text from user selections and fragments
     *
     * @return
     * @throws FileNotFoundException
     */
    private String getlibEFPInputText() throws FileNotFoundException {
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
        sb.append("pol_driver " + pol_solver.getValue() + "\n");
        sb.append("fraglib_path ../fraglib/\n");
        sb.append("userlib_path iSpiClient/Libefp/fraglib\n");


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
    public void updatelibEFPInputText() throws FileNotFoundException {
        if (run_type.getValue().equals("md")) {
            root.getTabs().add(1, md_pane);
        } else {

            root.getTabs().remove(md_pane);

        }
        libEFPInputTextArea.setText(getlibEFPInputText() + "\n" + coordinates);
        libEFPInputTextArea2.setText(getlibEFPInputText() + "\n" + coordinates);
        libEFPInputTextArea3.setText(getlibEFPInputText() + "\n" + coordinates);
    }

    // Generate libEFP Input file
    public void generatelibEFPInputFile() {
        String libEFPText = libEFPInputTextArea.getText();
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Libefp Input (*.in)", "*.in");
        fileChooser.getExtensionFilters().add(extFilter);

        File currentOpenFile = null;

        //TODO when you make job submission general for all fragments, fix this to get all and not just the first one
        if (!efpFiles.isEmpty()) {
            currentOpenFile = efpFiles.get(0);
        }
        if (currentOpenFile != null) {
            String fileName = currentOpenFile.getName();
            int dotIndex = fileName.indexOf('.');
            if (dotIndex != -1) fileName = fileName.substring(0, dotIndex);
            fileChooser.setInitialFileName(fileName);
        }

        //Show save file dialog
        Stage currStage = (Stage) root.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currStage);

        if (file != null) {
            saveFile(libEFPText, file);
        }
    }

    public void setEfpFiles(ArrayList<File> efpFiles){
        this.efpFiles = efpFiles;
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

    private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
        // Printing the complete absolute path from where the application was initialized
        System.out.println("Your working Directory is " + System.getProperty("user.dir"));
        String path = System.getProperty("user.dir");
        String executablePath = selectedServer.getWorkingDirectory();
        System.out.println("Executable path is: " + executablePath);
        String inputFileName = path + "/vmolAppJob_" + title.getText();
        String outputFileName = path += "/vmolAppJob_" + title.getText() + "_output";

        // First create the input File at that location with the content in libEFPInputTextArea
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
            } catch (Exception e) {
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
            File file = new File(path + LocalBundleManager.FILE_SEPERATOR + inputFileName);
            output = new BufferedWriter(new FileWriter(file));
            output.write(libEFPInputTextArea.getText());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Handle job submission for the efpmd package
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void handleSubmit() throws IOException, InterruptedException {
        ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
        if (selectedServer.getServerType().equalsIgnoreCase("local"))
            submitJobToLocalServer(selectedServer);
        else {
            String hostname = this.hostname;
            LoginForm loginForm = new LoginForm(hostname, "LIBEFP");
            boolean authorized = loginForm.authenticate();
            if (authorized) {


                createInputFile("md_1.in", this.libEFPInputsDirectory);
                Thread.sleep(100);
                System.out.println("sending these efp files:");
                for (String filename : this.efpFilenames) {
                    System.out.println(filename);
                }

                Connection conn = loginForm.getConnection(authorized);

                String username = loginForm.getUsername();
                String password = loginForm.getPassword();


                SCPClient scp = conn.createSCPClient();


                SCPOutputStream scpos = scp.put("md_1.in", new File(this.libEFPInputsDirectory + "/md_1.in").length(), "./iSpiClient/Libefp/input", "0666");
                FileInputStream in = new FileInputStream(new File(this.libEFPInputsDirectory + "/md_1.in"));


                IOUtils.copy(in, scpos);
                in.close();
                scpos.close();
                System.out.println("sent config file");


                Session sess = conn.openSession();
                sess.close();

                for (String filename : this.efpFilenames) {
                    System.out.println(filename);
                    filename = filename.toLowerCase();
                    //scpos = scp.put(filename,new File(this.efpFileDirectoryPath+filename).length(),"./vmol/fraglib","0666");
                    scpos = scp.put(filename, new File(this.efpFileDirectoryPath + filename).length(), "./iSpiClient/Libefp/fraglib", "0666");
                    in = new FileInputStream(new File(this.efpFileDirectoryPath + filename));
                    IOUtils.copy(in, scpos);
                    in.close();
                    scpos.close();
                }


                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                Date date = new Date();
                String currentTime = dateFormat.format(date).toString();

                String jobID = (new JobManager()).generateJobID().toString();

                String pbs_script = "./iSpiClient/Libefp/src/efpmd iSpiClient/Libefp/input/md_1.in > iSpiClient/Libefp/output/output_" + jobID;

                scpos = scp.put("vmol_" + jobID, pbs_script.length(), "iSpiClient/Libefp/output", "0666");
                InputStream istream = IOUtils.toInputStream(pbs_script, "UTF-8");
                IOUtils.copy(istream, scpos);
                istream.close();
                scpos.close();

                sess = conn.openSession();
                sess.execCommand("source /etc/profile; cd iSpiClient/Libefp/output; qsub -l walltime=00:30:00 -l nodes=1:ppn=1 -e error_" + jobID + " -q standby vmol_" + jobID);

                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                String clusterjobID = "";
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    System.out.println(line);
                    String[] tokens = line.split("\\.");
                    if (tokens[0].matches("\\d+")) {
                        clusterjobID = tokens[0];
                    }
                }
                System.out.println(clusterjobID);
                br.close();
                stdout.close();
                sess.close();
                conn.close();

                String time = currentTime; //equivalent but in different formats
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                currentTime = dateFormat.format(date).toString();

                userPrefs.put(clusterjobID, clusterjobID + "\n" + currentTime + "\n");

                String serverName = Main.iSpiEFP_SERVER;
                int port = Main.iSpiEFP_PORT;

                //send over job data to database
                String query = "Submit";
                query += "$END$";
                query += username + "  " + hostname + "  " + jobID + "  " + title.getText() + "  " + time + "  " + "QUEUE" + "  " + "LIBEFP";
                query += "$ENDALL$";

                //Socket client = new Socket(serverName, port);
                iSpiEFPServer iSpiServer = new iSpiEFPServer();
                Socket client = iSpiServer.connect(serverName, port);
                if (client == null) {
                    return;
                }
                OutputStream outToServer = client.getOutputStream();
                //DataOutputStream out = new DataOutputStream(outToServer);

                System.out.println(query);
                outToServer.write(query.getBytes("UTF-8"));
                client.close();
                outToServer.close();

                JobManager jobManager = new JobManager(username, password, hostname, jobID, title.getText(), time, "QUEUE", "LIBEFP");
                jobManager.watchJobStatus();


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Libefp Submission");
                alert.setHeaderText(null);
                alert.setContentText("Job submitted to cluster successfully.");
                alert.showAndWait();
            }
        }
        // Handle SSH case later
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}


