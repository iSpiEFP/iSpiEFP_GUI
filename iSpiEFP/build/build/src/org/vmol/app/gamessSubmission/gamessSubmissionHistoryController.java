package org.vmol.app.gamessSubmission;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class gamessSubmissionHistoryController {
    private static Preferences userPrefs = Preferences.userNodeForPackage(gamessSubmissionHistoryController.class);

    @FXML
    private Parent root;
    @FXML
    private TableView<gamessSubmissionRecord> tableView;

    @FXML
    public void initialize() throws BackingStoreException {
        ObservableList<gamessSubmissionRecord> data = tableView.getItems();

        String[] keys = userPrefs.keys();

        for (int i = 0; i < keys.length; i++) {
            gamessSubmissionRecord r = new gamessSubmissionRecord(keys[i], "Queuing", userPrefs.get(keys[i], null));
            data.add(r);
        }
    }

    @FXML
    public void clearRecords() throws BackingStoreException {
        userPrefs.clear();
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().clear();
        }
    }

    @FXML
    public void refresh() {

    }

}
