package org.ispiefp.app;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.ispiefp.app.EFPFileRetriever.LibEFPtoCSV;
//import org.ispiefp.app.gamess.gamessInputController;
//import org.ispiefp.app.gamess.GamessInputController;
import org.ispiefp.app.gamess.GamessInputController;
import org.ispiefp.app.analysis.GeometryAnalysisController;
import org.ispiefp.app.libEFP.OutputFile;
import org.ispiefp.app.gamess.gamessInputController;
import org.ispiefp.app.libEFP.libEFPInputController;
import org.ispiefp.app.metaDataSelector.MetaDataSelectorController;
import org.ispiefp.app.server.JobManager;
import org.ispiefp.app.submission.JobViewController;
import org.ispiefp.app.submission.JobsMonitor;
import org.ispiefp.app.submission.SubmissionRecord;
import org.ispiefp.app.util.*;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
//import org.ispiefp.app.database.DatabaseController;
import org.ispiefp.app.gamessSubmission.gamessSubmissionHistoryController;
import org.ispiefp.app.loginPack.LoginForm;
import org.ispiefp.app.submission.SubmissionHistoryController;
import org.ispiefp.app.visualizer.JmolMainPanel;

import org.ispiefp.app.visualizer.JmolPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static org.ispiefp.app.util.UserPreferences.appendToRecentFilesStr;
import static org.ispiefp.app.util.UserPreferences.getRecentFileAggStr;
import static org.junit.Assert.assertTrue;

public class MainViewController {

    private static final Image halo = new Image(Main.class.getResource("/images/select.png").toString());
    private static final Image scissors = new Image(Main.class.getResource("/images/scissors.png").toString());
    private static final Image play = new Image(Main.class.getResource("/images/play.png").toString());
    private static final Image pause = new Image(Main.class.getResource("/images/pause.png").toString());
    private static final Image terminal = new Image(Main.class.getResource("/images/terminal.png").toString());
    private static final Image ruler = new Image(Main.class.getResource("/images/ruler.png").toString());
    private static final Image center = new Image(Main.class.getResource("/images/center.png").toString());
    private static final Image selectAll = new Image(Main.class.getResource("/images/select_all.png").toString());
    private static final Image build = new Image(Main.class.getResource("/images/build.png").toString());

    private static String lastOpenedFile = new String();
    private static String lastOpenedFileName = new String();
    private static boolean[] interested_parameters = {false, false, false};
    private static ProgressIndicator pi = new ProgressIndicator();
    private static final String FIVE_MOST_RECENT_FILES = "RECENT_FILES";

    private ProgressIndicator pit = new ProgressIndicator();

    private JmolMainPanel jmolMainPanel;    //Main Viewer Container for Jmol Viewer
    private double upperXBound;
    private double upperYBound;
    private double maxXVal;
    private double maxYVal;
    private boolean xPressed;
    private boolean yPressed;

    @FXML
    private Parent root;

    /******************************************************************************************
     *             PANES & LISTS SECTION BEGINS                                               *
     ******************************************************************************************/
    @FXML
    private SplitPane leftRightSplitPane;

    @FXML
    private ListView<String> leftListView;

    @FXML
    private SplitPane middleRightSplitPane;

    @FXML
    private Pane middlePane;

    @FXML
    private SplitPane rightVerticalSplitPane;

    @FXML
    private Pane upperRightPane;

    @FXML
    private Pane bottomRightPane;

    /******************************************************************************************
     *             ICON BUTTON SECTION BEGINS                                         *
     ******************************************************************************************/
    @FXML
    private ToggleButton selectionButton;

    @FXML
    private ToggleButton haloButton;

    @FXML
    private ToggleButton snipButton;

    @FXML
    private ToggleButton measureButton;

    @FXML
    private ToggleButton pickCenterButton;

    @FXML
    private ToggleButton playPauseButton;

    @FXML
    private ToggleButton modelKitButton;

    @FXML
    private Button consoleButton;

    @FXML
    private Button libefpButton;

    @FXML
    private Menu openRecentMenu;


    // All the analysis buttons
    public Button analysisGeometries;
    public Button analysisEnergies;
    public Button analysisPairWiseEnergies;
    public Button analysisMD;
    public Button analysisStats;

    // History and Project List View
    @FXML
    private TreeView<String> historyTreeView;
    @FXML
    private TreeItem<String> historyRoot;

    //private UserPreferences userPrefs = new UserPreferences();

    private String[] rec_files;
    //private Menu recentMenu;

    /**
     * initialize(); is called after @FXML parameters have been loaded in
     * Loading order goes as: Constructor > @FXML > initialize();
     */
    @FXML
    public void initialize() {
        //set graphics
        selectionButton.setText("");
        selectionButton.setGraphic(new ImageView(selectAll));
        haloButton.setText("");
        haloButton.setGraphic(new ImageView(halo));
        snipButton.setText("");
        snipButton.setGraphic(new ImageView(scissors));
        measureButton.setText("");
        measureButton.setGraphic(new ImageView(ruler));
        pickCenterButton.setText("");
        pickCenterButton.setGraphic(new ImageView(center));
        playPauseButton.setText("");
        playPauseButton.setGraphic(new ImageView(play));
        modelKitButton.setText("");
        modelKitButton.setGraphic(new ImageView(build));
        consoleButton.setText("");
        consoleButton.setGraphic(new ImageView(terminal));

        jmolMainPanel = new JmolMainPanel(middlePane, leftListView);


        leftRightSplitPane.setDividerPositions(0.2f, 0.3f);
        middleRightSplitPane.setDividerPositions(1, 0);

        //TODO refactor the libefp button this exact phrase is also located in openFile MainViewController
        libefpButton.setDisable(true);

        /* Populate the historyTreeView */
        class HistoryTreeUpdater extends Task<TreeView<String>> {
            private TreeView<String> t;
            private HashMap<String, TreeItem> tMap;

            public HistoryTreeUpdater(TreeView<String> s) {
                t = s;
                tMap = new HashMap<>();
            }

            /*
            It is key to remember in this function the indices of the informative children nodes for a jo. I list them here:
            0 - job status
             */
            @Override
            protected TreeView<String> call() throws Exception {
                JobsMonitor jobsMonitor = UserPreferences.getJobsMonitor();
                HashSet<String> accountedForJobs = new HashSet<>();
                ConcurrentHashMap<String, SubmissionRecord> records = jobsMonitor.getRecords();
                historyRoot.setValue("Jobs");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                System.out.printf("Size of jobs is currently %d%n", jobsMonitor.getJobs().size());
                System.out.printf("Size of tMap is currently %d%n", tMap.size());
                System.out.printf("Size of records is currently %d%n", records.size());
                while (true) {
                    if (tMap.size() < records.size()) {
                        Enumeration<String> recordEnumeration = records.keys();
                        while (recordEnumeration.hasMoreElements()) {
                            String currentRecordName = recordEnumeration.nextElement();
                            if (!accountedForJobs.contains(currentRecordName)) {
                                accountedForJobs.add(currentRecordName);
//                                Text idText = new Text(currentRecordName);
                                TreeItem<String> jobIDTreeItem = new TreeItem<>(currentRecordName);
                                historyRoot.getChildren().add(jobIDTreeItem);
                                if (!tMap.containsKey(currentRecordName)) {
                                    tMap.put(currentRecordName, jobIDTreeItem);
                                    jobIDTreeItem.getChildren().add(0, new TreeItem<>());
                                }
                            }
                        }
                    }
                    Date currentTime = new Date();
                    Enumeration<String> recordEnumeration = records.keys();
                    while (recordEnumeration.hasMoreElements()) {
                        String currentRecordName = recordEnumeration.nextElement();
                        SubmissionRecord currentRecord = records.get(currentRecordName);
                        TreeItem<String> jobIDTreeItem = tMap.get(currentRecordName);
//                        TreeItem<Text> jobIDTreeItem = tMap.get(currentRecordName);
                        TreeItem<String> jobStatusTreeItem = jobIDTreeItem.getChildren().get(0);
//                        TreeItem<Text> jobStatusTreeItem = jobIDTreeItem.getChildren().get(0);
                        if (currentRecord.getStatus().equalsIgnoreCase("COMPLETE")) {
//                            Text statusText = new Text("Status: " + currentRecord.getStatus());
//                            statusText.setFill(Color.GREEN);
                            jobStatusTreeItem.setValue(currentRecord.getStatus());
//                            jobStatusTreeItem.setValue(statusText);
                        } else if (currentRecord.getStatus().equalsIgnoreCase("ERROR")) {
//                            Text statusText = new Text("Status: " + currentRecord.getStatus());
//                            statusText.setFill(Color.RED);
                            jobStatusTreeItem.setValue(currentRecord.getStatus());
                        } else {
                            try {
                                Date submissionTime = dateFormatter.parse(currentRecord.getTime());
                                long diffIn_ms = Math.abs(currentTime.getTime() - submissionTime.getTime());
                                long remainingTime_ms = diffIn_ms; // TimeUnit.MINUTES.convert(diffIn_ms, TimeUnit.MILLISECONDS);
                                long hours = TimeUnit.MILLISECONDS.toHours(remainingTime_ms);
                                remainingTime_ms -= TimeUnit.HOURS.toMillis(hours);
                                long mins = TimeUnit.MILLISECONDS.toMinutes(remainingTime_ms);
                                remainingTime_ms -= TimeUnit.MINUTES.toMillis(mins);
                                long secs = TimeUnit.MILLISECONDS.toSeconds(remainingTime_ms);

                                String runningTimeString = String.format("Status: Running(%02d:%02d:%02d)", hours, mins, secs);
//                                Text timeText = new Text(runningTimeString);
//                                timeText.setFill(Color.GOLD);
//                                jobStatusTreeItem.setValue(timeText);
                                jobStatusTreeItem.setValue(runningTimeString);
                            } catch (ParseException e) {
                                System.err.println("Was unable to parse the time of submission in its current format");
                            }
                        }
                    }
                    Thread.sleep(500);
                }
            }
        }
        Task<TreeView<String>> historyTreeUpdater = new HistoryTreeUpdater(historyTreeView);
        new Thread(historyTreeUpdater).start();

        /* Create "Delete Job" context menu option */
        MenuItem deleteRecordOption = new MenuItem("Delete Job");
        deleteRecordOption.setOnAction(action -> {
            try {
                String jobID = ((TreeItem<String>) historyTreeView.getSelectionModel().getSelectedItem()).getValue();
                /* 1. Kill the job on the server if it is running todo */
                /* 2. Remove it from the list of jobs on the jobsMonitor */
                //todo It now occurs to me that it would be more efficient to use a BST DS for the jobs instead of an arraylist
                CopyOnWriteArrayList<JobManager> runningJobs = UserPreferences.getJobsMonitor().getJobs();
                for (int i = 0; i < runningJobs.size(); i++) {
                    if (runningJobs.get(i).getJobID().equals(jobID)) runningJobs.remove(i);
                }
                /* 3. Remove it's record from the jobsMonitor's submission record */
                UserPreferences.getJobsMonitor().deleteRecord(
                        UserPreferences.getJobsMonitor().getRecords().get(jobID));
                /* 4. Remove it from the history pane */
                historyRoot.getChildren().remove(historyTreeView.getSelectionModel().getSelectedItem());
                System.out.println("Selected item is of class: " + ((TreeItem<String>) historyTreeView.getSelectionModel().getSelectedItem()).getValue());
            } catch (ClassCastException e) {
                //This is a cheap solution to the issue of the user being able to right click the root node.
            }
        });

        /* Create "View Job Information" Context Menu Option */
        MenuItem viewJobInfoOption = new MenuItem("View Job Info");
        viewJobInfoOption.setOnAction(action -> {
//            viewJobInfoOption.setDisable(true);
            String jobID = ((TreeItem<String>) historyTreeView.getSelectionModel().getSelectedItem()).getValue();
            ConcurrentHashMap<String, SubmissionRecord> records = UserPreferences.getJobsMonitor().getRecords();
            if (records.get(jobID).getStatus().equals("RUNNING")) {
                viewJobInfoOption.setDisable(true);
            }
            /* Pull up a view displaying all information about the job */
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/JobView.fxml"));
                JobViewController jobViewController = new JobViewController(records.get(jobID));
                loader.setController(jobViewController);
                Parent p = loader.load();

                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Job Information");
                stage.setScene(new Scene(p));

                try {
                    stage.showAndWait();
                } catch (Exception e) {
                    System.err.println("Unable to open new view");
                }
            } catch (IOException e) {
                System.err.println("Was unable to locate the view");
                e.printStackTrace();
            }
        });
        MenuItem exportCSVOption = new MenuItem("Export to CSV");
        exportCSVOption.setOnAction(action -> {
            String jobID = ((TreeItem<String>) historyTreeView.getSelectionModel().getSelectedItem()).getValue();
            ConcurrentHashMap<String, SubmissionRecord> records = UserPreferences.getJobsMonitor().getRecords();
            if (!records.get(jobID).getStatus().equals("COMPLETE")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please wait until job finishes.", ButtonType.OK);
                alert.showAndWait();
            } else {
                String localPathPrefix = records.get(jobID).getLocalOutputFilePath();
                String output = records.get(jobID).getOutputFilePath();
                String localPathTotal = localPathPrefix.substring(0, localPathPrefix.indexOf("/")) + File.separator + output;
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save CSV File");
                String path = records.get(jobID).getOutputFilePath();
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
                fileChooser.setInitialDirectory(new File(new File(localPathPrefix).getParent()));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                Stage currStage = (Stage) root.getScene().getWindow();
                LibEFPtoCSV libEFPtoCSV = new LibEFPtoCSV();
                String[] sheets = libEFPtoCSV.getCSVString(localPathTotal);
                for (int j = 0; j < sheets.length; j++) {
                    if (sheets[j] != null) {
                        if (j == 0) {
                            fileChooser.setInitialFileName(fileName + "_ene.csv");
                        } else {
                            fileChooser.setInitialFileName(fileName + "_pw.csv");
                        }
                        File file = fileChooser.showSaveDialog(currStage);
                        if (file != null) {
                            try {
                                FileWriter fileWriter = new FileWriter(file);
                                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                bufferedWriter.write(sheets[j]);
                                bufferedWriter.close();
                                fileWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        historyTreeView.setContextMenu(new ContextMenu(deleteRecordOption, viewJobInfoOption, exportCSVOption));
    }

    /**
     * Constructor for JavaFX main view controller.
     * Loading order goes as: Constructor > @FXML > initialize();
     */
    public MainViewController() {

    }

    public static String getLastOpenedFile() {
        return lastOpenedFile;
    }

    public static String getLastOpenedFileName() {
        return lastOpenedFileName.substring(0, lastOpenedFileName.length() - 4);
    }

    public static boolean[] get_interested_parameters() {
        return interested_parameters;
    }


    /******************************************************************************************
     *             FILE MENU BEGINS                                                           *
     ******************************************************************************************/

    @FXML
    /**
     * parse a pdb or xyz file and load the main jmolPanel
     * @throws IOException
     * @throws UnrecognizedAtomException
     */
    public void fileOpen() throws IOException, UnrecognizedAtomException {

        openRecentMenu.getItems().clear();
        // pit.setProgress(100);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Molecule");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XYZ", "*.xyz"),
                new FileChooser.ExtensionFilter("PDB", "*.pdb")
        );
        Stage currStage = (Stage) root.getScene().getWindow();

        File file = fileChooser.showOpenDialog(currStage);

        // user canceled file selection
        if (file == null) return;

        jmolMainPanel = new JmolMainPanel(middlePane, leftListView);
        if (jmolMainPanel.openFile(file)) {

            //Logic for saving to recent files
            lastOpenedFile = file.getAbsolutePath();
            appendToRecentFilesStr(lastOpenedFile);
            lastOpenedFileName = file.getName();

            leftRightSplitPane.setDividerPositions(0.2f, 0.3f);
            middleRightSplitPane.setDividerPositions(1, 0);

            //reset buttons
            haloButton.setSelected(false);
            snipButton.setSelected(false);
            playPauseButton.setText("");
            playPauseButton.setGraphic(new ImageView(play));
            playPauseButton.setSelected(false);

            //TODO refactor the libefp button
            libefpButton.setDisable(true);

//            System.out.println("Recents chain: " + getRecentFileAggStr());
            rec_files = getRecentFileAggStr().split("::");
            System.out.println("Rec files array in fileOpen: " + Arrays.toString(rec_files));
            populateOpenRecentMenu(); //populates menu w/ rec_files, it's global so not passed as parameter
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("File Does Not Exist");
            alert.setContentText("We couldn't find the file you wanted. Try checking if the path is correct.");

            alert.showAndWait();
        }
    }


    /*
     File open recent:
     Updated 5/11/20:
     Logic for fileOpenRecent contained in 3 methods below:
        - repopulate
        - populateOpenRecentMenu
        - fileOpenFromPath
        These methods are used as of the time of writing only for recent files logic.
     */

    public void repopulate() {
        openRecentMenu.getItems().clear();
        rec_files = getRecentFileAggStr().split("::");
        for (int i = rec_files.length - 1; i >= 0; i--) {

            MenuItem mi = new MenuItem(rec_files[i]);

            mi.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    try {
                        appendToRecentFilesStr(mi.getText());
                        fileOpenFromPath(mi.getText());
                        repopulate();
                    } catch (Exception e) {
                        System.out.println("Exception in fileOpenPath");
                        e.printStackTrace();
                    }
                }
            });

            openRecentMenu.getItems().add(mi);
        }

    }

    //Handler method for fileOpenRecent button/File open recent
    public void populateOpenRecentMenu() throws IOException {
        openRecentMenu.getItems().clear();
        rec_files = getRecentFileAggStr().split("::");

        for (int i = rec_files.length - 1; i >= 0; i--) {

            MenuItem mi = new MenuItem(rec_files[i]);

            mi.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    try {
                        appendToRecentFilesStr(mi.getText());
                        fileOpenFromPath(mi.getText());
                        repopulate();

                    } catch (Exception e) {
                        System.out.println("Exception in fileOpenPath");
                    }
                }
            });

            openRecentMenu.getItems().add(mi);
        }

    }

    /*
        Used for opening files in the recent file list //ONLY USED FOR FILEOPENRECENT
    */
    public void fileOpenFromPath(String moleculePath) throws IOException {
        File jmolFile = new File(moleculePath);
        jmolMainPanel = new JmolMainPanel(middlePane, leftListView);

        try {
            if (jmolMainPanel.openFile(jmolFile)) {

                leftRightSplitPane.setDividerPositions(0.2f, 0.3f);
                middleRightSplitPane.setDividerPositions(1, 0);

                //reset buttons
                haloButton.setSelected(false);
                snipButton.setSelected(false);
                playPauseButton.setText("");
                playPauseButton.setGraphic(new ImageView(play));
                playPauseButton.setSelected(false);

                //TODO refactor the libefp button
                libefpButton.setDisable(true);
            }

        } catch (IOException e) {
            System.out.println("IOException");
        } catch (Exception e) {
            System.out.println("General Exception");
            e.printStackTrace();
        }
    }

    @FXML
    /**
     * Opens a new stage for selecting a fragment from those contained within the fragmentTree
     * which has been built. Then hands off control to the MetaDataSelectorController
     */
    public void fragmentOpen() throws IOException {
        Stage stage = new Stage();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/metaDataSelector.fxml"));
        Parent fragmentSelector = loader.load();

        MetaDataSelectorController metaDataSelectorController = loader.getController();
        metaDataSelectorController.setData(stage);

        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Select Fragment");
        stage.setScene(new Scene(fragmentSelector));

        try {
            stage.showAndWait();    //TODO: Fixxxx. This causes errors when you do Cmnd+Tab
        } catch (Exception e) {
            System.err.println("FRAGMENT MAIN VIEW ERROR");
        }

        File xyzFile;

        try {
            xyzFile = Main.fragmentTree.getSelectedFragment().createTempXYZ();

//            Do not need to create a new panel every time, this actually causes issue
//            jmolMainPanel = new JmolMainPanel(middlePane, leftListView);

            if (jmolMainPanel.openFile(xyzFile)) {

                lastOpenedFile = xyzFile.getAbsolutePath();
                lastOpenedFileName = xyzFile.getName();

                leftRightSplitPane.setDividerPositions(0.2f, 0.3f);
                middleRightSplitPane.setDividerPositions(1, 0);

                //reset buttons
                haloButton.setSelected(false);
                snipButton.setSelected(false);
                playPauseButton.setText("");
                playPauseButton.setGraphic(new ImageView(play));
                playPauseButton.setSelected(false);
            }
        } catch (NullPointerException e) {
            System.out.println("User closed window without selecting a fragment");
        }
    }

    public void openSettings() throws IOException{
        Parent settingsView = FXMLLoader.load(getClass().getResource("/views/SettingsView.fxml"));
        Stage stage = new Stage();
        stage.setMinWidth(800);
        stage.setMinHeight(630);
        stage.setHeight(630);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Settings");
        stage.setScene(new Scene(settingsView));

        try {
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("SHOW AND WAIT ERROR IN MAIN");
        }
    }


    public void selectFragment() throws IOException {
        String noInternetWarning = "You are not currently connected to the internet.\n\n" +
                "You will only be able to select from " +
                "fragments whose parameters are contained within your user parameters directory.";
        if (!VerifyPython.isValidPython()) {
            VerifyPython.raisePythonError();
            return;
        }
        if (!CheckInternetConnection.checkInternetConnection()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noInternetWarning,
                    ButtonType.OK);
            alert.showAndWait();
            fragmentOpen();
        } else fragmentOpen();
    }

    /**
     * TODO: fileOpenRecent this button does not exist in the fxml doc and needs to be added
     *
     * @throws IOException This is currently disabled in the fxml doc since it is not currently operational
     */


    @FXML
    public void fileExit() throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit iSpiEFP");
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // ... user chose OK
            System.out.println("Stage is closing");
            Main.getPrimaryStage().close();
            Platform.exit();
        } else {
            // ... user chose CANCEL or closed the dialog
            System.out.println("User cancelled. Stage not closing");
        }
    }

    /******************************************************************************************
     *             EDITS MENU BEGINS                                                          *
     ******************************************************************************************/
    @FXML

    public void openLibEFPWindow() throws IOException {
        Parent libEFPInput = FXMLLoader.load(getClass().getResource("views/libEFP.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Libefp Input");
        stage.setScene(new Scene(libEFPInput));
        pit.setProgress(100);
        stage.show();

    }

    public void editUndo() throws IOException {
        jmolMainPanel.undoDeleteBond();
    }

    @FXML
    public void editRedo() throws IOException {
        //TODO JmolMainPanel history is current kept in a stack which pops only allowing an undo.
        /* to implement undo change the stack to an arraylist and keep an index of the current */

        //This is currently disabled in the fxml doc since it is not currently operational
    }

    @FXML
    public void openAbout() throws IOException {    //Help -> About dialog
        Alert a1 = new Alert(Alert.AlertType.CONFIRMATION, "hello");
        a1.setTitle("About iSpiEFP");
        a1.setHeaderText("About Us");
        a1.setContentText("iSpiEFP is a tool for visualizing and describing molecular systems with the EFP method created and managed " +
                "by the Slipchenko Research Group at Purdue University. iSpiEFP comes complete with a public database full of " +
                "EFP Parameter files, and missing parameters can be calculated using Gamess. This application serves as a " +
                "job-workflow manager which binds different technologies into one single application that allows chemists " +
                "to point and click while utilizing high performance computing. iSpiEFP is under the LGPL 2.1 license");
        a1.showAndWait();

    }

    public void editSelectAll() throws IOException {
        jmolMainPanel.viewer.runScript("selectionHalos on");
        jmolMainPanel.viewer.runScript("select all");
        jmolMainPanel.repaint();
    }

    @FXML
    public void editSelectNone() throws IOException {
        jmolMainPanel.viewer.runScript("selectionHalos off");
        jmolMainPanel.viewer.runScript("select none");
        jmolMainPanel.repaint();
    }

    /******************************************************************************************
     *             VIEW MENU BEGINS                                                           *
     ******************************************************************************************/
    @FXML
    public void viewFullScreen() throws IOException {
        Main.getPrimaryStage().setFullScreen(true);
        Main.getPrimaryStage().setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    @FXML
    public void viewWindowed() throws IOException {
        Main.getPrimaryStage().setFullScreen(false);
    }

    @FXML
    public void viewTop() throws IOException {
        jmolMainPanel.viewer.runScript("moveto 0 1 0 0 -90");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewLeft() throws IOException {
        jmolMainPanel.viewer.runScript("moveto 0 0 1 0 -90");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewRight() throws IOException {
        jmolMainPanel.viewer.runScript("moveto 0 0 1 0 90");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewBottom() throws IOException {
        jmolMainPanel.viewer.runScript("moveto 0 1 0 0 90");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewCenter() throws IOException {
//        jmolMainPanel.viewer.runScript("moveto 0 0 0 0 0 100");
        jmolMainPanel.viewer.runScript("moveto 0 {0 0 1} 0");
        jmolMainPanel.repaint();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //             ZOOM SUB MENU BEGINS                                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    public void viewZoomIn() throws IOException {
        jmolMainPanel.viewer.runScript("zoom in");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewZoomOut() throws IOException {
        jmolMainPanel.viewer.runScript("zoom out");
        jmolMainPanel.repaint();
    }

    public void openLibEFPServers() throws IOException {
        Parent serversList = FXMLLoader.load(getClass().getResource("views/LibEFPServers.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("LibEFP Servers");
        stage.setScene(new Scene(serversList));
        stage.show();
    }

    @FXML
    public void viewZoom50() throws IOException {
        jmolMainPanel.viewer.runScript("zoom 50");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewZoom75() throws IOException {
        jmolMainPanel.viewer.runScript("zoom 75");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewZoom100() throws IOException {
        jmolMainPanel.viewer.runScript("zoom 100");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewZoom150() throws IOException {
        jmolMainPanel.viewer.runScript("zoom 150");
        jmolMainPanel.repaint();
    }

    @FXML
    public void viewZoom200() throws IOException {
        jmolMainPanel.viewer.runScript("zoom 200");
        jmolMainPanel.repaint();
    }

    /******************************************************************************************
     *             SEARCH MENU BEGINS                                                         *
     ******************************************************************************************/
//    @FXML
    /**
     * Handle Search Fragments button. Search the database for similar fragments to the current molecule
     */
//    public void searchFindEFPPublicDatabase() throws IOException {
//        if (!lastOpenedFile.isEmpty()) {
//            //set divider positions
//            middleRightSplitPane.setDividerPositions(0.6f, 0.4f);
//            rightVerticalSplitPane.setDividerPositions(0.5f, 0.5f);
//
//            //Runs auxiliary JmolViewer
//            JmolPanel jmolPanel = new JmolPanel(upperRightPane);
//
//            //load aux table list
//            DatabaseController DBcontroller = new DatabaseController(bottomRightPane, jmolMainPanel, jmolPanel.viewer, jmolMainPanel.getFragmentComponents());
//            try {
//                //start database controller actions
//                DBcontroller.run();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("No file was opened");
//        }
//    }

    /******************************************************************************************
     *             CALCULATE MENU BEGINS                                                      *
     ******************************************************************************************/
    @FXML
    public void calculateLibefpSetup() throws IOException {
        String noInternetWarning = "You are not currently connected to the internet.\n\n" +
                "You will not be able to submit libEFP jobs to a cluster.";
        if (!CheckInternetConnection.checkInternetConnection()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noInternetWarning,
                    ButtonType.OK);
            alert.showAndWait();
        }
        if (jmolMainPanel.getFragmentComponents() == null) {
            String noFragmentsSelectedWarning = "You do not currently have any fragments in the viewer to perform" +
                    " calculations on. Add something to the system before attempting to perform libEFP calculations.";
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noFragmentsSelectedWarning,
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        FXMLLoader libEFPSubmissionLoader = new FXMLLoader(getClass().getResource("/views/libEFP.fxml"));
        //libEFPInputController libEFPCont = new libEFPInputController(getFragmentEFPFiles());
        Parent libEFPSubmissionParent = libEFPSubmissionLoader.load();
        libEFPInputController libEFPCont = libEFPSubmissionLoader.getController();
        //libEFPSubmissionLoader.setController(libEFPCont);
        libEFPCont.setJmolViewer(jmolMainPanel.viewer);
        libEFPCont.setViewerFragments(jmolMainPanel.getFragmentComponents());
        libEFPCont.initEfpFiles();
//        libEFPCont.setEfpFiles(getFragmentEFPFiles());
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Select Fragment");
        stage.setScene(new Scene(libEFPSubmissionParent));
        stage.showAndWait();
        /* Implementation currently assumes that the fragment obtained from select fragment has not been
        modified in any way. //TODO Make this general
         */
    }


    //TODO: This method should later return EFPFiles for every fragment in the viewer
    public ArrayList<File> getFragmentEFPFiles() {
        ArrayList<File> returnList = new ArrayList<>();
        returnList.add(Main.fragmentTree.getSelectedFragment().getEfpFile());
        System.out.println("return list is of size " + returnList.size());
        return returnList;
    }

    @FXML
    public void calculateLibefpHistory() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/views/submissionHistory.fxml"
                )
        );
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(
                new Scene(
                        loader.load()
                )
        );
        SubmissionHistoryController controller = loader.getController();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Submission History");
        stage.show();
    }


    @FXML
    public void calculateGamessSetup () throws IOException {
        String noInternetWarning = "You are not currently connected to the internet.\n\n" +
                "You will not be able to submit GAMESS jobs to a cluster.";
        if (!CheckInternetConnection.checkInternetConnection()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noInternetWarning,
                    ButtonType.OK);
            alert.showAndWait();
        }
        if (jmolMainPanel.getFragmentComponents() == null){
            String noFragmentsSelectedWarning = "You do not currently have any fragments in the viewer to perform" +
                    " calculations on. Add something to the system before attempting to perform GAMESS calculations.";
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noFragmentsSelectedWarning,
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        FXMLLoader gamessSubmissionLoader = new FXMLLoader(getClass().getResource("/views/gamessInput.fxml"));
        Parent gamessSubmissionParent = gamessSubmissionLoader.load();
        GamessInputController gamessInputController = gamessSubmissionLoader.getController();
        gamessInputController.setXyzFile(JmolHandler.createTempXYZFileFromViewer(jmolMainPanel, 0));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Select Fragment");
        stage.setScene(new Scene(gamessSubmissionParent));
        stage.showAndWait();

    }

    @FXML
    public void calculateGamessHistory() throws IOException {
        LoginForm loginForm = new LoginForm("GAMESS");
        boolean authorized = loginForm.authenticate();
        if (authorized) {
            gamessSubmissionHistoryController controller = new gamessSubmissionHistoryController(loginForm.getUsername(), loginForm.getPassword(), loginForm.getHostname());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "views/submissionHistory.fxml"
                    )
            );
            loader.setController(controller);

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setScene(
                    new Scene(
                            loader.load()
                    )
            );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Gamess Submission History");
            stage.show();
        }
    }

    @FXML
    public void calculateEditServers() throws IOException {
        Parent serversList = FXMLLoader.load(getClass().getResource("/views/ServersList.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Servers list");
        stage.setScene(new Scene(serversList));
        stage.show();
    }

    /******************************************************************************************
     *             HELP MENU BEGINS                                                           *
     ******************************************************************************************/
    @FXML
    public void helpCheckForUpdates() throws IOException {
        //TODO
        //This is currently disabled in the fxml doc since it is not currently operational
        CheckUpdates checkUpdates = new CheckUpdates();
        String[] versions = checkUpdates.getVersions();
        if (versions.length != 2) {
            throw new IOException();
        }
        Alert alert;
        if (versions[0].compareTo(versions[1]) == 0) {
            alert = new Alert(Alert.AlertType.INFORMATION, "You are up to date.\nVersion: "
                    + versions[0], ButtonType.OK);
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION, "Update available: Version " +
                    versions[1] + "\nCurrently using: Version " + versions[0], ButtonType.OK);
        }
        alert.showAndWait();
        //jmolMainPanel.repaint();
        return;
    }

    @FXML
    public void helpAbout() throws IOException {
        Main.hostServices.showDocument("https://www.chem.purdue.edu/Slipchenko/");
    }

    @FXML
    public void helpJmolWiki() throws IOException {
        Main.hostServices.showDocument("http://jmol.sourceforge.net/");
    }

    @FXML
    public void helpJmolConsole() throws IOException {
        //create window for console
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame consoleFrame = new JFrame();
        consoleFrame.setSize(800, 400);
        consoleFrame.setLocation(
                (screenSize.width - 500) / 2,
                (screenSize.height) / 2);
        consoleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //create and connect panel with jmol console
        JPanel console_panel = new JPanel();
        console_panel.setLayout(new BorderLayout());
        AppConsole console = new AppConsole(jmolMainPanel.viewer, console_panel,
                "Editor Font Variables History State Clear Help");

        // Callback any scripts run in console to jmol viewer in main
        jmolMainPanel.viewer.setJmolCallbackListener(console);

        //show console
        consoleFrame.getContentPane().add(console_panel);
        consoleFrame.setVisible(true);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleFrame.toFront();
                consoleFrame.repaint();
            }
        });
    }

/*
@FXML
public void openLibEFPWindow() throws IOException {
Parent libEFPInput = FXMLLoader.load(getClass().getResource("views/libEFP.fxml"));
Stage stage = new Stage();
stage.initModality(Modality.WINDOW_MODAL);
stage.setTitle("Libefp Input");
stage.setScene(new Scene(libEFPInput));
stage.show();
}*/

/*
@FXML
public void openGamessWindow() throws IOException {
Parent gamessInput = FXMLLoader.load(getClass().getResource("views/gamessInput.fxml"));
Stage stage = new Stage();
stage.initModality(Modality.WINDOW_MODAL);
stage.setTitle("Gamess Input");
stage.setScene(new Scene(gamessInput));
stage.show();
}*/

    /******************************************************************************************
     *             ICON BUTTON HANDLER SECTION BEGINS
     *
     *             The following section is for the Button Pane below the Menu Bar
     *             containing all of the icon buttons. These buttons interact with the
     *             Jmol Viewer object, and provide some default tools found in Jmol, as
     *             well as some custom buttons.
     ******************************************************************************************/
    /**
     * Handle Selection Toggle Button. Select all atoms and highlight
     */
    @FXML
    public void toggleSelection() {
        if (selectionButton.isSelected()) {
            jmolMainPanel.viewer.runScript("selectionHalos on");
            jmolMainPanel.viewer.runScript("select all");
            jmolMainPanel.repaint();
        } else {
            jmolMainPanel.viewer.runScript("selectionHalos off");
            jmolMainPanel.viewer.runScript("select none");
            jmolMainPanel.repaint();
        }
    }

    /**
     * Handle Halo Toggle Button. Turn On and Off golden rings around molecules
     */
    @FXML
    public void toggleHalo() {
        if (haloButton.isSelected()) {
            System.out.println("on");
            jmolMainPanel.viewer.runScript("selectionHalos on");
            jmolMainPanel.viewer.runScript(" set picking SELECT ATOM");
        } else {
            System.out.println("off");
            jmolMainPanel.viewer.runScript("selectionHalos off");
            jmolMainPanel.repaint();
        }
    }

    /**
     * Handle Snip Button. Turn on and off ability to fragment molecules by clicking on bonds
     */
    @FXML
    public void toggleSnip() {
        if (snipButton.isSelected()) {
            jmolMainPanel.viewer.runScript("set bondpicking true");
            jmolMainPanel.viewer.runScript("set picking deletebond");
        } else {
            jmolMainPanel.viewer.runScript("set bondpicking false");
        }
    }

    /**
     * Handle toggle measure button. Clicking on two seperate atoms measures distance
     */
    @FXML
    public void toggleMeasure() {
        if (measureButton.isSelected()) {
            jmolMainPanel.viewer.runScript("set picking MEASURE DISTANCE");
            jmolMainPanel.viewer.runScript("set pickingStyle MEASURE ON");
        } else {
            jmolMainPanel.viewer.runScript("set pickingStyle MEASURE OFF");
            jmolMainPanel.viewer.runScript(" set picking SELECT ATOM");
        }
    }

    /**
     * Handle Pick Center Button. Centers a atom on selection
     */
    @FXML
    public void handlePickCenter() {
        if (pickCenterButton.isSelected()) {
            jmolMainPanel.viewer.runScript("set picking CENTER");
        } else {
            jmolMainPanel.viewer.runScript(" set picking SELECT ATOM");
        }
    }

    @FXML
    public void showGeomAnalysis() throws IOException {
        FXMLLoader geoAnalysisLoader = new FXMLLoader(getClass().getResource("/views/analysisViews/GeometryAnalysisView.fxml"));
        Parent geoAnalysisParent = geoAnalysisLoader.load();
        GeometryAnalysisController geometryAnalysisController = geoAnalysisLoader.getController();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Geometry Analysis");
        stage.setScene(new Scene(geoAnalysisParent));
        stage.showAndWait();
//        Stage stage = new Stage();
//        stage.setTitle("Geometry Analysis");
//
//        ScrollPane geomScrollPane = new ScrollPane();
//
//        OutputFile of = new OutputFile("/Users/shaadhussain/Desktop/NewiSpiEFP/iSpiEFP_GUI/opt_1.out");
//        ArrayList<OutputFile.State> statesList = of.getStates();
//        currUnitLabelStr = "hartrees";
//        //boolean isDefaultUnit = true;
//        //Initial setup of chart
//        NumberAxis xAxis = new NumberAxis();
//        xAxis.setLabel("Geometry");
//        NumberAxis yAxis = new NumberAxis();
//        yAxis.setLabel("Energy " + "(" + currUnitLabelStr + ")");
//
//        LineChart geomVsEnergyChart = new LineChart(xAxis, yAxis);
//        geomVsEnergyChart.setTitle("Energy vs. Geometry");
//        geomVsEnergyChart.setLegendVisible(false);
//
//        XYChart.Series series = new XYChart.Series();
//        series.setName("OPT1 1 Values");
//
//        ListView<String> list = new ListView<String>();
////        ObservableList<String> items = FXCollections.observableArrayList (
////                "1. XXX", "2. XXX", "3. XXX", "4. XXX", "5. ...");
//        ObservableList<String> items = FXCollections.observableArrayList ();
//
//        maxYVal = statesList.get(0).getEnergyComponents().getTotalEnergy();
//        for (int i = 0; i < statesList.size(); i++) {
//            XYChart.Data<Number, Number> data1 = new XYChart.Data<Number, Number>(i, statesList.get(i).getEnergyComponents().getTotalEnergy());
//
////            Node node = data1.getNode();
////            node.focusTraversableProperty().unbind();
////            node.setFocusTraversable(true);
//
//            items.add(i + ": " + statesList.get(i).getEnergyComponents().getTotalEnergy());
//            if (statesList.get(i).getEnergyComponents().getTotalEnergy() > maxYVal) {
//                maxYVal = statesList.get(i).getEnergyComponents().getTotalEnergy();
//            }
//            series.getData().add(data1);
////            Node node = data1.getNode();
////            node.focusTraversableProperty().unbind();
////            node.setFocusTraversable(true);
//        }
//        maxXVal = statesList.size();
//       // maxYVal =
//        //End chart setup
//        list.setItems(items);
//        list.setMaxHeight(300);
//
//        GridPane geomGrid = new GridPane();
//        geomGrid.setPadding(new Insets(10, 10, 10, 10));
//
//        Button autosizeBtn = new Button("Autosize");
//
//        autosizeBtn.setPrefWidth(120);
//        autosizeBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                int xPow = getExponent(maxXVal);
//                int yPow = getExponent(maxYVal);
//
//                xAxis.setUpperBound(maxXVal + Math.pow(10, xPow));
//                yAxis.setUpperBound(maxYVal + Math.pow(10, yPow));
//
//            }
//        }));
//
//        String[] unit_types = {"hartrees", "kcals/mol", "kJ/mol", "cm-1"};
//
//        Label unitsSelectLabel = new Label("Select Units");
//
//        ComboBox unitsSelectCombBox = new ComboBox(FXCollections
//                .observableArrayList(unit_types));
//
//        HashMap<Integer, Integer> convertedUnitsMap = new HashMap<>();
//        EventHandler<ActionEvent> unitSelectedEvent =
//                new EventHandler<ActionEvent>() {
//                    public void handle(ActionEvent e)
//                    {
////                        int maxXUnit = 0;
////                        int maxYUnit = 0;
//                        yAxis.setLabel("Energy (" + unitsSelectCombBox.getValue() + ")");
//
////                        if (unitsSelectCombBox.getValue().equals("hartrees")) {
////                            //Do nothing
////                            for (Integer key : graphDataMap.keySet()) {
////                                convertedUnitsMap.put(key, graphDataMap.get(key));
////
////                                    if (key > maxXUnit) {
////                                        maxXUnit = key;
////                                    }
//////                                    if (convertedUnitsMap.get(key) > )
////                            }
////                        }
////
////                        if (unitsSelectCombBox.getValue().equals("kcal/mol")) {
////                            for (Integer key : graphDataMap.keySet()) {
////                                    convertedUnitsMap.put(key, graphDataMap.get(key) * 628);
////
//////                                    if (key > maxXUnit) {
//////                                        maxXUnit = key;
//////                                    }
//////                                    if (convertedUnitsMap.get(key) > )
////                                }
////                        }
////
////                        else if (unitsSelectCombBox.getValue().equals("kJ/mol")) {
////                            for (Integer key : graphDataMap.keySet()) {
////                                convertedUnitsMap.put(key, graphDataMap.get(key) * 2626);
////                            }
////                        }
////
////                        else if (unitsSelectCombBox.getValue().equals("cm-1"))  {
////                            for (Integer key : graphDataMap.keySet()) {
////                                convertedUnitsMap.put(key, graphDataMap.get(key) * 219475);
////                            }
////                        }
//
//                        series.getData().clear();
//                        for (Integer key : convertedUnitsMap.keySet()) {
//                            series.getData().add(new XYChart.Data<Number, Number>(key, convertedUnitsMap.get(key)));
//                        }
//                    }
//                };
//
//        unitsSelectCombBox.setOnAction(unitSelectedEvent);
//
//        unitsSelectCombBox.getSelectionModel().selectFirst();
//        Button leftArrowBtn = new Button();
//        leftArrowBtn.setStyle("-fx-shape: \"M 0 -3.5 v 7 l 4 -3.5 z\"");
//        leftArrowBtn.setRotate(180);
//
//        Button circularPlayButton = new Button();
//        circularPlayButton.setStyle("-fx-border-radius: 20;");
//        circularPlayButton.setPrefWidth(20);
//
//        Button rightArrowBtn = new Button();
//        rightArrowBtn.setStyle("-fx-shape: \"M 0 -3.5 v 7 l 4 -3.5 z\"");
//
//        ToggleButton chartInfoButton = new ToggleButton();
//
//        chartInfoButton.setText("");
//        chartInfoButton.setGraphic(new ImageView(chartUsageInfo));
//
//        chartInfoButton.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Info!");
//                alert.setHeaderText("Number Entry Error");
//                alert.setContentText("Info");
//                alert.showAndWait();
//            }
//        }));
//        HBox navBtnsHBox = new HBox(10);
//
//        geomScrollPane.setContent(geomVsEnergyChart);
////        maxYVal = 60;
////        maxXVal = 0.5;
//
//        upperXBound = maxXVal + 1;
//        upperYBound = maxYVal + 5;
//        xAxis.setAutoRanging(false);
//        xAxis.setLowerBound(0);
//        xAxis.setUpperBound(maxXVal + 1);
//        //xAxis.setTickUnit(1);
////NOTE: UNCOMMENT THIS^
//        geomVsEnergyChart.getData().add(series); //TODO:
//
////        Tooltip.install(data1.getNode(), new Tooltip("(" + data1.getXValue() + ", " + data1.getYValue() + ")"));
////        Tooltip.install(data2.getNode(), new Tooltip("(" + data2.getXValue() + ", " + data2.getYValue() + ")"));
////        Tooltip.install(data3.getNode(), new Tooltip("(" + data3.getXValue() + ", " + data3.getYValue() + ")"));
////        Tooltip.install(data4.getNode(), new Tooltip("(" + data4.getXValue() + ", " + data4.getYValue() + ")"));
//
//        Tooltip.install(xAxis, new Tooltip("Drag right to double scale, drag left to half scale"));
//        Tooltip.install(yAxis, new Tooltip("Drag up to double scale, drag down to half scale"));
//
//        xAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                xPressed = true;
//                lastXPosition = event.getSceneX();
//            }
//        }));
//
//        xAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                if (xPressed) {
//                    if (event.getSceneX() - lastXPosition >= 20.0) {
//                        xAxis.setAutoRanging(false);
//                        double tempMaxXVal = upperXBound * 2;
//                        xAxis.setUpperBound(tempMaxXVal);
//                        upperXBound *= 2;
//
//                        xPressed = false;
//                        System.out.println("Finished right drag logic");
//
//                    } else if (lastXPosition - event.getSceneX() >= 20.0) {
//                        xAxis.setAutoRanging(false);
//                        //xAxis.setTickUnit();
//                        xAxis.setUpperBound(Math.ceil(upperXBound / 2));
//                        upperXBound /= 2;
//                        xPressed = false;
//
//                    }
//                }
//            }
//        }));
//
//        yAxis.setOnMousePressed((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                lastYPosition = event.getSceneY();
//                yPressed = true;
//            }
//        }));
//
//        yAxis.setOnMouseDragged((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                //If the user drags right
//                if (yPressed) {
//                    if (lastYPosition - event.getSceneY() >= 20.0) {
//                        yAxis.setAutoRanging(false);
//                        yAxis.setUpperBound(upperYBound * 2);
//                        upperYBound *= 2;
//                        yPressed = false;
//                       // System.out.println("Finished up drag logic");
//                    }
//                    else if (event.getSceneY() - lastYPosition >= 20.0) {
//                        yAxis.setAutoRanging(false);
//                        yAxis.setUpperBound(upperYBound / 2);
//                        upperYBound /= 2;
//                        yPressed = false;
//                    }
//                }
//            }
//        }));
//
//        VBox axesEditVBox = new VBox(8);
//        HBox xHBox = new HBox(10);
//        HBox yHBox = new HBox(10);
//        HBox scaleBtnsHBox = new HBox(10);
//
//        Label xLabel = new Label("Set Custom X Bound");
//        TextField xAxeInput = new TextField();
//        xAxeInput.setPromptText("Current X axis Tick unit: " + xAxis.getTickUnit());
//        xHBox.getChildren().addAll(xLabel, xAxeInput);
//
//        Label yLabel = new Label("Set Custom Y Bound");
//        TextField yAxeInput = new TextField();
//        yAxeInput.setPromptText("Current Y axis Tick unit: " + yAxis.getTickUnit());
//        yHBox.getChildren().addAll(yLabel, yAxeInput);
//
//        Button scaleBtn = new Button("Scale");
//        scaleBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                xAxis.setAutoRanging(false);
//                yAxis.setAutoRanging(false);
//
//              //  try {
//
////                    if (parsedXInput < 0.0|| parsedYInput < 0.0) {
////                        showErrorDialog("Please make sure the scale values are greater than zero!");
////                    }
//
//                    /*
//                    Tasks 7/6/20
//
//                    - Offer to save at the out file initially
//                    - Format csv into columns, not rows
//                    - Give titles for each column
//                    - For prev and next buttons, you are plotting the "step number" on the x axis and total energy
//                    on the y axis
//                     */
//
//                    if (!xAxeInput.getText().isEmpty()) {
//                        Double parsedXInput = Double.parseDouble(xAxeInput.getText());
//                        xAxis.setUpperBound(parsedXInput);
//                    }
//                    if (!yAxeInput.getText().isEmpty()) {
//                        Double parsedYInput = Double.parseDouble(yAxeInput.getText());
//                        yAxis.setUpperBound(parsedYInput);
//                    }
//
//            }
//        }));
//
//        Button saveAsPNGBtn = new Button("Save as PNG");
//        saveAsPNGBtn.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//
//                WritableImage chartPNG = geomVsEnergyChart.snapshot(new SnapshotParameters(), null);
//                String home = System.getProperty("user.home");
//
//                // System.out.println("Home: " + home);
//                TextInputDialog pngDialog = new TextInputDialog("chart_snapshot");
//                pngDialog.setTitle("Name Your File");
//                pngDialog.setHeaderText("File Name");
//                pngDialog.setContentText("Please name your file (no extension) or use the default: ");
//
//                Optional<String> result = pngDialog.showAndWait();
//
//                File file = new File(home + "/Documents/" + result.get() + ".png");
//
//                try {
//                    ImageIO.write(SwingFXUtils.fromFXImage(chartPNG, null), "png", file);
//                }
//                catch (Exception e) {
//                    System.out.println("PNG Exception");
//                }
//
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setHeaderText("File Saved!");
//                alert.setContentText("Your file has been saved! It is located in the Documents folder at: " + file.getAbsolutePath());
//                alert.showAndWait();
//
//            }
//        }));
//
//        Button saveAsCSVButton = new Button("Save as CSV File");
//
//        List<String[]> dataLines = new ArrayList<>();
//
////        dataLines.add(new String[] {"1", "2", "3", "4"});
////        dataLines.add(new String[] {"60", "40", "25", "20"});
//
//        dataLines.add(new String[] {"Geometry Data"});
//        dataLines.add(new String[] {"X", "Y"});
//        dataLines.add(new String[] {"1", "60"});
//        dataLines.add(new String[] {"2", "40"});
//        dataLines.add(new String[] {"3", "25"});
//        dataLines.add(new String[] {"4", "20"});
//
//        saveAsCSVButton.setOnMouseClicked((new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                try {
//
//                    TextInputDialog csvDialog = new TextInputDialog("geom_data");
//                    csvDialog.setTitle("Name Your File");
//                    csvDialog.setHeaderText("File Name");
//                    csvDialog.setContentText("Please name your file (no extension) or use the default: ");
//
//                    Optional<String> result1 = csvDialog.showAndWait();
//
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                    alert.setTitle("Output Directory");
//                    alert.setHeaderText("Choose a directory");
//                    alert.setContentText("Would you like to ");
//
//                    ButtonType buttonTypeOne = new ButtonType("Choose Directory");
//                    ButtonType buttonTypeTwo = new ButtonType("Use Documents as Default");
//                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
//
//                    alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo,  buttonTypeCancel);
//
//                    Optional<ButtonType> result = alert.showAndWait();
//                    File selectedDirectory;
//                    String csvPath = "Empty";
//                    if (result.get() == buttonTypeOne){
//                        // ... user chose "One"
//                        DirectoryChooser directoryChooser = new DirectoryChooser();
//                        String h1 = System.getProperty("user.home");
//                        directoryChooser.setInitialDirectory(new File(h1));
//                        selectedDirectory = directoryChooser.showDialog(stage);
//                        csvPath = selectedDirectory.getAbsolutePath();
//
//                    } else if (result.get() == buttonTypeTwo) {
//                        // ... user chose "Two"
//                        String home = System.getProperty("user.home");
//                        csvPath = home + "/Documents/";
//
//                    }  else {
//                        // ... user chose CANCEL or closed the dialog
//                    }
//
//                    givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines, csvPath, result1.get());
//                }
//                catch (Exception e) {
//                    System.out.println("CSV filechooser fail Exception");
//                    e.printStackTrace();
//                }
//            }
//            }));
//
//        scaleBtnsHBox.getChildren().addAll(scaleBtn, autosizeBtn);
//        HBox unitsHBox = new HBox(10);
//        unitsHBox.getChildren().addAll(unitsSelectLabel, unitsSelectCombBox);
//        axesEditVBox.getChildren().addAll(xHBox, yHBox, scaleBtnsHBox, unitsHBox);
//
//        navBtnsHBox.getChildren().addAll(axesEditVBox, leftArrowBtn, circularPlayButton, rightArrowBtn, chartInfoButton, saveAsPNGBtn, saveAsCSVButton);
//
//        GridPane.setConstraints(list, 0, 0);
//        GridPane.setConstraints(geomVsEnergyChart, 1, 0);
//        GridPane.setConstraints(navBtnsHBox, 1, 2);
//        //GridPane.setConstraints(axesEditVBox, 2, 4);
//
//       // geomGrid.getChildren().addAll(list, geomScrollPane, navBtnsHBox);
//       geomGrid.getChildren().addAll(list, geomVsEnergyChart, navBtnsHBox);
//        gridScene = new Scene(geomGrid, 1000, 1000);
//        stage.setScene(gridScene);
//        stage.show();
//
    }
    public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines, String csvPath, String fileName) throws IOException {
        File csvOutputFile = new File(csvPath + "/" + fileName + ".csv");
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) { dataLines.stream().map(this::convertToCSV).forEach(pw::println);
    }
    assertTrue(csvOutputFile.exists());
    }



    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public int getExponent(double bound) {
        int pow = 0;
        int boundInt = (int) bound;

        boundInt /= 10;
        while (boundInt != 0) {
            pow++;
            boundInt /= 10;
        }
        return pow;
    }

    public void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("Number Entry Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void showEnergyAnalysis() {

        Stage stage = new Stage();
        stage.setTitle("Energy Analysis");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Total");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Energy (kcal/mol)");
        yAxis.setTickUnit(100);

        BarChart chart = new BarChart(xAxis, yAxis);

        XYChart.Series dataSeries1 = new XYChart.Series();

       dataSeries1.setName("Dummy Vals");

        double electrostatVal = -30.0;
        double exchRepulsVal = 40.0;
        double polarVal = -12.5;
        double dispersVal = -20.0;
        double totalVal = electrostatVal + exchRepulsVal + polarVal + dispersVal;

        VBox vBox = new VBox(chart);
        Scene scene = new Scene(vBox, 800, 800);

        scene.getStylesheets().add("bar_styles.css");


        dataSeries1.getData().add(new XYChart.Data("Electrostatic", electrostatVal));
        dataSeries1.getData().add(new XYChart.Data("Exchange-Repulsion", exchRepulsVal));
        dataSeries1.getData().add(new XYChart.Data("Polarization", polarVal));
        dataSeries1.getData().add(new XYChart.Data("Dispersion", dispersVal));
        dataSeries1.getData().add(new XYChart.Data("Total", totalVal));

        chart.getData().add(dataSeries1);

       // System.out.println("Chart Width: " + chart.getWidth());        //chart.setMaxWidth(50);

        chart.setMaxWidth(500);
        stage.setScene(scene);
        stage.setHeight(450);
        stage.setWidth(500);
        stage.show();
    }
    /**
     * Handle Play Pause. Capture Molecule
     */
    @FXML
    public void togglePlay() {
        playPauseButton.setText("");
        playPauseButton.setSelected(false);
        playPauseButton.setGraphic(new ImageView(play));
        if (playPauseButton.isSelected()) {
            playPauseButton.setGraphic(new ImageView(pause));
            jmolMainPanel.viewer.runScript("frame play");
        } else {
            playPauseButton.setGraphic(new ImageView(play));
            jmolMainPanel.viewer.runScript("animation off");
        }
    }

    /**
     * Handle display console button. Display the terminal for scripting with jmol viewer
     */
    @FXML
    public void displayConsole() {
        //create window for console
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame consoleFrame = new JFrame();
        consoleFrame.setSize(800, 400);
        consoleFrame.setLocation(
                (screenSize.width - 500) / 2,
                (screenSize.height) / 2);
        consoleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //create and connect panel with jmol console
        JPanel console_panel = new JPanel();
        console_panel.setLayout(new BorderLayout());
        AppConsole console = new AppConsole(jmolMainPanel.viewer, console_panel,
                "Editor Font Variables History State Clear Help");

        // Callback any scripts run in console to jmol viewer in main
        jmolMainPanel.viewer.setJmolCallbackListener(console);

        //show console
        consoleFrame.getContentPane().add(console_panel);
        consoleFrame.setVisible(true);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleFrame.toFront();
                consoleFrame.repaint();
            }
        });
    }

    /**
     * Handle the model kit button for making custom molecules
     */
    @FXML
    public void toggleModelKit() {
        if (modelKitButton.isSelected()) {
            jmolMainPanel.viewer.runScript("set modelKitMode true");
            jmolMainPanel.repaint();
        } else {
            jmolMainPanel.viewer.runScript("set modelKitMode false");
            jmolMainPanel.repaint();
        }
    }

    /**
     * Handle libefp button. Invoke Libefp Box for submitting a libefp job with the molecule.
     */
    @FXML
    public void libefp() {
        System.out.println("libefp button");
        //TODO need to call libefp constructor
    }

    public void VisualizeLibEFPResultFile() {
        try {
            File outFile = new File("iSpiEFP/pbc_1.out");
            File tempOutFile = new File("testTemp.xyz");

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempOutFile));
            BufferedReader bufferedReader = new BufferedReader(new FileReader(outFile));

            boolean startOfGeometry = false;
            boolean moleculeRead = false;
            int count = 0;
            String finalOut = "";
            int maxStep = 0;

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) break;
                else if (line.contains("max_steps")) maxStep = Integer.parseInt(line.split(" ")[1]);
                else if (line.contains("FINAL STATE")) startOfGeometry = true;
                else if (line.contains("STATE AFTER " + maxStep + " STEPS")) startOfGeometry = true;
                else if (line.contains("SINGLE POINT ENERGY JOB")) startOfGeometry = true;
                else if (startOfGeometry) {
                    line = line.replaceAll(" +", " ");
                    String[] unprocessedLine = line.split(" ");
                    if (unprocessedLine.length != 4 || unprocessedLine[0].charAt(0) != 'A') {
                        if (moleculeRead) break;
                    } else {
                        moleculeRead = true;
                        for (String s : unprocessedLine) {
                            if (s.equals("")) continue;
                            else if (s.contains("A")) finalOut += s.substring(1).replaceAll("[0-9]", "");
                            else finalOut += s;
                            finalOut += " ";
                        }
                        finalOut += '\n';
                        count++;
                    }
                }
            }

            finalOut = count + "\n" + "comment\n" + finalOut.substring(0, finalOut.length() - 1);

            bufferedWriter.write(finalOut);
            bufferedWriter.close();
            bufferedReader.close();

            jmolMainPanel.removeAll();
            jmolMainPanel.openFile(tempOutFile);

            tempOutFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /******************************************************************************************
     *             ICON BUTTON HANDLER SECTION ENDS                                           *
     ******************************************************************************************/
}
