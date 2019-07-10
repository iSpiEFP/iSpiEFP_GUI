package org.vmol.app;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.vmol.app.database.DatabaseController;
import org.vmol.app.fileparser.FileParserController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.loginPack.LoginForm;
import org.vmol.app.submission.SubmissionHistoryController;
import org.vmol.app.util.UnrecognizedAtomException;
import org.vmol.app.visualizer.JmolMainPanel;
import org.vmol.app.visualizer.JmolVisualizer;

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
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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
    private Viewer viewer;                  //Jmol Viewer Object, a member of JmolMainPanel, can be invoked by jmolMainPanel.viewer as well
    
    @FXML
    private Parent root;
    
    /******************************************************************************************
     *             PANES & LISTS SECTION BEGINS                                               *
     ******************************************************************************************/
    @FXML
    private SplitPane leftRightSplitPane;
    
    @FXML
    private ListView leftListView;
    
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
        
        jmolMainPanel = new JmolMainPanel(middlePane);
        this.viewer = jmolMainPanel.viewer;
        
        middleRightSplitPane.setDividerPositions(1, 0);
        leftRightSplitPane.setDividerPositions(0.2f, 0.3f);

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

    @FXML
    /**
     * parse a pdb or xyz file and load the main jmolPanel
     * @throws IOException
     * @throws UnrecognizedAtomException
     */
    public void openFile() throws IOException, UnrecognizedAtomException {
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
        
        jmolMainPanel = new JmolMainPanel(middlePane);
        this.viewer = jmolMainPanel.viewer;
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
        }
        /*
        if (file != null) {
            // Check if it's an xyz or pdb file
            lastOpenedFile = file.getAbsolutePath();
            lastOpenedFileName = file.getName();
            String fileName = file.getName();
            System.out.println(fileName);
            boolean isXyzorPDB = fileName.contains("xyz") || fileName.contains("pdb");
            if (isXyzorPDB) {
                // TODO: validate an xyz file if it is in correct format
				/*
			    Dialog dialog = new Dialog<>();
				dialog.setTitle("Parameter Choices");
				dialog.setHeaderText("Please pick the parameters for your system:");
				ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
				dialog.getDialogPane().getButtonTypes().addAll(ok);
				GridPane grid = new GridPane();
				grid.setHgap(10);
				grid.setVgap(10);
				grid.setPadding(new Insets(20, 150, 10, 10));
				CheckBox pol = new CheckBox();
				CheckBox disp = new CheckBox();
				CheckBox exr = new CheckBox();
				grid.add(new Label("Polarization"),0,0);
				grid.add(new Label("Dispersion"), 0, 1);
				grid.add(new Label("Exchange-Repulsion"), 0, 2);
				grid.add(pol, 1, 0);
				grid.add(disp, 1, 1);
				grid.add(exr, 1, 2);
				dialog.getDialogPane().setContent(grid);
				dialog.showAndWait();
				interested_parameters[0] = pol.isSelected();
				interested_parameters[1] = disp.isSelected();
				interested_parameters[2] = exr.isSelected();
				*/
          /*      boolean automaticFragmentation = false;

                //file is valid, sending to visualizer
                JmolVisualizer jmolVisualizer = new JmolVisualizer(Main.getJmolViewer(), automaticFragmentation);
                jmolVisualizer.show(file);
            } else {
                openFileParserWindow(file);
            }
        }*/
    }

    @FXML
    /**
     *  open a file with automatic fragmentation capabilities
     */
    public void autoFragOpenFile() throws IOException, UnrecognizedAtomException {
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
        if (file != null) {
            // Check if it's an xyz or pdb file
            lastOpenedFile = file.getAbsolutePath();
            lastOpenedFileName = file.getName();
            String fileName = file.getName();
            System.out.println(fileName);
            boolean isXyzorPDB = fileName.contains("xyz") || fileName.contains("pdb");
            if (isXyzorPDB) {
                // TODO: validate an xyz file if it is in correct format

                boolean automaticFragmentation = true;
                //file is valid, sending to visualizer
                JmolVisualizer jmolVisualizer = new JmolVisualizer(Main.getJmolViewer(), automaticFragmentation);
                jmolVisualizer.show(file);
            } else {
                openFileParserWindow(file);
            }
        }
    }

    /**
     * Open the file parser window if a file fails
     *
     * @param file
     * @throws IOException
     */
    private void openFileParserWindow(File file) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fileparser/FileParser.fxml"));

        Parent fileParser = loader.load();

        Stage stage = new Stage();
        stage.setTitle(file.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(root.getScene().getWindow());
        stage.setScene(new Scene(fileParser));

        // Set the file into the controller
        FileParserController controller = loader.getController();
        controller.setFile(file);

        stage.showAndWait();

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
     *             ICON BUTTON HANDLER SECTION BEGINS                                         *
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
            viewer.clearSelection();
            viewer.runScript("selectionHalos on");

        } else {
            System.out.println("off");
            viewer.runScript("selectionHalos off");
            viewer.runScript("select; halos off");
            viewer.clearSelection();
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
            viewer.runScript("set bondpicking true");
            viewer.runScript("set picking deletebond");
        } else {
            viewer.runScript("set bondpicking false");
        }
    }
    
    /**
     * Handle Undo Button. Reverse a snipping action on the molecule
     */
    @FXML
    public void undo() {
        //TODO
        //call JmolMainPanel.undo()
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
            viewer.runScript("frame play");
        } else {
            playPauseButton.setGraphic(new ImageView(play));
            viewer.runScript("animation off");
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
            AppConsole console = new AppConsole(viewer, console_panel,
                    "History State Clear");
    
            // Callback any scripts run in console to jmol viewer in main
            viewer.setJmolCallbackListener(console);
    
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
            //TODO, invoke databse controller
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
