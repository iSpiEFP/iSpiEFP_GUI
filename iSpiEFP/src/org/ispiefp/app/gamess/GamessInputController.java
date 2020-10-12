package org.ispiefp.app.gamess;

import ch.ethz.ssh2.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javajs.util.P3;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.libEFP.Submission;
import org.ispiefp.app.libEFP.SubmissionScriptTemplateViewController;
import org.ispiefp.app.libEFP.slurmSubmission;
import org.ispiefp.app.server.*;
import org.ispiefp.app.util.Connection;
import org.ispiefp.app.util.UserPreferences;
import org.jmol.modelset.Bond;
import org.apache.commons.io.IOUtils;
import org.ispiefp.app.MainViewController;
import org.ispiefp.app.util.Atom;
import org.jmol.viewer.Viewer;
import org.ispiefp.app.Main;
import org.ispiefp.app.gamessSubmission.gamessSubmissionHistoryController;
import org.ispiefp.app.loginPack.LoginForm;
import org.ispiefp.app.visualizer.JmolMainPanel;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handle all Gamess input and job submission for Gamess
 */
public class GamessInputController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextField title;

    @FXML
    private TextField gBasis;

    @FXML
    private CheckBox customBasis;

    @FXML
    private TextField customBasisPath;

    @FXML
    private Button findCustomBasis;

    @FXML
    private ComboBox<String> server;

    @FXML
    private TextField localWorkingDirectory;

    @FXML
    private Button findButton;

    @FXML
    private TextArea gamessInputTextArea;

    @FXML
    private ComboBox<String> serversList;

    private File xyzFile;

    private String chosenBasisFilepath;

    private HashMap<String, String> atomicCharges;

    public GamessInputController() {
        atomicCharges = new HashMap<>();
        atomicCharges.put("H", "1");
        atomicCharges.put("C", "6");
        atomicCharges.put("N", "7");
        atomicCharges.put("O", "8");
        atomicCharges.put("F", "9");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (UserPreferences.getServers().keySet().size() < 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "You have not configured any servers. Please go to your settings and add a server before proceeding.",
                    ButtonType.OK);
            alert.showAndWait();
        } else {
            server.getItems().setAll(UserPreferences.getServers().keySet());
        }

        gBasis.setText("n31 ngauss=6 ndfunc=1 diffsp=.t.");
        toggleBasis();

        gBasis.textProperty().addListener((observable, oldValue, newValue) -> {
            setgBasis();
        });

        localWorkingDirectory.textProperty().addListener((observable, oldValue, newValue) -> {
            setLocalWorkingDirectory();
        });

        customBasisPath.textProperty().addListener((observable, oldValue, newValue) -> {
            setCustomBasisPath();
        });
    }

    @FXML
    private void updateGamessInputText() {
        StringBuilder inputTextBuilder = new StringBuilder();
        String controlLine = " $contrl units=angs local=boys runtyp=makefp coord=cart icut=11 $end\n";
        String systemLine = " $system timlim=99999 mwords=200 $end\n";
        String selfConsistentFieldLine = " $scf dirscf=.t. soscf=.f. diis=.t. conv=1.0d-06 $end\n";
        String dampingLine = " $damp ifttyp(1)=2,0 iftfix(1)=1,1 thrsh=500.0 $end\n";
        String basisLine = customBasis.isSelected() ? String.format(" $basis extfil=%s\n", customBasisPath.getText()) :
                String.format(" $basis gbasis=%s $end\n", gBasis.getText());
        String makeEFPLine = " $makefp chtr=.f. disp7=.f. $end\n";
        inputTextBuilder.append(controlLine);
        inputTextBuilder.append(systemLine);
        inputTextBuilder.append(selfConsistentFieldLine);
        inputTextBuilder.append(dampingLine);
        inputTextBuilder.append(basisLine);
        inputTextBuilder.append(makeEFPLine);
        inputTextBuilder.append("\n $data\n\n");
        if (xyzFile != null) {
            try {
                String fragmentName = xyzFile.getName().substring(0, xyzFile.getName().indexOf('.') - 4);
                BufferedReader br = new BufferedReader(new FileReader(xyzFile));
                Integer numLines = Integer.parseInt(br.readLine());
                br.readLine(); //Consume commentLine;
                inputTextBuilder.append(String.format("C1\n", fragmentName));
                for (int i = 1; i < numLines; i++) {
                    String[] parsedLine = br.readLine().split("\\s+");
                    inputTextBuilder.append(String.format("%s  %s  %s  %s  %s\n",
                            parsedLine[0], atomicCharges.get(parsedLine[0].replaceAll("[^A-Za-z]", "")), parsedLine[1],
                            parsedLine[2], parsedLine[3]));
                }
            } catch (IOException e) {
                System.err.println("Was unable to open xyz file to create GAMESS template");
            }
        }
        inputTextBuilder.append(" $end\n");
        gamessInputTextArea.setText(inputTextBuilder.toString());
    }

    public void findDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Working Directory for Job: " + title.getText());
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        Stage currStage = (Stage) root.getScene().getWindow();

        File file = directoryChooser.showDialog(currStage);
        localWorkingDirectory.setText(file.getAbsolutePath());
    }

    @ FXML public void findFile(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Select a Working Directory for Job: " + title.getText());
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        Stage currStage = (Stage) root.getScene().getWindow();

        File file = fc.showOpenDialog(currStage);
        chosenBasisFilepath = file.getAbsolutePath();
    }

    public void generateGamessInputFile() {
        String gamessText = gamessInputTextArea.getText();
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Gamess Input (*.inp)", "*.inp");
        fileChooser.getExtensionFilters().add(extFilter);


        //Show save file dialog
        Stage currStage = (Stage) root.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currStage);

        if (file != null) {
            saveFile(gamessText, file);
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

    @FXML
    private void handleSubmit() throws IOException {
        Submission submission = null; /* Submitter responsible for dealing with server scheduling system */
        String password = null;             /* Password of the user for the server */
        String username = null;             /* Username of the user for the server */
        String jobID = null;                /* JobID for the job the user submits  */

        if (server.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("GAMESS Submission");
            alert.setHeaderText("Error");
            alert.setContentText("No server selected.");
            alert.showAndWait();
            return;
        }

        ServerInfo selectedServer = UserPreferences.getServers().get(server.getSelectionModel().getSelectedItem());

        if (selectedServer.getGamessPath() == null || selectedServer.getGamessPath().equals("")) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("GAMESS Submission");
            alert.setHeaderText("Error");
            alert.setContentText("Server selected does not have GAMESS installed.");
            alert.showAndWait();
            return;
        }

        if (selectedServer.getScheduler().equals("SLURM")) {
            submission = new slurmSubmission(selectedServer, title.getText(), "GAMESS");
        }
        //TODO: Handle case of PBS and Torque
        else if (selectedServer.getScheduler().equals("PBS")) {
            submission = new slurmSubmission(selectedServer, title.getText(), "GAMESS");
        }

        Connection con = new Connection(selectedServer, null);
        if (!con.connect()){
            System.err.println("Could not authenticate the user. Exiting submission...");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("GAMESS Submission");
            alert.setHeaderText("Error");
            alert.setContentText("Could not authenticate the user.");
            alert.showAndWait();
            return;
        }
        String keyPassword = con.getKeyPassword();
        /* Create the job workspace */
        if (!submission.createJobWorkspace(title.getText(), keyPassword)){
            return;
        }

        /* Show submission script options */
        FXMLLoader subScriptViewLoader = new FXMLLoader(getClass().getResource("/views/SubmissionScriptTemplateView.fxml"));
        Parent subScriptParent = subScriptViewLoader.load();
        SubmissionScriptTemplateViewController subScriptCont = subScriptViewLoader.getController();
        subScriptCont.setSubmission(submission);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Submission Script Options");
        stage.setScene(new Scene(subScriptParent));
        stage.showAndWait();
        /* Check if user closed the options without hitting submit */
        if (!subScriptCont.isSubmitted()) return;

        /* Send input file */
        File inputFile = createInputFile(submission.getInputFilePath());
        if (!submission.sendInputFile(inputFile, keyPassword)) {
            System.err.println("Was unable to send the input file to the server");
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        String currentTime = dateFormat.format(date.getTime());

        System.out.printf("SUBMISSION TIME: %s\n\n\n\n\n", currentTime);

        String time = currentTime; //equivalent but in different formats
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        submission.submit(subScriptCont.getUsersSubmissionScript(), keyPassword);
        currentTime = dateFormat.format(date.getTime());
        JobManager jobManager = new JobManager(selectedServer, localWorkingDirectory.getText(),
                submission.getOutputFilename(), title.getText(),
                currentTime, "QUEUE", "LIBEFP", keyPassword);
        UserPreferences.getJobsMonitor().addJob(jobManager);
        Stage currentStage = (Stage) root.getScene().getWindow();
        currentStage.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GAMESS Submission");
        alert.setHeaderText(null);
        alert.setContentText("Job submitted to cluster successfully.");
        alert.showAndWait();
    }

    private File createInputFile(String inputFileName) {
        BufferedWriter output = null;
        File file;
        try {
            file = new File(inputFileName);
            file.deleteOnExit();
            output = new BufferedWriter(new FileWriter(file));
            output.write(gamessInputTextArea.getText());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public void setXyzFile(File xyzFile) {
        this.xyzFile = xyzFile;
        updateGamessInputText();
    }

    @FXML
    private void toggleBasis() {
        if (customBasis.isSelected()) {
            gBasis.setDisable(true);
            customBasisPath.setDisable(false);
            findButton.setDisable(false);
        } else {
            customBasisPath.setDisable(true);
            findButton.setDisable(true);
            gBasis.setDisable(false);
        }
    }

    public void setCustomBasisPath() {
        updateGamessInputText();
    }

    public void setgBasis() {
        updateGamessInputText();
    }

    public void setTitle() {
        updateGamessInputText();
    }

    public void setLocalWorkingDirectory() {
        updateGamessInputText();
    }

    //    private ArrayList<ArrayList<Atom>> connections;
//    private ArrayList<Atom> atoms;
//    private ArrayList<ArrayList> final_lists = new ArrayList<ArrayList>();
//    // TODO: change this to use hashmap
//    String[] bonds = {"HH", "CC", "NN", "OO", "FF", "CLCL", "BRBR", "II", "CN", "NC", "CO", "OC", "CS", "SC", "CF",
//            "FC", "CCL", "CLC", "CBR", "BRC", "CI", "IC", "HC", "CH", "HN", "NH", "HO", "OH", "HF", "FH", "HCL", "CLH",
//            "HBR", "BRH", "HI", "IH"};
//    double[] lengths = {0.74, 1.54, 0.45, 1.48, 1.42, 1.99, 2.28, 2.67, 1.47, 1.47, 1.43, 1.43, 1.82, 1.82, 1.35, 1.35,
//            1.77, 1.77, 1.94, 1.94, 2.14, 2.14, 1.09, 1.09, 1.01, 1.01, 0.96, 0.96, 0.92, 0.92, 1.27, 1.27, 1.41, 1.41,
//            1.61, 1.61};
//    List<ServerDetails> serverDetailsList;
//    Map<String, Double> charges;
//
//    private ArrayList inputs;
//    private ArrayList<Integer> fragmentNumbers;
//    private Viewer viewer;
//    private JmolMainPanel jmolMainPanel;
//
//    public GamessInputController(File file, ArrayList<ArrayList> groups, ArrayList to_be_submitted, JmolMainPanel jmolMainPanel) {
//        inputs = new ArrayList();
//        fragmentNumbers = to_be_submitted;
//
//        this.viewer = jmolMainPanel.viewer;
//        this.jmolMainPanel = jmolMainPanel;
//
//        // get atoms from to be submitted atoms list
//        for (int i = 0; i < to_be_submitted.size(); i++) {
//            ArrayList<Atom> curr_group = new ArrayList<Atom>();
//
//            for (int j = 0; j < groups.get((Integer) to_be_submitted.get(i)).size(); j++) {
//                int atomNum = (int) groups.get((Integer) to_be_submitted.get(i)).get(j);
//                org.jmol.modelset.Atom atom = viewer.ms.at[atomNum];
//                Atom liteAtom = new Atom(atom.getElementSymbol(), atomNum, atom.getElementNumber(), atom.x, atom.y, atom.z);
//                curr_group.add(liteAtom);
//            }
//            final_lists.add(curr_group);
//        }
//
//        // find missing bonds and add hydrogens to them
//        for (int i = 0; i < to_be_submitted.size(); i++) {
//            ArrayList<Atom> new_hydrogens = addHydrogens(groups.get((Integer) to_be_submitted.get(i)));
//            for (int j = 0; j < new_hydrogens.size(); j++) {
//
//                final_lists.get(i).add(new_hydrogens.get(j));
//
//            }
//        }
//        generateGamessInputFiles();
//    }
//
//    /**
//     * Find the missing atom where there used to be one from a bond
//     *
//     * @param fragAtomIndex fragmented atom index
//     * @param frag          list of frags
//     * @param originalBonds the original bonds before slicing
//     * @return
//     */
//    private ArrayList<Integer> missingAtom(int fragAtomIndex, ArrayList<Integer> frag,
//                                           ArrayList<ArrayList<Integer>> originalBonds) {
//        ArrayList<Integer> missingAtoms = new ArrayList<Integer>();
//        for (Integer atomIndex : originalBonds.get(fragAtomIndex)) {
//            boolean found = false;
//            // search frag for atomIndex, if not there then it has been broken
//            for (Integer fragAtom : frag) {
//                if (fragAtom.equals(atomIndex)) {
//                    found = true;
//                }
//            }
//            if (!found) {
//                // bond has been cut here
//                System.out.println("bond missing between atom:" + (fragAtomIndex + 1) + " and atom:" + (atomIndex + 1));
//                missingAtoms.add(atomIndex);
//            }
//        }
//        return missingAtoms;
//    }
//
//    private static int searchArray(String[] a, String to_be_matched) {
//        for (int i = 0; i < a.length; i++) {
//            if (a[i].equals(to_be_matched)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    public ArrayList<ArrayList> get_fragments_with_h() {
//        return final_lists;
//    }
//
//    private ArrayList<ArrayList<Integer>> buildOriginalBondMap(Viewer viewer) {
//	int atomCount = viewer.ms.at.length;
//    	ArrayList<ArrayList<Integer>> bondMap = new ArrayList<ArrayList<Integer>>();
//    	Bond[] bonds = viewer.ms.bo;
//
//    	//init bondMap
//    	for (int i = 0; i < atomCount; i++) {
//        	bondMap.add(new ArrayList<Integer>());
//    	}
//
//    	for (int i = 0; i < bonds.length; i++) {
//        	int atomIndex1 = bonds[i].getAtomIndex1();
//        	int atomIndex2 = bonds[i].getAtomIndex2();
//
//        	//update lists
//        	bondMap.get(atomIndex1).add(atomIndex2);
//        	bondMap.get(atomIndex2).add(atomIndex1);
//   	}
//    	return bondMap;
//    }
//
//
//
//
//
//    /**
//     * Add dummy hydrogens where bonds were sliced
//     *
//     * @param frag list of fragments
//     * @return
//     */
//
//    public ArrayList<Atom> addHydrogens(ArrayList frag) {
//        ArrayList<ArrayList<Integer>> originalBonds = buildOriginalBondMap(viewer);
//	//ArrayList<ArrayList<Integer>> originalBonds = JmolVisualizer.bondMap;
//        //Viewer viewer = Main.jmolPanel.viewer;
//
//        ArrayList<Atom> hydrogens = new ArrayList<Atom>();
//        for (int i = 0; i < frag.size(); i++) {
//            int atomIndex = (int) frag.get(i);
//            org.jmol.modelset.Atom atom1 = viewer.ms.at[atomIndex];
//
//
//            ArrayList<Integer> cutOffAtoms = missingAtom(atomIndex, frag, originalBonds);
//
//            for (Integer atomNum : cutOffAtoms) {
//                org.jmol.modelset.Atom atom2 = viewer.ms.at[atomNum];
//
//                String a_type = atom1.getAtomName();
//                String b_type = atom2.getAtomName();
//
//                javajs.util.Lst <org.jmol.modelset.Atom> atom_list = new javajs.util.Lst <>();
//                atom_list.add(0, atom1);
//                atom_list.add(0, atom2);
//
//
//                int index = searchArray(bonds, a_type + b_type);
//                double desired_length = lengths[index];
//                double x1 = atom1.x;
//                double y1 = atom1.y;
//                double z1 = atom1.z;
//                double x2 = atom2.x;
//                double y2 = atom2.y;
//                double z2 = atom2.z;
//
//
//                javajs.util.P3 [] points = new javajs.util.P3 [2];
//                javajs.util.P3 point1 = new javajs.util.P3();
//                float x1_new =(float)x1;
//                float y1_new = (float)y1;
//                float z1_new = (float)z1;
//
//                point1.x=x1_new;
//                point1.y=y1_new;
//                point1.z=z1_new;
//
//                javajs.util.P3 point2 = new javajs.util.P3();
//                float x2_new =(float)x2;
//                float y2_new = (float)y2;
//                float z2_new = (float)z2;
//
//                point2.x=x2_new;
//                point2.y=y2_new;
//                point2.z=z2_new;
//
//                int[] nTotal = {2};
//
//                P3[][] result = viewer.ms.calculateHydrogens(null,nTotal,false,false,atom_list);
//
//
//                ArrayList<Float> dimension = new ArrayList<>();
//
//                dimension.add(result[0][0].x);
//                dimension.add(result[0][0].y);
//                dimension.add(result[0][0].z);
//
//                hydrogens.add(new Atom("H000", -1, 1, dimension.get(0), dimension.get(1), dimension.get(2)));
//            }
//        }
//        return hydrogens;
//    }
//
//    @Override
//    /**
//     * Initialize thee Gamess form for javaFX
//     */
//    public void initialize(URL location, ResourceBundle resources) {
//
//        try {
//
//            for (int i = 0; i < final_lists.size(); i++) {
//                if (i < final_lists.size() - 1) {
//                    updateGamessInputArea((String) inputs.get(i));
//                    updateGamessInputArea("\n\n\n");
//                } else {
//                    updateGamessInputArea((String) inputs.get(i));
//                }
//
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        // Initializing serversList
//        serverDetailsList = new ArrayList<>();
//        try {
//            serverDetailsList = ServerConfigController.getServerDetailsList();
//        } catch (ClassNotFoundException | BackingStoreException | IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        List<String> serverNames = new ArrayList<>();
//        for (ServerDetails server : serverDetailsList) {
//            serverNames.add(server.getAddress());
//        }
//        serversList.setItems(FXCollections.observableList(serverNames));
//        if (serverNames.size() > 0)
//            serversList.setValue(serverNames.get(0));
//
//        gamessInputArea.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(final ObservableValue<? extends String> observable, final String oldValue,
//                                final String newValue) {
//                // this will run whenever text is changed
//                String[] arrays = newValue.split("\n\n\n");
//                inputs.clear();
//                for (int i = 0; i < arrays.length; i++) {
//                    if (arrays[i] != null && !arrays[i].trim().isEmpty()) {
//                        inputs.add(arrays[i]);
//                        System.out.println(arrays[i]);
//                    }
//
//                }
//            }
//        });
//    }
//
//    // This method will be called on update of any of the input fields
//    public void updateGamessInputArea(String text) throws FileNotFoundException {
//        gamessInputArea.setText(gamessInputArea.getText() + text);
//    }
//
//    /**
//     * Generate Gamess input Form from selected Fragments
//     */
//    public void generateGamessInputFiles() {
//
//        for (int i = 0; i < final_lists.size(); i++) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(" $contrl units=angs local=boys runtyp=makefp\n");
//            sb.append("       mult=1 icharg=0 coord=cart icut=11 $end\n");
//            sb.append(" $system timlim=99999   mwords=200 $end\n");
//            sb.append(" $scf soscf=.f. dirscf=.t. diis=.t. CONV=1.0d-06  $end\n");
//            sb.append(" $basis gbasis=n31 ngauss=6 ndfunc=1 $end\n");
//            sb.append(" $DAMP IFTTYP(1)=2,0 IFTFIX(1)=1,1 thrsh=500.0 $end\n");
//            String pol = "f";
//            String disp = "f";
//            String exrep = "f";
//            boolean[] interteted_parameters = MainViewController.get_interested_parameters();
//            if (interteted_parameters[0] == true) {
//                pol = "t";
//            }
//            if (interteted_parameters[1] == true) {
//                disp = "t";
//            }
//            if (interteted_parameters[2] == true) {
//                exrep = "t";
//            }
//
//            sb.append(" $MAKEFP  POL=." + pol + ". DISP=." + disp + ". CHTR=.f.  EXREP=." + exrep + ". $end\n");
//            sb.append(" $data\n");
//            sb.append("Fragment" + " " + (fragmentNumbers.get(i) + 1) + "\n");
//            sb.append(" C1\n");
//            for (int j = 0; j < final_lists.get(i).size(); j++) {
//                Atom a = (Atom) final_lists.get(i).get(j);
//
//                sb.append("  ");
//                sb.append(a.type);
//                sb.append("   ");
//                sb.append(String.format("%.1f", a.charge));
//                sb.append("   ");
//                sb.append(Double.toString(a.x));
//                sb.append("   ");
//                sb.append(Double.toString(a.y));
//                sb.append("   ");
//                sb.append(Double.toString(a.z));
//                sb.append("\n");
//
//            }
//            sb.append(" $end\n $comment Atoms to be erased:  $end\n");
//            // System.out.println(sb.toString());
//            inputs.add(sb.toString());
//        }
//    }
//
//    // Generate Q-Chem Input file
//    public void export() throws IOException {
//        DirectoryChooser chooser = new DirectoryChooser();
//        chooser.setTitle("Save Zip Files to...");
//        File defaultDirectory = new File(".");
//        chooser.setInitialDirectory(defaultDirectory);
//        Stage currStage = (Stage) root.getScene().getWindow();
//        File selectedDirectory = chooser.showDialog(currStage);
//
//        // System.out.println(selectedDirectory.getAbsolutePath());
//        FileOutputStream fos = new FileOutputStream(selectedDirectory.getAbsolutePath() + "/gamess.zip");
//        ZipOutputStream zos = new ZipOutputStream(fos);
//        for (int i = 0; i < inputs.size(); i++) {
//
//            byte[] b = ((String) inputs.get(i)).getBytes(Charset.forName("UTF-8"));
//            ZipEntry entry = new ZipEntry("gamess_" + Integer.toString(i) + ".inp");
//            entry.setSize(b.length);
//            zos.putNextEntry(entry);
//            zos.write(b);
//            zos.closeEntry();
//
//        }
//        zos.close();
//        fos.close();
//
//    }
//
//    public static void writeZipFile(File directoryToZip, List<File> fileList) {
//        try {
//            FileOutputStream fos = new FileOutputStream(directoryToZip.getName() + ".zip");
//            ZipOutputStream zos = new ZipOutputStream(fos);
//
//            for (File file : fileList) {
//                if (!file.isDirectory()) { // we only zip files, not directories
//                    addToZip(directoryToZip, file, zos);
//                }
//            }
//
//            zos.close();
//            fos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void addToZip(File directoryToZip, File file, ZipOutputStream zos)
//            throws FileNotFoundException, IOException {
//
//        FileInputStream fis = new FileInputStream(file);
//
//        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
//                file.getCanonicalPath().length());
//        System.out.println("Writing '" + zipFilePath + "' to zip file");
//        ZipEntry zipEntry = new ZipEntry(zipFilePath);
//        zos.putNextEntry(zipEntry);
//
//        byte[] bytes = new byte[1024];
//        int length;
//        while ((length = fis.read(bytes)) >= 0) {
//            zos.write(bytes, 0, length);
//        }
//
//        zos.closeEntry();
//        fis.close();
//    }
//
//    /**
//     * Handle job submission button on Gamess Form
//     *
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    public void handleSubmit() throws IOException, InterruptedException {
//        ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
//        System.out.println(selectedServer.getServerType());
//        if (selectedServer.getServerType().equalsIgnoreCase("local"))
//            submitJobToLocalServer(selectedServer);
//        else {
//
//            String hostname = selectedServer.getAddress();
//            LoginForm loginForm = new LoginForm(hostname, "GAMESS");
//            boolean authorized = loginForm.authenticate();
//
//            if (authorized) {
//                Connection conn = loginForm.getConnection(authorized);
//
//                String username = loginForm.getUsername();
//                String password = loginForm.getPassword();
//
//                ArrayList jobids = new ArrayList();
//
//                //For each fragment, generate input
//                for (int i = 0; i < inputs.size(); i++) {
//
//                    SCPClient scp = conn.createSCPClient();
//                    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
//                    Date date = new Date();
//                    String currentTime = dateFormat.format(date).toString();
//
//                    String jobID = (new JobManager()).generateJobID().toString();
//
//                    SCPOutputStream scpos = scp.put("gamess_" + jobID + ".inp", ((String) inputs.get(i)).length(),
//                            "./iSpiClient/Gamess/src", "0666");
//                    InputStream istream = IOUtils.toInputStream((String) inputs.get(i), "UTF-8");
//                    IOUtils.copy(istream, scpos);
//                    istream.close();
//                    scpos.close();
//
//                    String pbs_script = "cd iSpiClient/Gamess/src;\n ./rungms gamess_" + jobID + ".inp" + " > gamess_"
//                            + jobID + ".log";
//
//                    scpos = scp.put("pbs_" + jobID, pbs_script.length(), "./iSpiClient/Gamess/src", "0666");
//
//                    istream = IOUtils.toInputStream(pbs_script, "UTF-8");
//                    IOUtils.copy(istream, scpos);
//                    istream.close();
//                    scpos.close();
//
//                    Session sess = conn.openSession();
//
//                    sess.execCommand(
//                            "source /etc/profile; cd iSpiClient/Gamess/src; qsub -l walltime=00:30:00 -l nodes=1:ppn=1 -q standby pbs_"
//                                    + jobID);
//
//                    InputStream stdout = new StreamGobbler(sess.getStdout());
//                    BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//                    String clusterjobID = "";
//                    while (true) {
//                        String line = br.readLine();
//                        if (line == null)
//                            break;
//                        String[] tokens = line.split("\\.");
//                        if (tokens[0].matches("\\d+")) {
//                            clusterjobID = tokens[0];
//                        }
//                    }
//                    br.close();
//                    stdout.close();
//                    sess.close();
//
//
//                    jobids.add(clusterjobID);
//                    // Date date = new Date();
//                    Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);
//                    String str = date.toString() + "\n" + MainViewController.getLastOpenedFileName() + "\n" + hostname
//                            + "\n";// + uname + "\n" + pwd + "\n";
//                    System.out.println(str);
//                    System.out.println(clusterjobID);
//                    userPrefs.put(clusterjobID, str);
//
//                    userPrefs.put(clusterjobID, clusterjobID + "\n" + currentTime + "\n");
//
//                    String serverName = Main.iSpiEFP_SERVER;
//                    int port = Main.iSpiEFP_PORT;
//
//                    String title = "A_Default_title";
//                    String time = currentTime;
//
//                    // send over job data to database
//                    String query = "Submit";
//                    query += "$END$";
//                    query += username + "  " + hostname + "  " + jobID + "  " + title + "  " + time + "  " + "QUEUE"
//                            + "  " + "GAMESS";
//                    query += "$ENDALL$";
//
//                    // Socket client = new Socket(serverName, port);
//                    iSpiEFPServer iSpiServer = new iSpiEFPServer();
//                    Socket client = iSpiServer.connect(serverName, port);
//                    if (client == null) {
//                        return;
//                    }
//                    OutputStream outToServer = client.getOutputStream();
//                    // DataOutputStream out = new DataOutputStream(outToServer);
//
//                    System.out.println(query);
//                    outToServer.write(query.getBytes("UTF-8"));
//                    client.close();
//                    outToServer.close();
//
//                    //poll for job finishing
////                    JobManager jobManager = new JobManager(, null, jobID, title, time, "QUEUE",
////                            "GAMESS");
////                    jobManager.watchJobStatus();
//
//                    //send success alert to user
//                    Alert alert = new Alert(AlertType.INFORMATION);
//                    alert.setTitle("Gamess Submission");
//                    alert.setHeaderText(null);
//                    alert.setContentText("Job submitted to cluster successfully.");
//                    alert.showAndWait();
//                }
//                conn.close();
//
//            }
//
//        }
//        // Handle SSH case later
//    }
//
//    private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
//        // // Printing the complete absolute path from where the application was
//        // initialized
//        // System.out.println("Your working Directory is " +
//        // System.getProperty("user.dir"));
//        // String path = System.getProperty("user.dir");
//        // String executablePath = selectedServer.getWorkingDirectory();
//        // System.out.println("Executable path is: " + executablePath);
//        // String inputFileName = path + "/vmolAppJob_" + title.getText();
//        // String outputFileName = path += "/vmolAppJob_" + title.getText() +
//        // "_output";
//        //
//        // // First create the input File at that location with the content in
//        // qchemInputTextArea
//        // boolean inputFileCreated = createInputFile(inputFileName);
//        // if (!inputFileCreated) return; // Can probably return some error here
//        // List<String> command = new ArrayList<String>();
//        // command.add(executablePath);
//        // command.add(inputFileName);
//        // ProcessBuilder builder = new ProcessBuilder(command);
//        // builder.redirectOutput(new File(outputFileName));
//        //
//        // final Process process = builder.start();
//        //
//        // int errorCode = process.waitFor(); // 0 means everything went well!
//        // System.out.println("Process execution completed with errorCode : " +
//        // String.valueOf(errorCode));
//        //
//        // if (outputFileName.length() != 0) {
//        // System.out.println("Printing output File contents: ");
//        // Scanner s = null;
//        // try {
//        // s = new Scanner(new File(outputFileName));
//        // while (s.hasNextLine()) {
//        // System.out.println(s.nextLine());
//        // }
//        // } catch (Exception e){
//        // e.printStackTrace(System.out);
//        // } finally {
//        // if (s != null) s.close();
//        // }
//        // }
//    }
//
//    // Creates an input file at this location
//    private boolean createInputFile(String inputFileName) {
//        BufferedWriter output = null;
//        try {
//            File file = new File(inputFileName);
//            output = new BufferedWriter(new FileWriter(file));
//            output.write(gamessInputArea.getText());
//            output.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

}
