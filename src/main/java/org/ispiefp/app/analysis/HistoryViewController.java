package org.ispiefp.app.analysis;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.ispiefp.app.jobSubmission.JobHistory;
import org.ispiefp.app.jobSubmission.SubmissionRecord;

import java.util.ArrayList;

public class HistoryViewController {
    @FXML
    private TextField jobSearchField;
    @FXML
    private TableView jobTable;
    @FXML
    private TableColumn jobName;
    @FXML
    private TableColumn jobStatus;
    @FXML
    private TableColumn submissionTime;
    @FXML
    private TableColumn onServer;

    private ObservableList<SubmissionRecord> observableJobs = FXCollections.observableArrayList();

    private ArrayList<SubmissionRecord> prevJobs;

    public HistoryViewController() {
        prevJobs = new JobHistory().getHistory();
        observableJobs.addAll(prevJobs);
    }

    @FXML
    public void initialize() {
        jobName.setCellValueFactory(new PropertyValueFactory<SubmissionRecord, String>("name"));
        jobStatus.setCellValueFactory(new PropertyValueFactory<SubmissionRecord, String>("status"));
        submissionTime.setCellValueFactory(new PropertyValueFactory<SubmissionRecord, String>("submissionTime"));
        onServer.setCellValueFactory(new PropertyValueFactory<SubmissionRecord, String>("hostname"));
        //Wrap the observables in a FilteredList
        FilteredList<SubmissionRecord> filteredData = new FilteredList<>(observableJobs, p -> true);

        //Set the filter predicate (Don't have to use lambdas if you don't want to (Requires Java 8)
        jobSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(sr -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (sr.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                else if (sr.getStatus().toLowerCase().contains(lowerCaseFilter)) return true;
                else return false;
            });
        });
        SortedList<SubmissionRecord> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(jobTable.comparatorProperty());
        jobTable.setItems(sortedData);
        Platform.runLater(() -> jobTable.refresh());
    }

}
