/*
 *     iSpiEFP is an open source workflow optimization program for chemical simulation which provides an interactive GUI and interfaces with the existing libraries GAMESS and LibEFP.
 *     Copyright (C) 2021  Lyudmila V. Slipchenko
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please direct all questions regarding iSpiEFP to Lyudmila V. Slipchenko (lslipche@purdue.edu)
 */

package org.ispiefp.app.jobSubmission;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;
import org.ispiefp.app.libEFP.LibEFPOutputFile;
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
            LibEFPOutputFile outfile;
            try {
                outfile = new LibEFPOutputFile(filePath);
            } catch (IOException e){
                e.printStackTrace();
                return;
            }
            outfile.viewState(jmolPreviewPanel, outfile.getStates().size() - 1);
        }
    }
}
