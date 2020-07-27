package org.ispiefp.app.libEFP;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.ispiefp.app.server.ServerInfo;

import java.net.URL;
import java.util.ResourceBundle;

public class SubmissionScriptWindow implements Initializable {
    private libEFPSubmission submission;

    @FXML
    private TextField jobName;
    @FXML
    private TextField queue;
    @FXML
    private TextField numNodes;
    @FXML
    private TextField numProcs;
    @FXML
    private TextField walltime;
    @FXML
    private TextField memory;
    @FXML
    private TextArea submissionScriptTextArea;
    @FXML
    private Button nextButton;

    private ServerInfo server;

    public SubmissionScriptWindow(libEFPSubmission submission) {
        this.submission = submission;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jobName.setPromptText("My Job");
        queue.setPromptText("standby");
        numNodes.setPromptText("1");
        numProcs.setPromptText("20");

        walltime.setPromptText("hh:mm:ss");
        nextButton.setDisable(true);
    }

    @FXML
    private void updateSubmissionScriptText(){
        submissionScriptTextArea.setText(submission.getSubmissionScriptText());
        validateInput();
    }

    private void validateInput(){
        boolean valid =
                jobName.getText()  != "" &&
                queue.getText()    != "" &&
                numNodes.getText() != "" &&
                numProcs.getText() != "" &&
                walltime.getText() != "" &&
                memory.getText()   != "";
        if (!valid){
            nextButton.setDisable(true);
            return;
        }
        String reqWalltime = walltime.getText();
        if (!reqWalltime.matches("^[0-9]+[0-9]?:[0-6][0-9]:[0-6][0-9]$")){
            nextButton.setDisable(true);
            return;
        }
        if (!(numNodes.getText().matches("^[0-9]+$") &&
              numProcs.getText().matches("^[0-9]+$") &&
              memory.getText().matches("^[0-9]+$"))) {
            nextButton.setDisable(true);
            return;
        }
        nextButton.setDisable(false);
        return;
    }

    @FXML
    public void setJobName(){
        submission.setOutputFilename(jobName.getText());
        updateSubmissionScriptText();
    }

    @FXML
    public void setQueue(){
        submission.setQueueName(queue.getText());
        updateSubmissionScriptText();
    }

    @FXML
    public void setNumNodes(){
        submission.setNumNodes(Integer.parseInt(numNodes.getText()));
        updateSubmissionScriptText();
    }

    @FXML
    public void setNumProcs(){
        submission.setNumProcessors(Integer.parseInt(numProcs.getText()));
        updateSubmissionScriptText();
    }

    @FXML
    public void setWalltime(){
        submission.setWalltime(walltime.getText());
        updateSubmissionScriptText();
    }

    @FXML
    public void setMemory(){
        submission.setMem(Integer.parseInt(memory.getText()));
        updateSubmissionScriptText();
    }

    public void setServer(ServerInfo server) {
        this.server = server;
    }

    public ServerInfo getServer() {
        return server;
    }

    public void handleNext(){

    }

}