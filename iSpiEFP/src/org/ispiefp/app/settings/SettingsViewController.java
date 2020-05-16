package org.ispiefp.app.settings;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ispiefp.app.Initializer;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.util.UserPreferences;

import java.io.File;
import java.util.prefs.Preferences;

public class SettingsViewController {
    /* Overarching Class Fields */
    private VBox currentVbox;

    @FXML
    private AnchorPane anchor;

    /* Fields for Path Settings */
    @FXML
    private VBox pathsBox;
    @FXML
    private TextField parameterPathField;
    @FXML
    private TextField pythonPathField;

    /* Fields for GAMESS Server Settings */
    @FXML
    private VBox gamessBox;
    @FXML
    private TextField gamessServerField;
    @FXML
    private TextField gamessUserName;
    @FXML
    private PasswordField gamessPassword;
    @FXML
    private TextField gamessOutputPath;

    /* Fields for LibEFP Server Settings */
    @FXML
    private VBox libEFPBox;
    @FXML
    private TextField libEFPServerField;
    @FXML
    private TextField libEFPUserName;
    @FXML
    private PasswordField libEFPPassword;
    @FXML
    private TextField libEFPOutputPath;

    /*Fields for persistent scene */
    @FXML
    private VBox settingsBox;
        @FXML
    private TreeView<String> menuTree;
    @FXML
    private TreeItem topLeveLSettings;
    @FXML
    private TreeItem<String> defaultPaths;
    @FXML
    private TreeItem<String> serverSettings;
    @FXML
    private TreeItem<String> gamessServer;
    @FXML
    private TreeItem<String> libEFPServer;

    public void initialize() {
        initializePaths();
        initializeGamess();
        initializeLibEFP();
        if (menuTree == null) System.out.println("yeh");
        menuTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                switch (newValue.getValue()) {
                    case "Default Paths":
                        openPathsSettings();
                        break;
                    case "GAMESS Server Settings":
                        openGamessSettings();
                        break;
                    case "LibEFP Server Settings":
                        openLibEFPSettings();
                        break;
                }
            }
        });
        currentVbox = pathsBox;
        pathsBox.setVisible(true);
    }

    private void initializePaths(){
        parameterPathField.setPromptText(UserPreferences.getUserParameterPath());
        pythonPathField.setPromptText(UserPreferences.getPythonPath());
    }

    @FXML
    private void selectParameterDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select the Directory Containing your EFP files");
        dc.setInitialDirectory(new File(UserPreferences.getUserParameterPath()));
        Stage currStage = (Stage) anchor.getScene().getWindow();
        try {
            parameterPathField.setText(dc.showDialog(currStage).getAbsolutePath());
        } catch (NullPointerException e){
            System.out.println("User closed dialog without selecting a file");;
        }
    }

    @FXML
    private void selectPythonPath() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select your Python Interpreter");
        if (UserPreferences.pythonPathExists()) {
            File python = new File(UserPreferences.getPythonPath());
            fc.setInitialFileName(python.getName());
            fc.setInitialDirectory(new File(python.getParent()));
        } else {
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        Stage currStage = (Stage) anchor.getScene().getWindow();
        try{
            pythonPathField.setText(fc.showOpenDialog(currStage).getAbsolutePath());
        } catch(NullPointerException e){
            System.out.println("User closed dialog without selecting a file");;
        }
    }

    @FXML
    private void pathsSave(){
        if (!pythonPathField.getText().equals("")){
            UserPreferences.setPythonPath(pythonPathField.getText());
        }
        if (!parameterPathField.getText().equals("")){
            UserPreferences.setUserParameterPath(parameterPathField.getText());
            Initializer init = new Initializer();
            init.generateMetas(UserPreferences.getUserParameterPath());
            init.addMetasToTree();
        }
    }

    @FXML
    private void pathsRestoreDefaults(){
        UserPreferences.setUserParameterPath(LocalBundleManager.USER_PARAMETERS);
        Initializer init = new Initializer();
        init.generateMetas(UserPreferences.getUserParameterPath());
        init.addMetasToTree();
        parameterPathField.setText(UserPreferences.getUserParameterPath());
        if (System.getenv("PYTHONPATH") != null) {
            UserPreferences.setPythonPath(System.getenv("PYTHONPATH"));
            pythonPathField.setText(System.getenv("PYTHONPATH"));
        } else {
            UserPreferences.setPythonPath("check");
            pythonPathField.clear();
            pythonPathField.setPromptText("Could not automatically find python interpreter");
        }
    }

    private void openPathsSettings(){
        currentVbox.setVisible(false);
        pathsBox.setVisible(true);
        currentVbox = pathsBox;
    }

    private void initializeGamess(){
        if (UserPreferences.getGamessServer().equals("check")) gamessServerField.setPromptText("Enter Server Address");
        else gamessServerField.setPromptText(UserPreferences.getGamessServer());
        if (UserPreferences.getGamessUsername().equals("check")) gamessUserName.setPromptText("Enter your username for the server");
        else gamessUserName.setPromptText(UserPreferences.getGamessUsername());
        if (UserPreferences.getGamessOutputPath().equals("check")) gamessOutputPath.setPromptText(UserPreferences.getUserParameterPath());
        else gamessOutputPath.setPromptText(UserPreferences.getGamessOutputPath());
    }

    @FXML
    private void selectGamessOutputDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Directory for the Output of GAMESS Calculations");
        dc.setInitialDirectory(new File(UserPreferences.getUserParameterPath()));
        Stage currStage = (Stage) anchor.getScene().getWindow();
        try {
            gamessOutputPath.setText(dc.showDialog(currStage).getAbsolutePath());
        } catch (NullPointerException e){
            System.out.println("User closed dialog without selecting a file");
        }
    }
    @FXML
    private void gamessSave(){
        if (!gamessServerField.getText().equals("")){
            UserPreferences.setGamessServer(gamessServerField.getText());
        }
        if (!gamessUserName.getText().equals("")){
            UserPreferences.setGamessUsername(gamessUserName.getText());
        }
        if (!gamessPassword.getText().equals("")){
            UserPreferences.setGamessPassword(gamessPassword.getText());
        }
        if (!gamessOutputPath.getText().equals("")){
            UserPreferences.setGamessOutputPath(gamessOutputPath.getText());
        }
    }

    @FXML
    private void gamessClear(){
        UserPreferences.setGamessServer("check");
        UserPreferences.setGamessUsername("check");
        UserPreferences.setGamessPassword("check");
        gamessServerField.setPromptText("Enter Server Address");
        gamessUserName.setPromptText("Enter your username for the server");
        gamessPassword.setPromptText("");
    }

    private void openGamessSettings(){
        currentVbox.setVisible(false);
        gamessBox.setVisible(true);
        currentVbox = gamessBox;
    }

    private void initializeLibEFP(){
        if (UserPreferences.getLibefpServer().equals("check")) libEFPServerField.setPromptText("Enter Server Address");
        else libEFPServerField.setPromptText(UserPreferences.getGamessServer());
        if (UserPreferences.getLibefpUsername().equals("check")) libEFPUserName.setPromptText("Enter your username for the server");
        else libEFPUserName.setPromptText(UserPreferences.getGamessUsername());
        if (UserPreferences.getLibefpOutputPath().equals("check")) {
            libEFPOutputPath.setPromptText(LocalBundleManager.LIBEFP);
        } else if (new File(UserPreferences.getLibefpOutputPath()).exists() &&
                new File(UserPreferences.getLibefpOutputPath()).isDirectory()) {
            libEFPOutputPath.setPromptText(UserPreferences.getLibefpOutputPath());
        } else{
            libEFPOutputPath.setPromptText(System.getProperty("user.home"));
        }
    }

    @FXML
    private void selectLibEFPOutputDirectory() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Directory for the Output of libEFP Calculations");
        if (UserPreferences.getLibefpOutputPath().equals("check")) {
            dc.setInitialDirectory(new File(LocalBundleManager.LIBEFP));
        } else if (new File(UserPreferences.getLibefpOutputPath()).exists() &&
                new File(UserPreferences.getLibefpOutputPath()).isDirectory()) {
            dc.setInitialDirectory(new File(UserPreferences.getLibefpOutputPath()));
        } else{
            dc.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        Stage currStage = (Stage) anchor.getScene().getWindow();
        try {
            libEFPOutputPath.setText(dc.showDialog(currStage).getAbsolutePath());
        } catch (NullPointerException e){
            System.out.println("User closed dialog without selecting a file");
        }
    }
    @FXML
    private void libEFPSave(){
        if (!libEFPServerField.getText().equals("")){
            UserPreferences.setLibefpServer(libEFPServerField.getText());
        }
        if (!libEFPUserName.getText().equals("")){
            UserPreferences.setLibefpUsername(libEFPUserName.getText());
        }
        if (!libEFPPassword.getText().equals("")){
            UserPreferences.setLibefpPassword(libEFPPassword.getText());
        }
        if (!libEFPOutputPath.getText().equals("")){
            UserPreferences.setLibefpOutputPath(libEFPOutputPath.getText());
        }
    }

    @FXML
    private void libEFPClear(){
        UserPreferences.setLibefpServer("check");
        UserPreferences.setLibefpUsername("check");
        UserPreferences.setLibefpPassword("check");
        libEFPServerField.setPromptText("Enter Server Address");
        libEFPUserName.setPromptText("Enter your username for the server");
        libEFPPassword.setPromptText("");
    }

    private void openLibEFPSettings(){
        currentVbox.setVisible(false);
        libEFPBox.setVisible(true);
        currentVbox = libEFPBox;
    }

}
