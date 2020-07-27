package org.ispiefp.app.libEFP;

import ch.ethz.ssh2.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.CheckComboBox;
import org.ispiefp.app.EFPFileRetriever.GithubRequester;
import org.ispiefp.app.MetaData.MetaData;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.loginPack.LoginForm;
import org.ispiefp.app.metaDataSelector.MetaDataSelectorController;
import org.ispiefp.app.server.*;
import org.ispiefp.app.submission.SubmissionHistoryController;
import org.ispiefp.app.Main;
import org.ispiefp.app.util.ExecutePython;
import org.ispiefp.app.util.UserPreferences;
import org.ispiefp.app.visualizer.ViewerHelper;
import org.jmol.viewer.Viewer;

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

    @FXML
    private ComboBox<String> presets;

    @FXML
    private ComboBox<String> server;

    @FXML private TextField localWorkingDirectory;
    @FXML Button findButton;
    @FXML
    private Button nextButton;

    String coordinates;

    ArrayList jobids;

    private ArrayList<String> efpFilenames;
    private ArrayList<File> efpFiles;
    private ArrayList<MetaData> metaDataList;
    private String workingDirectoryPath;
    private String libEFPInputsDirectory;
    private String efpFileDirectoryPath;

    private Viewer jmolViewer;
    private ArrayList<ArrayList<Integer>> viewerFragments;
    private Map<Integer, Map<MetaData, String>> viewerFragmentMap;
    private Map<String, MetaData> fragmentMap;


    List<ServerDetails> serverDetailsList;
    private ServerInfo selectedServer;

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
        this.efpFileDirectoryPath = LocalBundleManager.LIBEFP_PARAMETERS + File.separator;  //storage for db incoming efp files
        this.libEFPInputsDirectory = LocalBundleManager.LIBEFP_INPUTS + File.separator;         //needed for db file storage
        // initWorkingDir();
    }

    public libEFPInputController(){
        super();
        this.workingDirectoryPath = LocalBundleManager.workingDirectory;
        this.efpFileDirectoryPath = LocalBundleManager.LIBEFP_PARAMETERS + File.separator;  //storage for db incoming efp files
        this.libEFPInputsDirectory = LocalBundleManager.LIBEFP_INPUTS + File.separator;         //needed for db file storage
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
        pol_solver.setItems(FXCollections.observableList(pol_solver_string));
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

        // Initializing libEFPInputTextArea
        try {
            libEFPInputTextArea.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea2.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea3.setText(getlibEFPInputText() + "\n" + coordinates);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        viewerFragmentMap = new HashMap<>();
        fragmentMap = new HashMap<>();


        //Initializing server field
        if (UserPreferences.getServers().keySet().size() < 1){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "You have not configured any servers. Please go to your settings and add a server before proceeding.",
                    ButtonType.OK);
            alert.showAndWait();
        }
        else {
            server.getItems().setAll(UserPreferences.getServers().keySet());
        }
        // Adding listener to presets
        ObservableList<String> available_presets = FXCollections.observableArrayList();
        System.out.println(UserPreferences.getLibEFPPresetNames());
        available_presets.addAll(UserPreferences.getLibEFPPresetNames());
        presets.getItems().addAll(available_presets);
        presets.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null){
                loadPreset(UserPreferences.getLibEFPPresets().get(newValue));
            }
        }));
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
        if (!server.getSelectionModel().isEmpty() ||
                (new File(localWorkingDirectory.getText()).exists() &&
                            new File(localWorkingDirectory.getText()).isDirectory())) nextButton.setDisable(false);
    }

    public void generatelibEFPInputFile() {
        String libEFPText = libEFPInputTextArea.getText();
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Libefp Input (*.in)", "*.in");
        fileChooser.getExtensionFilters().add(extFilter);

        File currentOpenFile = null;

        //if (MainViewController.getJmolVisualization() != null)
        //	currentOpenFile = MainViewController.getJmolVisualization().getCurrentOpenFile();

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

    /**
     * Written by Ryan DeRue
     *
     * This method is called from the controller which initialized this controller. By calling this function,
     * each of the fragments in the viewer have their RMSD computed against fragments whose chemical formula
     * match in the list of available fragments. It then uses this information to populate the libEFP Input text
     * areas. Currently it uses the first available fragment with a sufficiently small RMSD.
     *
     * Confusing variables:
     *   1. viewerFragments ArrayList<ArrayList<Integer>>
     *       An ArrayList<Integer> is the internal representation jmol uses for fragments. This variable is an
     *       ArrayList of all of those representations.
     *   2. viewerFragmentMap Map<Integer, Map<String, String>>
     *       Maps the viewerFragment index of a fragment to a map whose keys are the name of a fragment
     *       that matches on chemical formula and values are the computed RMSD values of this fragment against
     *       the viewerFragment as a String.
     *
     * TODO: Create a list of fragments that matched on chemical formula and their RMSDs and let user pick.
     */
    public void initEfpFiles() throws IOException {
        efpFiles = new ArrayList<>();
        ArrayList<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < viewerFragments.size(); i++){
            if (viewerFragmentMap.get(i).keySet().size() > 0) validIndices.add(i);
        }
        if (validIndices.size() > 0) {
            Stage stage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SelectRMSD.fxml"));
            Parent fragmentSelector = loader.load();

            SelectRMSDController selectRMSDController = loader.getController();
            selectRMSDController.setStage(stage);
            selectRMSDController.setViewerFragments(viewerFragments);
            selectRMSDController.setViewerFragmentMap(viewerFragmentMap);
            selectRMSDController.setMainJmolViewer(jmolViewer);
            selectRMSDController.setValidIndices(validIndices);
            System.out.println(validIndices);
            selectRMSDController.offerNextFragmentSelection();

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Select Fragment");
            stage.setScene(new Scene(fragmentSelector));
            stage.showAndWait();
        }


        for (int i = 0; i < viewerFragments.size(); i++) {
            Map<MetaData, String> fragmentMetas = viewerFragmentMap.get(i);
            if (fragmentMetas.keySet().size() > 0) {
                MetaData md = (MetaData) fragmentMetas.keySet().toArray()[0];
                String metaDataName = md.getFragmentName();
                if (md != null) {
                    md.setEfpFile();
                    System.out.printf("Within the libEFPInputController, the size of the %s is %d%n", md.getEfpFile().getName(), md.getEfpFile().length());
                    efpFiles.add(md.getEfpFile());
                } else System.err.println("Could not find the metadata " + metaDataName);
            }
        }
        coordinates = generateInputText();
        try {
            libEFPInputTextArea.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea2.setText(getlibEFPInputText() + "\n" + coordinates);
            libEFPInputTextArea3.setText(getlibEFPInputText() + "\n" + coordinates);
        } catch (IOException e){
            e.printStackTrace();
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
        libEFPSubmission submission = null; /* Submitter responsible for dealing with server scheduling system */
        String password = null;             /* Password of the user for the server */
        String username = null;             /* Username of the user for the server */
        String jobID = null;                /* JobID for the job the user submits  */

        selectedServer = UserPreferences.getServers().get(server.getSelectionModel().getSelectedItem());

        if (selectedServer.getScheduler().equals("SLURM")) {
            submission = new libEFPSlurmSubmission(selectedServer);
        }
        //TODO: Handle case of PBS and Torque
        else if (selectedServer.getScheduler().equals("PBS")) {
            submission = new libEFPSlurmSubmission(selectedServer);
        }
        username = submission.username;
        password = submission.password;

        FXMLLoader subScriptViewLoader = new FXMLLoader(getClass().getResource("/views/SubmissionScriptTemplateView.fxml"));
        Parent subScriptParent = subScriptViewLoader.load();
        SubmissionScriptTemplateViewController subScriptCont = subScriptViewLoader.getController();
        subScriptCont.setSubmission(submission);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Submission Script Options");
        stage.setScene(new Scene(subScriptParent));
        stage.showAndWait();
        if (!subScriptCont.isSubmitted()) return;
        Connection con = new Connection(selectedServer.getHostname());
        con.connect();
        boolean authorized = con.authenticateWithPassword(username, password);
        if (authorized) {
            createInputFile(submission.inputFilePath, this.libEFPInputsDirectory);
            Thread.sleep(100);
            System.out.println("sending these efp files:");
            for (File file : efpFiles) {
                System.out.println(file.getName());
            }
            SCPClient scp = con.createSCPClient();


            SCPOutputStream scpos = scp.put(submission.inputFilePath, new File(this.libEFPInputsDirectory + submission.inputFilePath).length(), "./iSpiClient/Libefp/input", "0666");
            FileInputStream in = new FileInputStream(new File(this.libEFPInputsDirectory + submission.inputFilePath));


            IOUtils.copy(in, scpos);
            in.close();
            scpos.close();
            System.out.println("sent config file");


            Session sess = con.openSession();
            sess.close();

            for (File file : efpFiles) {
                SCPClient scpClient = con.createSCPClient();
                String filename = file.getName().substring(0, file.getName().indexOf('.') + 4);
                filename = filename.toLowerCase();
                scpos = scpClient.put(filename, file.length(), "./iSpiClient/Libefp/fraglib", "0666");
                System.out.printf("Creating new FIS: %s%s%n", this.efpFileDirectoryPath, filename);
                in = new FileInputStream(file);
                IOUtils.copy(in, scpos);
                in.close();
                scpos.close();
                //Wait for each file to actually be on the server
                while (true) {
                    try {
                        SCPInputStream scpis = null;
                        scpis = scp.get("./iSpiClient/Libefp/fraglib/" + filename);
                        scpis.close();
                    } catch (IOException e) {
                        continue;
                    }
                    break;
                }
            }



            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            Date date = new Date();
            String currentTime = dateFormat.format(date).toString();


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
            con.close();

            String time = currentTime; //equivalent but in different formats
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            submission.submit(subScriptCont.getUsersSubmissionScript());
            currentTime = dateFormat.format(date).toString();
            userPrefs.put(clusterjobID, clusterjobID + "\n" + currentTime + "\n");
            JobManager jobManager = new JobManager(username, password, selectedServer.getHostname(),
                    localWorkingDirectory.getText(), submission.outputFilename, title.getText(),
                    currentTime, "QUEUE", "LIBEFP");
            UserPreferences.getJobsMonitor().addJob(jobManager);
            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Libefp Submission");
            alert.setHeaderText(null);
            alert.setContentText("Job submitted to cluster successfully.");
            alert.showAndWait();
        }
    }

    public void saveCalculationType() {
        Boolean[] terms = new Boolean[4];
        for (int i = 0; i < 4; i++) {
            terms[i] = this.terms.getCheckModel().isChecked(i);
        }
        CalculationPreset savedType = new CalculationPreset(
                title.getText(),
                run_type.getSelectionModel().getSelectedItem(),
                format.getSelectionModel().getSelectedItem(),
                elec_damp.getSelectionModel().getSelectedItem(),
                disp_damp.getSelectionModel().getSelectedItem(),
                terms,
                pol_damp.getSelectionModel().getSelectedItem(),
                pol_solver.getSelectionModel().getSelectedItem()
        );
        UserPreferences.addLibEFPPreset(savedType);
        presets.getItems().add(savedType.getTitle());
        presets.getSelectionModel().select(savedType.getTitle());
    }

    /**
     * Written by Ryan DeRue
     * This method loads every non coordinate field of the libEFP setup with one of the user's saved calculations
     * whose options are saved in the CalculationPreset class which is passed. Is backed by the UserPreferences
     * Java class.
     * @param cp CalculationPreset class containing the user's options
     */
    @FXML
    public void loadPreset(CalculationPreset cp) {
        title.setText(cp.getTitle());
        run_type.setValue(cp.getRunType());
        format.setValue(cp.getFormat());
        elec_damp.setValue(cp.getElecDamp());
        disp_damp.setValue(cp.getDispDamp());
        pol_damp.setValue(cp.getPolDamp());
        pol_solver.setValue(cp.getPolSolver());
        terms.getCheckModel().clearChecks();
        for (int i = 0; i < 4; i++){if (cp.getTerms()[i]) terms.getCheckModel().check(i);}
        try{
            updatelibEFPInputText();
        } catch (IOException e){
            System.err.println("Was unable to write to input file area");
        }
    }

    /**
     * Written by Ryan DeRue
     * Deletes the currently selected preset from the User's machine.
     */
    @FXML
    public void deletePreset() {
        if (presets.getSelectionModel().isEmpty()) return;
        UserPreferences.removeLibEFPPreset(presets.getSelectionModel().getSelectedItem());
        presets.getItems().remove(presets.getSelectionModel().getSelectedItem());
    }

    //converts Addison's frag list to Hanjings Groups
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArrayList<ArrayList> getGroups(List<ArrayList<Integer>> fragment_list) {
        ArrayList<ArrayList> groups = new ArrayList<ArrayList>();
        for (ArrayList<Integer> frag : fragment_list) {
            if (frag.size() > 0) {
                ArrayList curr_group = new ArrayList();
                for (int piece : frag) {
                    curr_group.add(piece);
                }
                Collections.sort(curr_group);
                groups.add(curr_group);
            }
        }
        return groups;
    }

    /**
     * Written by Ryan DeRue
     * This method is responsible for generating the text which populates the box at the bottom of the view which will
     * eventually be used to create the actual input file that will be sent to run the calculation. The getGroups fxn
     * was already written when I started working on this class. I use it as a blackBox to interpret the viewerFragments
     * correctly.
     *
     * Confusing variables:
     *   1. viewerFragments ArrayList<ArrayList<Integer>>
     *       An ArrayList<Integer> is the internal representation jmol uses for fragments. This variable is an
     *       ArrayList of all of those representations.
     *   2. viewerFragmentMap Map<Integer, Map<MetaData, String>>
     *       Maps the viewerFragment index of a fragment to a map whose keys are the metadata of a library fragment
     *       that matches on chemical formula and values are the computed RMSD values of this fragment against
     *       the viewerFragment as a String.
     * @return The String populate the inputTextArea.
     */
    private String generateInputText()  {
        StringBuilder sb = new StringBuilder();
        ArrayList<ArrayList> groups = getGroups(viewerFragments);
        int group_number = 0;
        System.out.printf("ViewerFragmentMap size is %d%n", viewerFragmentMap.size());
        for (int i = 0; i < viewerFragments.size(); i++) {
            //parse filename
            MetaData md = (MetaData) viewerFragmentMap.get(i).keySet().toArray()[0];
            if (group_number == 0 && viewerFragmentMap.get(i).size() > 0) {
                sb.append(String.format("fragment %s%n", md.getFragmentName()));
                System.out.println("Getting name of fragment to be " + viewerFragmentMap.get(i).keySet().toArray()[0]);
            } else {
                sb.append(String.format("%nfragment %s%n", md.getFragmentName()));
            }
            //apend equivalent group coordinates
            ArrayList<Integer> fragment = groups.get(group_number);
            int j = 0;
            for (int atom_num : fragment) {
                if (j == 3) {
                    break;
                }
                org.jmol.modelset.Atom current_atom = jmolViewer.ms.at[atom_num];
                sb.append(current_atom.x + "  " + current_atom.y + "  " + current_atom.z + "\n");
                j++;
            }
            group_number++;
        }
        return sb.toString();
    }

    /**
     * Written by Ryan DeRue
     * Method creates a temporary xyz file for the viewerFragment whose index is passed to this function. This function
     * is primarily used for computing RMSDs against the xyz files of the library fragments. The created file will be
     * deleted when the program exits.
     * @param fragmentIndex The index of the fragment in the viewer
     * @return A java.io file containing the xyz coordinates of a viewerFragment.
     * @throws IOException if the file is not able to be created.
     */
    private File createTempXYZFileFromViewer(int fragmentIndex) throws IOException {
//        System.out.println("\n\nTEMP XYZZZZZZZ CALLLLLLLEDDDDD\n\n");
        BufferedWriter bw = null;
        File xyzFile = null;
        try {
            //Create a temp xyz file
            xyzFile = File.createTempFile("fragment_" + fragmentIndex, ".xyz");
            xyzFile.deleteOnExit();
            bw = new BufferedWriter(new FileWriter(xyzFile));

            ArrayList<Integer> atoms = getGroups(viewerFragments).get(fragmentIndex);
            //Write number of atoms not including dummy atoms in XYZ file
            bw.write(String.format("%d%n%n", atoms.size()));
            System.out.println(atoms.size());
            System.out.println();
            for (int atom_num : atoms) {
                org.jmol.modelset.Atom atom = jmolViewer.ms.at[atom_num];
                bw.write(String.format("%s\t%.5f\t%.5f\t%.5f%n",
                        atom.getAtomName(),
                        ViewerHelper.convertAngstromToBohr(atom.x),
                        ViewerHelper.convertAngstromToBohr(atom.y),
                        ViewerHelper.convertAngstromToBohr(atom.z)
                ));
                System.out.println(String.format("%s\t%.5f\t%.5f\t%.5f",
                        atom.getAtomName(),
                        ViewerHelper.convertAngstromToBohr(atom.x),
                        ViewerHelper.convertAngstromToBohr(atom.y),
                        ViewerHelper.convertAngstromToBohr(atom.z)
                ));
            }
        }
        finally {
            if (bw != null) bw.close();
        }
        return xyzFile;
    }

    /**
     * Written by Ryan DeRue
     * Method computes the RMSD between each of the fragments in the viewer and each of the fragments in the
     * local fragment tree
     * @return a Map containing each of the fragments mapped to their respective RMSD values which had an RMSD below 0.5
     */
    private Map<MetaData, String> computeRMSD(int fragmentIndex){
        Map<MetaData, String> rmsdMap = new HashMap<>(); /* Will be populated with all of the fragment names and      *
         * their respective RMSDs                                    */
        File fragmentXYZFile = null;
        File viewerFragmentXYZFile = null;
        try {
            viewerFragmentXYZFile = createTempXYZFileFromViewer(fragmentIndex);
        } catch (IOException e){
            e.printStackTrace();
            System.err.println("Unable to create temporary xyz file of viewer fragment");
        }
        for (MetaData md : Main.fragmentTree.getMetaDataIterator()) {
            Double RMSD = Double.MAX_VALUE;
            if (md.getChemFormula().equals(getChemicalFormula(fragmentIndex))) {
                System.out.println("Found a match!");
                try {
                    fragmentXYZFile = md.createTempXYZ();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Was unable to create temporary file for computing RMSD");
                }
                String RMSDString = ExecutePython.runPythonScript(
                        "calculate_rmsd.py",
                        String.format("--reorder %s %s", fragmentXYZFile.getAbsolutePath(), viewerFragmentXYZFile.getAbsolutePath())
                );
                if (RMSDString.contains("OUTPUT")) {
                    String [] parsedString = RMSDString.split("null");
                    RMSDString = parsedString[parsedString.length - 1].split("OUTPUT")[1];
                    RMSD = Double.parseDouble(RMSDString);
                }
                if (RMSD < 5) {
                    rmsdMap.put(md, RMSDString);
                    fragmentMap.put(md.getFragmentName(), md);
                }
                System.out.println("RMSDString was " + RMSDString);
                System.out.println("Produced an RMSD of value " + RMSD);
            }
        }
        return rmsdMap;
    }

    public void findDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Working Directory for Job: " + title.getText());
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        Stage currStage = (Stage) root.getScene().getWindow();

        File file = directoryChooser.showDialog(currStage);
        localWorkingDirectory.setText(file.getAbsolutePath());
    }

    // Handle SSH case later

    public Viewer getJmolViewer() {
        return jmolViewer;
    }

    public void setJmolViewer(Viewer v){
        jmolViewer = v;
    }

    public ArrayList<ArrayList<Integer>> getViewerFragments(){
        return viewerFragments;
    }

    public void setViewerFragments(ArrayList<ArrayList<Integer>> frags){
        System.out.println("\nSETVIEWERFRAGS CALLED\n");
        System.out.println("ViewerFrags size: " );
        viewerFragments = frags;
        for (int i = 0; i < viewerFragments.size(); i++) {
            viewerFragmentMap.put(i, computeRMSD(i));
        }
    }

    public String getChemicalFormula(int fragmentIndex){
        ArrayList<Integer> atoms = getGroups(viewerFragments).get(fragmentIndex);
        HashMap<String, Integer> atomTypeMap = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>();
        for (int atom_num : atoms) {
            org.jmol.modelset.Atom atom = jmolViewer.ms.at[atom_num];
            String atomName = atom.getAtomName().replaceAll("[^A-Za-z]", "");
            Integer numThatAtom = atomTypeMap.containsKey(atomName) ? atomTypeMap.get(atomName) + 1 : 1;
            atomTypeMap.put(atomName, numThatAtom);
        }
        Iterator<String> keysItr = atomTypeMap.keySet().iterator();
        while (keysItr.hasNext()){
            StringBuilder sb = new StringBuilder();
            String key = keysItr.next();
            sb.append(key);
            sb.append(atomTypeMap.get(key));
            pq.add(sb.toString());
        }
        String returnString = "";
        while (!pq.isEmpty()){
            returnString += pq.poll();
        }
        return returnString;
    }
}


