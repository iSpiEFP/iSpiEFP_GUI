package org.ispiefp.app.submission;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.libEFP.OutputFile;
import org.ispiefp.app.visualizer.JmolMainPanel;

import java.io.File;
import java.io.IOException;

public class JobViewController {
    /* Fields controlling the displayed text */
    @FXML private Text jobName;
    @FXML private Text submissionTime;
    @FXML private Text finishTime;
    @FXML private Text outputFile;
    @FXML private Text errorFile;
    @FXML private Text currentlyVisualizedFile;

    /* Fields controlling drop down options */
    @FXML private ComboBox<String> usedEFPFiles;

    /* Fields controlling jmol interaction */
    @FXML private Pane previewPane;
    private JmolMainPanel jmolPreviewPanel;


    /* Fields controlling file content visualization */
    @FXML private TextArea fileContentsTextArea;
    @FXML private Button visualizeInputButton;
    @FXML private Button visualizeOutputButton;
    @FXML private Button visualizeErrorButton;

    /* Record for the job the user is viewing */
    private SubmissionRecord record;

    public JobViewController(SubmissionRecord record){
        super();
        this.record = record;
    }

    public void initialize(){
        jmolPreviewPanel = new JmolMainPanel(previewPane, new ListView<>());
        jobName.setText(record.getJob_id());
        submissionTime.setText(record.getTime());
        if (record.getStatus().equalsIgnoreCase("complete")){
            //todo Add finish time as a field
        }
        finishTime.setText("Running...");
        outputFile.setText(record.getLocalOutputFilePath());
        errorFile.setText(record.getLocalStdoutputFilePath());
//        ObservableList<String> observableEFPFiles = FXCollections.observableArrayList(record.getUsedEfpFilepaths());
//        usedEFPFiles.setItems(observableEFPFiles);

        visualizeInputButton.setOnAction(action -> populateTextArea(usedEFPFiles.getSelectionModel().getSelectedItem(), true));
        visualizeOutputButton.setOnAction(action -> populateTextArea(outputFile.getText(), true));
        visualizeErrorButton.setOnAction(action -> populateTextArea(errorFile.getText(), false));
    }

    public void setRecord(SubmissionRecord record) {
        this.record = record;
    }

    public void populateTextArea(String filePath, boolean isVisualizable){
        String fileContents = null;
        try {
            fileContents = FileUtils.readFileToString(new File(filePath), "UTF-8");
            currentlyVisualizedFile.setText("Currently Visualized File: " + filePath);
        } catch(IOException e) {
            System.err.printf("Could not read the file: %s%n", filePath);
        }
        if (fileContents != null) fileContentsTextArea.setText(fileContents);
        else fileContentsTextArea.setText("Was unable to open the file");

        if (isVisualizable){
            OutputFile outfile;
            try {
                outfile = new OutputFile(filePath);
            } catch (IOException e){
                e.printStackTrace();
                return;
            }
            outfile.viewState(jmolPreviewPanel, outfile.getStates().size() - 1);
        }
    }
}
