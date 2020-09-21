package org.ispiefp.app.settings;

import ch.ethz.ssh2.Connection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ispiefp.app.Initializer;
import org.ispiefp.app.installer.BundleManager;
import org.ispiefp.app.installer.LocalBundleManager;
import org.ispiefp.app.server.ServerInfo;
import org.ispiefp.app.util.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SettingsViewController {

    public HBox locationHBox;

    public ComboBox<String> signInMethodComboBox;
    public Label passwordLabel;
    public CheckBox sshFileEncrypted;
    public Label sshFileEncryptedLabel;
    public Button fileChooser;
    private PasswordField signInPasswordField;
    private TextField signInFileLocationField;

    private String tempPassword;
    private String tempSshFileLocation;

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


    /* Fields for Server Settings */
    @FXML
    private GridPane serversBox;
    @FXML
    private TextField alias;
    @FXML
    private TextField hostname;
    @FXML
    private TextField username;
    @FXML
    private TextField libEFPInstallationPath;
    @FXML
    private TextField GAMESSInstallationPath;
    @FXML
    private CheckBox hasLibEFPButton;
    @FXML
    private CheckBox hasGAMESSButton;
    @FXML
    private ChoiceBox scheduler;
    @FXML
    private TextField addQueueField;
    @FXML
    private ChoiceBox defaultQueue;

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
    private TreeItem<String> addNew;
    @FXML
    private TreeItem<String> servers;

    public void initialize() {

        initializePaths();
        initializeServers();
        initializeSignInMethod();
        scheduler.getItems().addAll("PBS", "SLURM", "TORQUE");
        menuTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                switch (newValue.getValue()) {
                    case "Default Paths":
                        openPathsSettings();
                        break;
                    case "Add New Server":
                        addNewServer();
                        break;
                    default:
                        if (newValue.getValue() == null) break;
                        if (UserPreferences.getServers().containsKey(newValue.getValue())) {
                            loadServer(UserPreferences.getServers().get(newValue.getValue()));
                        }
                }
            }
        });
        currentVbox = pathsBox;
        pathsBox.setVisible(true);
    }

    private void initializeSignInMethod() {
        signInPasswordField = new PasswordField();
        signInPasswordField.setMaxWidth(Double.MAX_VALUE);
        signInPasswordField.setMinHeight(Double.NEGATIVE_INFINITY);
        signInPasswordField.setMinWidth(Double.NEGATIVE_INFINITY);
        GridPane.setConstraints(signInPasswordField, 1, 8);

        signInFileLocationField = new TextField();
        signInFileLocationField.setMaxWidth(Double.MAX_VALUE);
        signInFileLocationField.setMinHeight(Double.NEGATIVE_INFINITY);
        signInFileLocationField.setMinWidth(Double.NEGATIVE_INFINITY);
        HBox.setHgrow(signInFileLocationField, Priority.ALWAYS);

        fileChooser = new Button();
        fileChooser.setText("...");
        fileChooser.setPrefWidth(20);
        fileChooser.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            File file;
            try {
                chooser.setInitialDirectory(new File(System.getProperty("user.home") + "/.ssh"));
                file = chooser.showOpenDialog(fileChooser.getScene().getWindow());
            } catch (Exception e) {
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                file = chooser.showOpenDialog(fileChooser.getScene().getWindow());
            }
            signInFileLocationField.setText(file.getAbsolutePath());
        });

        locationHBox = new HBox(signInFileLocationField, fileChooser);
        locationHBox.setMaxWidth(Double.MAX_VALUE);
        locationHBox.setSpacing(10);
        GridPane.setConstraints(locationHBox, 1, 8);

        serversBox.getChildren().addAll(locationHBox, signInPasswordField);

        signInMethodComboBox.getItems().addAll("SSH Key", "Password");
        signInMethodComboBox.getSelectionModel().select(0);
        SignInMethodChanged();
    }

    private void initializePaths() {
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
        } catch (NullPointerException e) {
            System.out.println("User closed dialog without selecting a file");
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
        try {
            pythonPathField.setText(fc.showOpenDialog(currStage).getAbsolutePath());
        } catch (NullPointerException e) {
            System.out.println("User closed dialog without selecting a file");
            ;
        }
    }

    @FXML
    private void pathsSave() {
        if (!pythonPathField.getText().equals("")) {
            UserPreferences.setPythonPath(pythonPathField.getText());
        }
        if (!parameterPathField.getText().equals("")) {
            UserPreferences.setUserParameterPath(parameterPathField.getText());
            Initializer init = new Initializer();
            init.generateMetas(UserPreferences.getUserParameterPath());
            init.addMetasToTree();
        }
    }

    @FXML
    private void pathsRestoreDefaults() {
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

    private void openPathsSettings() {
        pathsBox.setVisible(true);
        serversBox.setVisible(false);
    }


//    @FXML
//    private void selectGamessOutputDirectory() {
//        DirectoryChooser dc = new DirectoryChooser();
//        dc.setTitle("Directory for the Output of GAMESS Calculations");
//        dc.setInitialDirectory(new File(UserPreferences.getUserParameterPath()));
//        Stage currStage = (Stage) anchor.getScene().getWindow();
//        try {
//            gamessOutputPath.setText(dc.showDialog(currStage).getAbsolutePath());
//        } catch (NullPointerException e){
//            System.out.println("User closed dialog without selecting a file");
//        }
//    }


//    @FXML
//    private void selectLibEFPOutputDirectory() {
//        DirectoryChooser dc = new DirectoryChooser();
//        dc.setTitle("Directory for the Output of libEFP Calculations");
//        if (UserPreferences.getLibefpOutputPath().equals("check")) {
//            dc.setInitialDirectory(new File(LocalBundleManager.LIBEFP));
//        } else if (new File(UserPreferences.getLibefpOutputPath()).exists() &&
//                new File(UserPreferences.getLibefpOutputPath()).isDirectory()) {
//            dc.setInitialDirectory(new File(UserPreferences.getLibefpOutputPath()));
//        } else {
//            dc.setInitialDirectory(new File(System.getProperty("user.home")));
//        }
//        Stage currStage = (Stage) anchor.getScene().getWindow();
//        try {
//            libEFPOutputPath.setText(dc.showDialog(currStage).getAbsolutePath());
//        } catch (NullPointerException e) {
//            System.out.println("User closed dialog without selecting a file");
//        }
//    }


    /* Begin methods for server VBox */

    private void initializeServers() {
        servers.getChildren().clear();
//        System.out.println(UserPreferences.getServers().keySet());
        for (String serverName : UserPreferences.getServers().keySet()) {
            servers.getChildren().add(new TreeItem<>(serverName));
        }
        servers.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                openServerSettings();
                loadServer(UserPreferences.getServers().get(newValue));
            }
        }));
    }

    private void openServerSettings() {
        serversBox.setVisible(true);
        pathsBox.setVisible(false);
    }

    private void loadServer(ServerInfo si) {
        openServerSettings();
        alias.setText(si.getEntryname());
        hostname.setText(si.getHostname());
        username.setText(si.getUsername());
        if (si.isSshKeyMethod()) {
            signInMethodComboBox.getSelectionModel().select(0);
            SignInMethodChanged();
            signInFileLocationField.setText(si.getSshKeyLocation());
            sshFileEncrypted.setSelected(si.isSshFileEncrypted());
        } else {
            signInMethodComboBox.getSelectionModel().select(1);
            SignInMethodChanged();
            signInPasswordField.setText(si.getPassword());
        }
        scheduler.setValue(si.getScheduler());
        defaultQueue.getItems().setAll(si.getQueues() == null ? new String[]{} : si.getQueues());
        if (si.hasGAMESS()) {
            hasGAMESSButton.setSelected(true);
            GAMESSInstallationPath.setText(si.getGamessPath());
        } else {
            hasGAMESSButton.setSelected(false);
            GAMESSInstallationPath.setDisable(true);
        }
        if (si.hasLibEFP()) {
            hasLibEFPButton.setSelected(true);
            libEFPInstallationPath.setText(si.getLibEFPPath());
        } else {
            hasLibEFPButton.setSelected(false);
            libEFPInstallationPath.setDisable(true);
        }
    }

    @FXML
    private void addNewQueue() {
        try {
            ServerInfo si = UserPreferences.getServers().get(alias.getText());
            si.addQueue(addQueueField.getText());
        } catch (NullPointerException e) {
            System.err.println("Caught a null pointer exception. Alias likely null");
        } catch (NoSuchElementException e) {
            System.err.println("The server has not yet been saved and therefore does not exist in the hashmap");
            defaultQueue.getItems().add(addQueueField.getText());
        }
    }

    @FXML
    private void deleteQueue() {
        try {
            ServerInfo si = UserPreferences.getServers().get(alias.getText());
            si.deleteQueue(addQueueField.getText());
        } catch (NullPointerException e) {
            System.err.println("Caught a null pointer exception. Alias likely null");
        } catch (NoSuchElementException e) {
            System.err.println("The server has not yet been saved and therefore does not exist in the hashmap");
            defaultQueue.getItems().remove(addQueueField.getText());
        }
    }

    @FXML
    private void enableLibEFPPath() {
        libEFPInstallationPath.setDisable(!hasLibEFPButton.isSelected());
    }

    @FXML
    private void enableGAMESSPath() {
        GAMESSInstallationPath.setDisable(!hasGAMESSButton.isSelected());
    }

    //ArrayList<String> all_server_names = new ArrayList<>();

    @FXML
    private void saveServer() {

        UserPreferences.removeServer(alias.getText());
        ServerInfo si = new ServerInfo(alias.getText(), true);

        if (hostname.getText() == null || hostname.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The hostname is not entered. Please correct and save again.",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (username.getText() == null || username.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The username is not entered. Please correct and save again.",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (scheduler.getValue() == null || scheduler.getValue().toString().equals("null")) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The scheduler is not entered. Please correct and save again.",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }

        si.setHostname(hostname.getText());
        si.setUsername(username.getText());
        if (signInMethodComboBox.getSelectionModel().getSelectedItem().equals("SSH Key")) {
            si.setSshKeyMethod(true);
            si.setSshFileEncrypted(sshFileEncrypted.isSelected());
            si.setSshKeyLocation(signInFileLocationField.getText());
        } else {
            si.setSshKeyMethod(false);
            si.setPassword(signInPasswordField.getText());
        }
        si.setScheduler(scheduler.getValue().toString());

        if (hasLibEFPButton.isSelected()) {
            si.setHasLibEFP(true);
            si.setLibEFPPath(libEFPInstallationPath.getText());
        } else {
            si.setHasLibEFP(false);
        }
        if (hasGAMESSButton.isSelected()) {
            si.setHasGAMESS(true);
            si.setGamessPath(GAMESSInstallationPath.getText());
        } else {
            si.setHasGAMESS(false);
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("Server saved"),
                ButtonType.OK);
        alert.showAndWait();


        UserPreferences.addServer(si);
    }

    @FXML
    private void deleteServer() {
        System.out.println(servers.getValue());
        UserPreferences.removeServer(menuTree.getSelectionModel().getSelectedItem().getValue());
        initializeServers();
        clearServerForm();
        menuTree.getSelectionModel().clearSelection();
    }

    private void addNewServer() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Server");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Save Server Alias", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverAlias = new TextField();

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);


        grid.add(new Label("Server Alias:"), 0, 0);
        grid.add(serverAlias, 1, 0);


        // Do some validation (using the Java 8 lambda syntax).
        serverAlias.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> username.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return serverAlias.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            //servers.getChildren().add(new TreeItem<>(result.get()));
            openServerSettings();
            ServerInfo si = new ServerInfo(result.get(), true);
            loadServer(si);

            boolean duplicated_server_name = false;
            for (String serverName : UserPreferences.getServers().keySet()) {
                //System.out.println(serverName);
                if (serverName.equals(si.getEntryname())) {
                    duplicated_server_name = true;
                    break;
                }

            }
            //System.out.println(duplicated_server_name);
            if (!duplicated_server_name) {
                TreeItem<String> newServer = new TreeItem<>(result.get());
                servers.getChildren().add(newServer);
                menuTree.getSelectionModel().select(newServer);
                Platform.runLater(() -> hostname.requestFocus());

            } else {
                //System.out.println("caught!");
                //UserPreferences.removeServer(si.getEntryname());
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "The server is already exist, please use another server name",
                        ButtonType.OK);
                alert.showAndWait();
                return;
            }
            //UserPreferences.addServer(si);
        }
    }

    private void clearServerForm() {
        alias.setText("");
        hostname.setText("");
        username.setText("");
        signInPasswordField.setText("");
        signInFileLocationField.setText("");
        hasLibEFPButton.setSelected(false);
        hasGAMESSButton.setSelected(false);
        libEFPInstallationPath.setText("");
        GAMESSInstallationPath.setText("");
    }

    @FXML
    private void authenticateServer() {
        Connection connection;
        try {
            connection = new Connection(hostname.getText());
            connection.connect();
            if (!connection.authenticateWithPassword(username.getText(), signInPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        String.format("Was unable to connect to %s with your credentials", hostname.getText()),
                        ButtonType.OK);
                alert.showAndWait();
                return;
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    String.format("Was unable to connect to %s with your credentials", hostname.getText()),
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (hasLibEFPButton.isSelected()) {
            BundleManager libEFPBundleManager = new BundleManager(username.getText(),
                    signInPasswordField.getText(),
                    hostname.getText(),
                    "LIBEFP",
                    connection);
            if (!libEFPBundleManager.manageRemote()) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        String.format(
                                "Was able to connect to the remote, but was unable to verify the libEFP installation at %s",
                                libEFPInstallationPath.getText()),
                        ButtonType.OK);
                alert.showAndWait();
                return;
            }
        }

        if (hasGAMESSButton.isSelected()) {
            BundleManager libEFPBundleManager = new BundleManager(username.getText(),
                    signInPasswordField.getText(),
                    hostname.getText(),
                    "GAMESS",
                    connection);
            if (!libEFPBundleManager.manageRemote()) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        String.format(
                                "Was able to connect to the remote, but was unable to verify the GAMESS installation at %s",
                                GAMESSInstallationPath.getText()
                        ),
                        ButtonType.OK);
                alert.showAndWait();
                return;
            }
        }
        if (hasLibEFPButton.isSelected() && hasGAMESSButton.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Authenticated and verified installation of LIBEFP and GAMESS",
                    ButtonType.OK);
            alert.showAndWait();
        } else if (hasLibEFPButton.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Authenticated and verified installation of LIBEFP",
                    ButtonType.OK);
            alert.showAndWait();
        } else if (hasGAMESSButton.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Authenticated and verified installation of GAMESS",
                    ButtonType.OK);
            alert.showAndWait();
        } else if (hasLibEFPButton.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Authenticated",
                    ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void SignInMethodChanged() {
        tempPassword = signInPasswordField.getText();
        tempSshFileLocation = signInFileLocationField.getText();
        if (signInMethodComboBox.getValue().equals("SSH Key")) {
            passwordLabel.setText("SSH Key Location: ");
            locationHBox.setVisible(true);
            signInPasswordField.setVisible(false);
            signInFileLocationField.setText(tempSshFileLocation);
            sshFileEncrypted.setVisible(true);
            sshFileEncryptedLabel.setVisible(true);
        } else {
            passwordLabel.setText("Password: ");
            locationHBox.setVisible(false);
            signInPasswordField.setVisible(true);
            signInPasswordField.setText(tempPassword);
            sshFileEncrypted.setVisible(false);
            sshFileEncryptedLabel.setVisible(false);
        }
    }
}
