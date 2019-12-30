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
    private Label parameterPathFieldLabel;
    @FXML
    private TextField parameterPathField;
    @FXML
    private DirectoryChooser ParameterDirectoryChooser;
    @FXML
    private Button directorySelectButton;
    @FXML
    private Label pythonPathFieldLabel;
    @FXML
    private TextField pythonPathField;
    @FXML
    private Button pythonSelectButton;
    @FXML
    private Button pathsSave;
    @FXML
    private Button pathsDefault;

    /* Fields for GAMESS Server Settings */
    @FXML
    private VBox gamessBox;


    /* Fields for LibEFP Server Settings */
    @FXML
    private VBox libEFPBox;



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
        if (menuTree == null) System.out.println("yeh");
        menuTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                initializePaths();
                initializeGamess();
                initializeLibEFP();
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
        parameterPathField.setPromptText(Preferences);
    }

    private void openPathsSettings(){
        currentVbox.setVisible(false);
        pathsBox.setVisible(true);
        currentVbox = pathsBox;
    }

    private void initializeGamess(){

    }

    private void openGamessSettings(){
        currentVbox.setVisible(false);
        gamessBox.setVisible(true);
        currentVbox = gamessBox;
    }

    private void initializeLibEFP(){

    }

    private void openLibEFPSettings(){
        currentVbox.setVisible(false);
        libEFPBox.setVisible(true);
        currentVbox = libEFPBox;
    }

}
