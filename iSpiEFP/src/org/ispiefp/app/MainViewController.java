package org.ispiefp.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.Jmol;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.ispiefp.app.database.DatabaseController;
import org.ispiefp.app.gamessSubmission.gamessSubmissionHistoryController;
import org.ispiefp.app.loginPack.LoginForm;
import org.ispiefp.app.submission.SubmissionHistoryController;
import org.ispiefp.app.util.UnrecognizedAtomException;
import org.ispiefp.app.visualizer.JmolMainPanel;
import org.ispiefp.app.visualizer.JmolPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class MainViewController {

    private static final Image halo = new Image(Main.class.getResource("/images/halo.png").toString());
    private static final Image scissors = new Image(Main.class.getResource("/images/scissors.png").toString());
    private static final Image play = new Image(Main.class.getResource("/images/play.png").toString());
    private static final Image pause = new Image(Main.class.getResource("/images/pause.png").toString());
    private static final Image terminal = new Image(Main.class.getResource("/images/terminal.png").toString());
    
    private static String lastOpenedFile = new String();
    private static String lastOpenedFileName = new String();
    private static boolean[] interested_parameters = {false, false, false};
    
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
    private ToggleButton haloButton;
        
    @FXML
    private ToggleButton snipButton;
    
    @FXML
    private Button undoButton;
    
    @FXML
    private ToggleButton playPauseButton;
    
    @FXML
    private Button consoleButton;
    
    @FXML
    private Button searchFragmentsButton;
    
    @FXML
    private Button libefpButton;
    
    /**
     * initialize(); is called after @FXML parameters have been loaded in
     * Loading order goes as: Constructor > @FXML > initialize();
     */
    @FXML
    public void initialize() {
        //set graphics
        haloButton.setText("");
        haloButton.setGraphic(new ImageView(halo));
        snipButton.setText("");
        snipButton.setGraphic(new ImageView(scissors));
        playPauseButton.setText("");
        playPauseButton.setGraphic(new ImageView(play));
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
        
        jmolMainPanel = new JmolMainPanel(middlePane, leftListView);
        if(jmolMainPanel.openFile(file)) {
            
            lastOpenedFile = file.getAbsolutePath();
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
        }
    }

    /**
     * TODO: fileOpenRecent this button does not exist in the fxml doc and needs to be added
     * @throws IOException
     *  This is currently disabled in the fxml doc since it is not currently operational
     */


    @FXML
    public void fileExit() throws IOException {
        System.out.println("Stage is closing");
        Main.getPrimaryStage().close();
        System.exit(0);
        //TODO: add pop-up message, "Are you sure?"
    }

    /******************************************************************************************
     *             EDITS MENU BEGINS                                                          *
     ******************************************************************************************/
    @FXML
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
        }
    }
    /******************************************************************************************
     *             CALCULATE MENU BEGINS                                                      *
     ******************************************************************************************/
    @FXML
    public void calculateLibefpSetup() throws IOException {
        //TODO
    }

    @FXML
    public void calculateLibefpHistory() throws IOException {
        //TODO
    }

    @FXML
    public void calculateGamessSetup() throws IOException {
        //TODO
    }

    @FXML
    public void calculateGamessHistory() throws IOException {
        //TODO
    }

    @FXML
    public void calculateEditServers() throws IOException {
        //TODO
    }

    /******************************************************************************************
     *             HELP MENU BEGINS                                                           *
     ******************************************************************************************/
    @FXML
    public void helpCheckForUpdates() throws IOException {
        //TODO
        //This is currently disabled in the fxml doc since it is not currently operational
    }

    @FXML
    public void helpAbout() throws IOException {
        //TODO
    }

    @FXML
    public void helpAboutJmol() throws IOException {
        //TODO
    }
    @FXML
    public void helpJmolWiki() throws IOException {
        //TODO
    }
    @FXML
    public void helpJmolConsole() throws IOException {
        //TODO
    }

    @FXML
    public void openLibEFPWindow() throws IOException {
        Parent libEFPInput = FXMLLoader.load(getClass().getResource("libEFP/libEFP.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Libefp Input");
        stage.setScene(new Scene(libEFPInput));
        stage.show();
    }

    @FXML
    public void openServersListWindow() throws IOException {
        Parent serversList = FXMLLoader.load(getClass().getResource("server/ServersList.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Servers list");
        stage.setScene(new Scene(serversList));
        stage.show();
    }

	
    @FXML
    public void openAbout() throws IOException{    //Help -> About dialog
        Alert a1 = new Alert(Alert.AlertType.CONFIRMATION,"hello");
        a1.setTitle("About iSpiEFP");
        a1.setHeaderText("Look, an Information Dialog");
        a1.setContentText("I have a great message for you!");
        a1.showAndWait();
    }
	
    @FXML
    public void openGamessWindow() throws IOException {
        Parent gamessInput = FXMLLoader.load(getClass().getResource("gamess/gamessInput.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Gamess Input");
        stage.setScene(new Scene(gamessInput));
        stage.show();
    }

    @FXML
    public void openSubmissionHistoryWindow() throws IOException {
        LoginForm loginForm = new LoginForm("LIBEFP");
        boolean authorized = loginForm.authenticate();
        if (authorized) {
            SubmissionHistoryController controller = new SubmissionHistoryController(loginForm.getUsername(), loginForm.getPassword(), loginForm.getHostname());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "submission/submissionHistory.fxml"
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
    public void openGamessSubmissionHistoryWindow() throws IOException {
        LoginForm loginForm = new LoginForm("GAMESS");
        boolean authorized = loginForm.authenticate();
        if (authorized) {
            gamessSubmissionHistoryController controller = new gamessSubmissionHistoryController(loginForm.getUsername(), loginForm.getPassword(), loginForm.getHostname());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "submission/submissionHistory.fxml"
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
    
    /******************************************************************************************
     *             ICON BUTTON HANDLER SECTION BEGINS
     *
     *             The following section is for the Button Pane below the Menu Bar
     *             containing all of the icon buttons. These buttons interact with the
     *             Jmol Viewer object, and provide some default tools found in Jmol, as
     *             well as some custom buttons.
     ******************************************************************************************/
    /**
     * Handle Halo Toggle Button. Turn On and Off golden rings around molecules
     */
    @FXML
    public void toggleHalo() {
        if(lastOpenedFile.isEmpty()) {
            haloButton.setSelected(false);
        } else if (haloButton.isSelected()) {
            System.out.println("on");
            jmolMainPanel.viewer.clearSelection();
            jmolMainPanel.viewer.runScript("selectionHalos on");

        } else {
            System.out.println("off");
            jmolMainPanel.viewer.runScript("selectionHalos off");
            jmolMainPanel.viewer.runScript("select; halos off");
            jmolMainPanel.viewer.clearSelection();
            jmolMainPanel.repaint();
        }
    }
    
    /**
     * Handle Snip Button. Turn on and off ability to fragment molecules by clicking on bonds
     */
    @FXML
    public void toggleSnip() {
        if(lastOpenedFile.isEmpty()) {
            snipButton.setSelected(false);
        } else if (snipButton.isSelected()) {
            jmolMainPanel.viewer.runScript("set bondpicking true");
            jmolMainPanel.viewer.runScript("set picking deletebond");
        } else {
            jmolMainPanel.viewer.runScript("set bondpicking false");
        }
    }
    
    /**
     * Handle Undo Button. Reverse a snipping action on the molecule
     */
    @FXML
    public void undo() {
        //TODO
        jmolMainPanel.undoDeleteBond();
    }
    
    /**
     * Handle Play Pause. Capture Molecule
     */
    @FXML
    public void togglePlay() {
        playPauseButton.setText("");
        
        if(lastOpenedFile.isEmpty()) {
            playPauseButton.setSelected(false);
            playPauseButton.setGraphic(new ImageView(play));
        } else if (!lastOpenedFile.isEmpty() && playPauseButton.isSelected()) {
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
        if (!lastOpenedFile.isEmpty()) {
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
                    "History State Clear");
    
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
    }
    
    /**
     * Handle Search Fragments button. Search the database for similar fragments to the current molecule
     */
    @FXML
    public void searchFragments() {
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
