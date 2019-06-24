package org.vmol.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.vmol.app.fileparser.FileParserController;
import org.vmol.app.gamessSubmission.gamessSubmissionHistoryController;
import org.vmol.app.loginPack.LoginForm;
import org.vmol.app.submission.SubmissionHistoryController;
import org.vmol.app.util.UnrecognizedAtomException;
import org.vmol.app.visualizer.JmolVisualizer;

import java.io.File;
import java.io.IOException;

public class MainViewController {

    private static String lastOpenedFile;
    private static String lastOpenedFileName;
    private static boolean[] interested_parameters = {false, false, false};
    @FXML
    private Parent root;

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
                boolean automaticFragmentation = false;

                //file is valid, sending to visualizer
                JmolVisualizer jmolVisualizer = new JmolVisualizer(Main.getJmolViewer(), automaticFragmentation);
                jmolVisualizer.show(file);
            } else {
                openFileParserWindow(file);
            }
        }
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
        Parent qChemInput = FXMLLoader.load(getClass().getResource("qchem/QChemInput.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Libefp Input");
        stage.setScene(new Scene(qChemInput));
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
}
