package org.ispiefp.app.server.view.config;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.ispiefp.app.server.ServerDetails;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerEditConfigViewController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextField submitField;

    @FXML
    private TextField queryField;

    @FXML
    private TextField killField;

    @FXML
    private TextField jobFileListField;

    @FXML
    private TextField queueInfoField;

    @FXML
    private TextArea runFileTemplateField;

    @FXML
    private TextArea runFileTemplateField2;

    @FXML
    private TextField updateIntervalField;

    private boolean okClicked;

    private ServerDetails.QueueOptions queueOptions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.queueOptions = new ServerDetails().new QueueOptions(); // Create an object for this
    }

    public void setOriginalQueueOptions(ServerDetails.QueueOptions queueOptions, String serverType) {
        submitField.setText(queueOptions.getSubmit());
        queryField.setText(queueOptions.getQuery());
        killField.setText(queueOptions.getKill());
        jobFileListField.setText(queueOptions.getJobFileList());
        queueInfoField.setText(queueOptions.getQueueInfo());
	runFileTemplateField.setText("#!/bin/bash\n" +
            "# --------------------------------\n" +
            "# iSpiEFP Gamess job template\n" +
            "# --------------------------------\n" +
            "#PBS -q lslipche\n" +
            "#PBS -l nodes=1:ppn=${NCPUS}\t\n" +
            "#PBS -l walltime=${WALLTIME} \n" +
            "#PBS -r n\n" +
            "#PBS -S /bin/bash\n" +
            "\n" +
            "# Set up environment for Gamess\n" +
            "module load gamess\n" +
            "\n" +
            "# And run Gamess\n" +
            "cd \"${PBS_O_WORKDIR}\" \n" +
            "run_gms ${JOB_NAME}.inp");    
	runFileTemplateField2.setText("#!/bin/bash\n" +
                "# ------------------------------\n" +
                "# iSpiEFP LibEFP job template\n" +
                "# ------------------------------\n" +
                "#PBS -q lslipche\n" +
                "#PBS -l nodes=1:ppn=${NCPUS}\t\n" +
                "#PBS -l walltime=${WALLTIME} \n" +
                "#PBS -r n\n" +
                "#PBS -S /bin/bash\n" +
                "\n" +
                "# Set up environment for LibEFP\n" +
                "# TODO: Ask a chemist!\n" +
                "\n" +
                "# And run LibEFP\n" +
                "cd \"${PBS_O_WORKDIR}\" \n" +
                "# TODO: Ask a chemist!");
        //runFileTemplateField.setText(queueOptions.getRunFileTemplate());
        updateIntervalField.setText(Integer.toString(queueOptions.getUpdateIntervalSecs()));
        if (serverType.equals("Local")) {
            queryField.setDisable(true);
            killField.setDisable(true);
            jobFileListField.setDisable(true);
            queueInfoField.setDisable(true);
            // runFileTemplateField.setDisable(true);
        }
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            // Set the values of this class variable when input is valid
            queueOptions.setSubmit(submitField.getText());
            queueOptions.setQuery(queryField.getText());
            queueOptions.setKill(killField.getText());
            queueOptions.setJobFileList(jobFileListField.getText());
            queueOptions.setQueueInfo(queueInfoField.getText());
            queueOptions.setRunFileTemplate(runFileTemplateField.getText());
            queueOptions.setUpdateIntervalSecs(Integer.parseInt(updateIntervalField.getText()));
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

    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (submitField.getText() == null || submitField.getText().length() == 0) {
            errorMessage += "No valid submit Field !\n";
        }

        if (queryField.getText() == null || queryField.getText().length() == 0) {
            errorMessage += "No valid query Field!\n";
        }

        if (killField.getText() == null || killField.getText().length() == 0) {
            errorMessage += "No valid kill Field!\n";
        }

        if (jobFileListField.getText() == null || jobFileListField.getText().length() == 0) {
            errorMessage += "No valid Job File List field!\n";
        }

        if (queueInfoField.getText() == null || queueInfoField.getText().length() == 0) {
            errorMessage += "No valid Queue Info Field!\n";
        }

        if (runFileTemplateField.getText() == null || runFileTemplateField.getText().length() == 0) {
            errorMessage += "No valid Run File Template Field!\n";
        }

        if (updateIntervalField.getText() == null || updateIntervalField.getText().length() == 0) {
            errorMessage += "No valid update Interval Field!\n";
        } else {
            // try to parse this value
            try {
                Integer.parseInt(updateIntervalField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid update Interval Field (must be an integer)!\n";
            }
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

    public ServerDetails.QueueOptions getQueueOptions() {
        return queueOptions;
    }

}
