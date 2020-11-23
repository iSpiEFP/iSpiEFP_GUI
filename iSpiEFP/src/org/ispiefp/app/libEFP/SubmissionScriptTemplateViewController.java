package org.ispiefp.app.libEFP;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.ispiefp.app.jobSubmission.Submission;
import org.ispiefp.app.server.ServerInfo;

import java.net.URL;
import java.util.ResourceBundle;

    public class SubmissionScriptTemplateViewController implements Initializable {
        private Submission submission;
        private boolean submitted = false;

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

        public SubmissionScriptTemplateViewController(Submission submission) {
            this.submission = submission;
        }

        public SubmissionScriptTemplateViewController() { super(); }

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            queue.setPromptText("standby");
            numNodes.setPromptText("1");
            numProcs.setPromptText("20");

            walltime.setPromptText("hh:mm:ss");
            nextButton.setDisable(true);

            queue.textProperty().addListener((observable, ov, nv) -> {
                setQueue();
            });

            numNodes.textProperty().addListener((observable, ov, nv) -> {
                setNumNodes();
            });

            numProcs.textProperty().addListener((observable, ov, nv) -> {
                setNumProcs();
            });

            walltime.textProperty().addListener((observable, ov, nv) -> {
                setWalltime();
            });

            memory.textProperty().addListener((observable, ov, nv) -> {
                setMemory();
            });
        }

        @FXML
        private void updateSubmissionScriptText(){
            validateInput();
            if (submission.getSubmissionType().equalsIgnoreCase("LIBEFP"))
                submissionScriptTextArea.setText(submission.getLibEFPSubmissionScriptText());
            else submissionScriptTextArea.setText(submission.getGAMESSSubmissionScriptText());
        }

        private void validateInput(){
            boolean valid =
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
        public void setQueue(){
            submission.setQueueName(queue.getText());
            updateSubmissionScriptText();
        }

        @FXML
        public void setNumNodes(){
            try {
                submission.setNumNodes(Integer.parseInt(numNodes.getText()));
            } catch (NumberFormatException e){
                if (numNodes.getText().length() == 0) numNodes.setText("");
                else numNodes.setText(numNodes.getText(0, numNodes.getText().length() - 1));
            }
            updateSubmissionScriptText();
        }

        @FXML
        public void setNumProcs(){
            try{
            submission.setNumProcessors(Integer.parseInt(numProcs.getText()));
            } catch (NumberFormatException e){
                if (numProcs.getText().length() == 0) numProcs.setText("");
                else numProcs.setText(numProcs.getText(0, numProcs.getText().length() - 1));
            }
            updateSubmissionScriptText();
        }

        @FXML
        public void setWalltime(){
            submission.setWalltime(walltime.getText());
            updateSubmissionScriptText();
        }

        @FXML
        public void setMemory(){
            try{
                submission.setMem(Integer.parseInt(memory.getText()));
            } catch (NumberFormatException e){
                if (memory.getText().length() == 0) memory.setText("");
                else memory.setText(memory.getText(0, memory.getText().length() - 1));
            }
            updateSubmissionScriptText();
        }

        public void setSubmission(Submission submission) {
            this.submission = submission;
            updateSubmissionScriptText();
        }

        public void setServer(ServerInfo server) {
            this.server = server;
        }

        public ServerInfo getServer() {
            return server;
        }

        public boolean isSubmitted() {
            return submitted;
        }

        public void handleSubmit(){
            submitted = true;
            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.close();
        }

        public String getUsersSubmissionScript(){
            return submissionScriptTextArea.getText();
        }

    }
