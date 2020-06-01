package org.ispiefp.app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.ispiefp.app.libEFP.libEFPInputController;
import org.ispiefp.app.metaDataSelector.MetaDataSelectorController;
import org.ispiefp.app.util.*;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.ispiefp.app.database.DatabaseController;
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
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import static org.ispiefp.app.util.UserPreferences.appendToRecentFilesStr;
import static org.ispiefp.app.util.UserPreferences.getRecentFileAggStr;

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
                new FileChooser.ExtensionFilter("All Files", "*.*"  ),
                new FileChooser.ExtensionFilter("XYZ", "*.xyz"),
                new FileChooser.ExtensionFilter("PDB", "*.pdb")
        );
        Stage currStage = (Stage) root.getScene().getWindow();

        File file = fileChooser.showOpenDialog(currStage);

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
        }

        else {
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
                    }
                    catch (Exception e) {
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

                    }
                    catch (Exception e) {
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

        }

        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            System.err.println("FRAGMENT MAIN VIEW ERROR");
        }
       // stage.showAndWait();    //TODO: Fixxxx. This causes errors when you do Cmnd+Tab
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
        Parent fragmentSelector = FXMLLoader.load(getClass().getResource("/views/SettingsView.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Settings");
        stage.setScene(new Scene(fragmentSelector));

        try {
            stage.showAndWait();
        }
        catch (Exception e) {
            System.err.println("SHOW AND WAIT ERROR IN MAIN");
        }
    }


    public void selectFragment() throws IOException{
        String noInternetWarning = "You are not currently connected to the internet.\n\n" +
                "You will only be able to select from " +
                "fragments whose parameters are contained within your user parameters directory.";
        if (!VerifyPython.isValidPython()){
            VerifyPython.raisePythonError();
            return;
        }
        if (!CheckInternetConnection.checkInternetConnection()){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    noInternetWarning,
                    ButtonType.OK);
            alert.showAndWait();
            fragmentOpen();
        }
        else fragmentOpen();
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
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            System.out.println("Stage is closing");
            Main.getPrimaryStage().close();
            System.exit(0);
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
        jmolMainPanel.viewer.runScript("select all");
    }

    @FXML
    public void editSelectNone() throws IOException {
        jmolMainPanel.viewer.runScript("select none");
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
        jmolMainPanel.viewer.runScript("moveto 0 0 0 0 0 100");
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
    @FXML
    /**
     * Handle Search Fragments button. Search the database for similar fragments to the current molecule
     */
    public void searchFindEFPPublicDatabase() throws IOException {
        if (!lastOpenedFile.isEmpty()) {
            //set divider positions
            middleRightSplitPane.setDividerPositions(0.6f, 0.4f);
            rightVerticalSplitPane.setDividerPositions(0.5f, 0.5f);

            //Runs auxiliary JmolViewer
            JmolPanel jmolPanel = new JmolPanel(upperRightPane);

            //load aux table list
            DatabaseController DBcontroller = new DatabaseController(bottomRightPane, jmolMainPanel, jmolPanel.viewer, jmolMainPanel.getFragmentComponents());
            try {
                //start database controller actions
                DBcontroller.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No file was opened");
        }
    }

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
    public void calculateLibefpHistory () throws IOException {
        LoginForm loginForm = new LoginForm("LIBEFP");
        boolean authorized = loginForm.authenticate();
        if (authorized) {
            SubmissionHistoryController controller = new SubmissionHistoryController(loginForm.getUsername(), loginForm.getPassword(), loginForm.getHostname());
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
            stage.setTitle("Submission History");
            stage.show();
        }
    }


    @FXML
    public void calculateGamessSetup () throws IOException {
        //TODO this should open the gamess setup page
        //This is currently disabled in the fxml doc since it is not currently operational

    }

    @FXML
    public void calculateGamessHistory () throws IOException {
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
    public void calculateEditServers () throws IOException {
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
    public void helpCheckForUpdates () throws IOException {
        //TODO
        //This is currently disabled in the fxml doc since it is not currently operational
    }

    @FXML
    public void helpAbout () throws IOException {
        Main.hostServices.showDocument("https://www.chem.purdue.edu/Slipchenko/");
    }

    @FXML
    public void helpJmolWiki () throws IOException {
        Main.hostServices.showDocument("http://jmol.sourceforge.net/");
    }

    @FXML
    public void helpJmolConsole () throws IOException {
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
    public void toggleSelection () {
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
    public void toggleHalo () {
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
    public void toggleSnip () {
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

    /******************************************************************************************
     *             ICON BUTTON HANDLER SECTION ENDS                                           *
     ******************************************************************************************/
}
