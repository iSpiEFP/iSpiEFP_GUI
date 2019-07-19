package org.libefp.app.server.view;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.libefp.app.server.ServerDetails;
import org.libefp.app.server.view.config.ServerEditConfigViewController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServerEditViewController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextField serverNameField;

    // Assign to this toggle group when multiple radio buttons are added
    @FXML
    private ToggleGroup serverTypeGroup;

    @FXML
    private RadioButton sshRadioButton;

    @FXML
    private RadioButton localRadioButton;

    @FXML
    private TextField serverAddressField;

    @FXML
    private TextField portField;


    @FXML
    private TextField workingDirectoryField;

    @FXML
    private ComboBox<String> queueSystem;

    private ServerDetails serverDetails;

    private ServerDetails.QueueOptions queueOptions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initializing queue ComboBox
        List<String> queueSystemTypes = new ArrayList<String>();
        queueSystemTypes.add("Basic");
        queueSystemTypes.add("PBS");
        queueSystemTypes.add("SGE");

        queueSystem.setItems(FXCollections.observableList(queueSystemTypes));
        queueSystem.setValue("PBS");
        // Set sshRadioButton to true by default
        sshRadioButton.setSelected(true);
        // On change of server Type to local or SSH
        serverTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                RadioButton checked = (RadioButton) new_toggle;
                // System.out.println(checked.getText());
                String serverType = checked.getText();
                if (serverType.equals("SSH")) {
                    disableFieldsAndSetTextsForServerType(false);
                } else if (serverType.equals("Local")) {
                    disableFieldsAndSetTextsForServerType(true);
                }
            }
        });
    }

    private void disableFieldsAndSetTextsForServerType(boolean value) {
        serverAddressField.setDisable(value);
        portField.setDisable(value);
        if (value) {
            serverAddressField.setText("localhost");
            portField.setText("0");
        } else {
            portField.setText("22");
        }
    }

    private boolean okClicked = false;

    /**
     * Sets the server Details to be edited.
     *
     * @param serverDetails
     */
    public void setServerDetails(ServerDetails serverDetails) {
        this.serverDetails = serverDetails;

        serverNameField.setText(serverDetails.getServerName());
        String serverType = serverDetails.getServerType();
        if (serverType.equals("SSH")) {
            serverTypeGroup.selectToggle(sshRadioButton);
        } else if (serverType.equals("Local")) {
            serverTypeGroup.selectToggle(localRadioButton);
        }
        serverAddressField.setText(serverDetails.getAddress());
        portField.setText(Integer.toString(serverDetails.getPort()));
        workingDirectoryField.setText(serverDetails.getWorkingDirectory());
        if (serverDetails.getQueueSystemType() != null && serverDetails.getQueueSystemType().length() != 0)
            queueSystem.setValue(serverDetails.getQueueSystemType());
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        // TODO : Save the serverDetails to the preferences when ok is clicked! 
        if (isInputValid()) {
            serverDetails.setServerName(serverNameField.getText());
            serverDetails.setServerType(((RadioButton) serverTypeGroup.getSelectedToggle()).getText());
            serverDetails.setAddress(serverAddressField.getText());
            serverDetails.setPort(Integer.parseInt(portField.getText()));
            serverDetails.setWorkingDirectory(workingDirectoryField.getText());
            serverDetails.setQueueSystemType(queueSystem.getValue());
            if (queueOptions != null) { // This is set during configure routine
                serverDetails.setQueueOptions(queueOptions);
            }
            okClicked = true;
            ((Stage) root.getScene().getWindow()).close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        ((Stage) root.getScene().getWindow()).close();
    }

    /**
     * Called when the user clicks configure.
     *
     * @throws IOException
     */
    @FXML
    private void handleConfigure() throws IOException {
        boolean okClicked = showServerEditConfigView();
        if (okClicked) {
            // serversList.getItems().add(serverDetails);
            // Don't save anything until the handleOk is clicked!
        }
    }

    private boolean showServerEditConfigView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("config/ServerEditConfigView.fxml"));
        Parent serverEditConfigView = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Queue Options");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner((Stage) root.getScene().getWindow());
        stage.setScene(new Scene(serverEditConfigView));

        // Set the serverDetails into the controller
        String selectedserverType = ((RadioButton) serverTypeGroup.getSelectedToggle()).getText();
        ServerEditConfigViewController controller = loader.getController();
        if (queueOptions != null) {
            controller.setOriginalQueueOptions(queueOptions, selectedserverType);
        } else {
            // Called first time!
            controller.setOriginalQueueOptions(serverDetails.getQueueOptions(), selectedserverType);
        }
        stage.showAndWait();
        if (controller.isOkClicked()) this.queueOptions = controller.getQueueOptions();
        return controller.isOkClicked();
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (serverNameField.getText() == null || serverNameField.getText().length() == 0) {
            errorMessage += "No valid server name!\n";
        }

        if (serverAddressField.getText() == null || serverAddressField.getText().length() == 0) {
            errorMessage += "No valid server address!\n";
        }

        if (portField.getText() == null || portField.getText().length() == 0) {
            errorMessage += "No valid port!\n";
        } else {
            // try to parse the port into an int.
            try {
                Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid port (must be an integer)!\n";
            }
        }

        String serverType = ((RadioButton) serverTypeGroup.getSelectedToggle()).getText();


        if (workingDirectoryField.getText() == null || workingDirectoryField.getText().length() == 0) {
            errorMessage += "No valid working Directory!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner((Stage) root.getScene().getWindow());
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}
